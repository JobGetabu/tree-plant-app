package com.mobiletreeplantingapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.mobiletreeplantingapp.data.model.SavedArea
import com.mobiletreeplantingapp.data.model.TreeProgress
import com.mobiletreeplantingapp.data.model.GuideStep
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.util.Log
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Query
import com.mobiletreeplantingapp.data.model.SavedTree
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.mobiletreeplantingapp.data.model.UserStats
import com.mobiletreeplantingapp.data.model.TreeRecommendationData
import com.mobiletreeplantingapp.ui.screen.navigation.settings.SettingsViewModel
import kotlinx.coroutines.withContext
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint

@Singleton
class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FirestoreRepository {

    private val areasCollection = firestore.collection("areas")
    private val treeProgressCollection = firestore.collection("tree_progress")
    private val treesCollection = firestore.collection("trees")

    override suspend fun saveArea(area: SavedArea): Result<Unit> = try {
        Log.d("FirestoreRepo", "Starting to save area: ${area.name}")
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        // Create a batch to perform multiple operations atomically
        val batch = firestore.batch()

        // Add area document
        val areaRef = areasCollection.document()
        batch.set(
            areaRef,
            hashMapOf(
                "userId" to area.userId,
                "name" to area.name,
                "points" to area.points,
                "areaSize" to area.areaSize,
                "soilType" to area.soilType,
                "elevation" to area.elevation,
                "climateZone" to area.climateZone,
                "timestamp" to area.timestamp
            )
        )

        // Update user's total area
        val userRef = firestore.collection("users").document(userId)
        batch.set(
            userRef,
            mapOf("totalArea" to FieldValue.increment(area.areaSize)),
            SetOptions.merge()
        )

        // Commit the batch
        batch.commit().await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirestoreRepo", "Exception while saving area", e)
        Result.failure(e)
    }

    override fun getUserAreas(): Flow<Result<List<SavedArea>>> = callbackFlow {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            trySend(Result.failure(Exception("User not authenticated")))
            close()
            return@callbackFlow
        }

        val listener = areasCollection
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }

                val areas = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(SavedArea::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                trySend(Result.success(areas))
            }

        awaitClose { listener.remove() }
    }

    override fun getAreaById(areaId: String): Flow<Result<SavedArea>> = callbackFlow {
        val listener = areasCollection.document(areaId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    trySend(Result.failure(e))
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(Result.failure(Exception("Area not found")))
                    return@addSnapshotListener
                }

                val area = snapshot.toObject(SavedArea::class.java)?.copy(id = snapshot.id)
                if (area != null) {
                    trySend(Result.success(area))
                } else {
                    trySend(Result.failure(Exception("Failed to parse area data")))
                }
            }

        awaitClose { listener.remove() }
    }

    override fun getGuideSteps(species: String): List<GuideStep> {
        // This is a simplified version. In a real app, you might want to fetch this from Firestore
        // or a local database based on the tree species
        return when (species.lowercase()) {
            "oak" -> listOf(
                GuideStep(
                    id = 1,
                    title = "Prepare the Soil",
                    description = "Dig a hole twice the width of the root ball and as deep as the root ball.",
                    required = true
                ),
                GuideStep(
                    id = 2,
                    title = "Plant the Tree",
                    description = "Place the tree in the hole and backfill with soil.",
                    required = true
                ),
                GuideStep(
                    id = 3,
                    title = "Initial Watering",
                    description = "Water thoroughly after planting.",
                    required = true
                ),
                GuideStep(
                    id = 4,
                    title = "Add Mulch",
                    description = "Apply 2-4 inches of mulch around the base, keeping it away from the trunk.",
                    required = false
                )
            )
            // Add more species-specific guides as needed
            else -> listOf(
                GuideStep(
                    id = 1,
                    title = "Prepare the Soil",
                    description = "Dig a hole twice the width of the root ball and as deep as the root ball.",
                    required = true
                ),
                GuideStep(
                    id = 2,
                    title = "Plant the Tree",
                    description = "Place the tree in the hole and backfill with soil.",
                    required = true
                ),
                GuideStep(
                    id = 3,
                    title = "Water the Tree",
                    description = "Water thoroughly after planting.",
                    required = true
                )
            )
        }
    }

    override suspend fun saveTree(tree: SavedTree): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        // Create a batch to perform multiple operations atomically
        val batch = firestore.batch()

        // Add tree document
        val treeRef = treesCollection.document(tree.id)
        batch.set(
            treeRef,
            tree.toMap().plus(
                mapOf(
                    "userId" to userId,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
        )

        // Update user stats with atomic operations
        val userRef = firestore.collection("users").document(userId)
        val statsUpdate = mutableMapOf<String, Any>(
            "treesPlanted" to FieldValue.increment(1),
            "co2Offset" to FieldValue.increment(20), // 20kg CO2 per tree
            "lastPlantingDate" to FieldValue.serverTimestamp()
        )

        // Get the area size and update total area if needed
        val areaDoc = areasCollection.document(tree.areaId).get().await()
        val area = areaDoc.toObject(SavedArea::class.java)
        area?.let {
            // Only update area stats if it's the first tree in this area
            val existingTrees = treesCollection
                .whereEqualTo("areaId", tree.areaId)
                .get()
                .await()
            if (existingTrees.isEmpty) {
                statsUpdate["totalArea"] = FieldValue.increment(it.areaSize)
            }
        }

        batch.set(userRef, statsUpdate, SetOptions.merge())

        // Commit all operations
        batch.commit().await()
        Log.d(TAG, "Successfully saved tree and updated user stats")

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Error saving tree", e)
        Result.failure(e)
    }

    override suspend fun deleteTree(treeId: String): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        // First verify the tree belongs to the user and get tree data
        val treeDoc = treesCollection.document(treeId).get().await()
        if (treeDoc.getString("userId") != userId) {
            throw Exception("Unauthorized to delete this tree")
        }

        // Create batch operation
        val batch = firestore.batch()

        // Delete the tree
        batch.delete(treesCollection.document(treeId))

        // Update user stats (decrement counters)
        val userRef = firestore.collection("users").document(userId)
        batch.update(userRef, mapOf(
            "treesPlanted" to FieldValue.increment(-1),
            "co2Offset" to FieldValue.increment(-20) // Assuming 20kg CO2 per tree
        ))

        // Commit the batch
        batch.commit().await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Error deleting tree", e)
        Result.failure(e)
    }

    override suspend fun updateTree(tree: SavedTree): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        // First verify the tree belongs to the user
        val treeDoc = treesCollection.document(tree.id).get().await()
        if (treeDoc.getString("userId") != userId) {
            throw Exception("Unauthorized to update this tree")
        }

        treesCollection
            .document(tree.id)
            .update(tree.toMap().plus("updatedAt" to FieldValue.serverTimestamp()))
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Error updating tree", e)
        Result.failure(e)
    }

    override fun getAreaTrees(areaId: String): Flow<Result<List<SavedTree>>> = callbackFlow {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val listener = treesCollection
                .whereEqualTo("userId", userId)
                .whereEqualTo("areaId", areaId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Result.failure(error))
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val trees = snapshot.documents.mapNotNull { doc ->
                            try {
                                SavedTree(
                                    id = doc.id,
                                    species = doc.getString("species") ?: "",
                                    notes = doc.getString("notes") ?: "",
                                    areaId = doc.getString("areaId") ?: "",
                                    dateAdded = doc.getTimestamp("createdAt")?.seconds ?: 0
                                )
                            } catch (e: Exception) {
                                Log.e("FirestoreRepository", "Error parsing tree document", e)
                                null
                            }
                        }.sortedByDescending { it.dateAdded }
                        trySend(Result.success(trees))
                    }
                }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close(e)
        }
    }

    override suspend fun saveTreeProgress(progress: TreeProgress): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        Log.d(TAG, "Saving tree progress for treeId: ${progress.treeId}")

        treeProgressCollection
            .document(progress.treeId)
            .set(
                progress.toMap().plus(
                    mapOf(
                        "userId" to userId,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            )
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error saving tree progress", e)
        Result.failure(e)
    }

    override suspend fun updateTreeProgress(progress: TreeProgress): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        Log.d(TAG, "Updating tree progress for user: $userId, tree: ${progress.treeId}")

        val docRef = firestore.collection("users")
            .document(userId)
            .collection("tree_progress")
            .document(progress.treeId)

        // Always set the full document to ensure all fields are updated
        docRef.set(progress).await()

        Log.d(TAG, "Successfully updated tree progress")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error updating tree progress", e)
        Result.failure(e)
    }

    override suspend fun getTreeProgress(treeId: String): Flow<Result<TreeProgress?>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        Log.d(TAG, "Getting tree progress for user: $userId, tree: $treeId")

        val listener = firestore.collection("users")
            .document(userId)
            .collection("tree_progress")
            .document(treeId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting tree progress", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    try {
                        val progress = snapshot.toObject(TreeProgress::class.java)
                        Log.d(TAG, "Loaded progress: $progress")
                        trySend(Result.success(progress))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error converting snapshot to TreeProgress", e)
                        trySend(Result.failure(e))
                    }
                } else {
                    Log.d(TAG, "No existing progress found for tree: $treeId")
                    trySend(Result.success(null))
                }
            }

        awaitClose {
            Log.d(TAG, "Removing tree progress listener")
            listener.remove()
        }
    }

    override suspend fun deleteArea(areaId: String): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
        Log.d(TAG, "Starting area deletion process for areaId: $areaId")

        // Get the area details first
        val areaDoc = areasCollection.document(areaId).get().await()
        val area = areaDoc.toObject(SavedArea::class.java)
            ?: throw IllegalStateException("Area not found")

        // Get all trees in this area
        val treesSnapshot = treesCollection
            .whereEqualTo("areaId", areaId)
            .get()
            .await()

        val treesCount = treesSnapshot.size()
        Log.d(TAG, "Found $treesCount trees to delete")

        // Create a batch operation
        val batch = firestore.batch()

        // Delete all trees
        treesSnapshot.documents.forEach { treeDoc ->
            batch.delete(treeDoc.reference)
        }

        // Delete the area
        batch.delete(areasCollection.document(areaId))

        // Update user stats
        val userRef = firestore.collection("users").document(userId)
        batch.update(userRef, mapOf(
            "treesPlanted" to FieldValue.increment(-treesCount.toLong()),
            "totalArea" to FieldValue.increment(-area.areaSize),
            "co2Offset" to FieldValue.increment(-(treesCount * 20).toLong()) // Assuming 20kg CO2 per tree
        ))

        // Commit all operations
        batch.commit().await()
        Log.d(TAG, "Successfully deleted area and updated stats")

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting area", e)
        Result.failure(e)
    }

    override suspend fun deleteTreesByAreaId(areaId: String): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

        // Get all trees in the area
        val treesSnapshot = treesCollection
            .whereEqualTo("areaId", areaId)
            .get()
            .await()

        val treesCount = treesSnapshot.size()
        
        // Create batch operation
        val batch = firestore.batch()

        // Delete all trees
        treesSnapshot.documents.forEach { document ->
            batch.delete(document.reference)
        }

        // Update user stats
        val userRef = firestore.collection("users").document(userId)
        batch.update(userRef, mapOf(
            "treesPlanted" to FieldValue.increment(-treesCount.toLong()),
            "co2Offset" to FieldValue.increment(-(treesCount * 20).toLong())
        ))

        // Commit batch
        batch.commit().await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error deleting trees by area ID", e)
        Result.failure(e)
    }

    override suspend fun getUserStats(): Flow<Result<UserStats>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

        // Initialize user stats document if it doesn't exist
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            if (!userDoc.exists()) {
                firestore.collection("users").document(userId)
                    .set(
                        mapOf(
                            "treesPlanted" to 0,
                            "co2Offset" to 0,
                            "totalArea" to 0.0
                        )
                    )
                    .await()
            }
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "Error initializing user stats", e)
        }

        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    trySend(Result.success(UserStats()))
                    return@addSnapshotListener
                }

                val stats = UserStats(
                    treesPlanted = snapshot.getLong("treesPlanted")?.toInt() ?: 0,
                    co2Offset = snapshot.getLong("co2Offset")?.toInt() ?: 0,
                    totalArea = snapshot.getDouble("totalArea") ?: 0.0,
                    lastPlantingDate = snapshot.getTimestamp("lastPlantingDate")?.toDate()?.time
                )

                trySend(Result.success(stats))
            }

        awaitClose { listener.remove() }
    }

    private fun calculateAreaSize(points: List<LatLng>): Double {
        if (points.size < 3) return 0.0

        var area = 0.0
        val n = points.size
        for (i in 0 until n) {
            val j = (i + 1) % n
            area += points[i].latitude * points[j].longitude
            area -= points[j].latitude * points[i].longitude
        }
        area = kotlin.math.abs(area) / 2.0
        // Convert to hectares (approximate)
        return area * 111.32 * 111.32 * kotlin.math.cos(points[0].latitude * kotlin.math.PI / 180)
    }

    private fun DocumentSnapshot.toTreeProgress(): TreeProgress {
        return TreeProgress(
            treeId = id,
            species = getString("species") ?: "",
            plantedDate = getTimestamp("plantedDate")?.seconds ?: 0,
            completedSteps = (get("completedSteps") as? List<*>)?.mapNotNull { it as? Int } ?: emptyList(),
            photos = (get("photos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
        )
    }

    private fun TreeProgress.toMap(): Map<String, Any> {
        return mapOf(
            "treeId" to treeId,
            "species" to species,
            "plantedDate" to plantedDate,
            "completedSteps" to completedSteps,
            "photos" to photos
        )
    }

    private fun SavedTree.toMap() = mapOf(
        "species" to species,
        "notes" to notes,
        "areaId" to areaId,
        "dateAdded" to dateAdded
    )

    override suspend fun getTreeRecommendations(
        soilType: String,
        elevation: Double,
        climateZone: String
    ): Flow<Result<List<TreeRecommendationData>>> = callbackFlow {
        try {
            Log.d(TAG, "Fetching recommendations for: soil=$soilType, elevation=$elevation, climate=$climateZone")
            
            // Create collection reference
            val recommendationsRef = firestore.collection("tree_recommendations")
            
            // Use a simpler query with just one range filter
            val query = recommendationsRef
                .whereLessThanOrEqualTo("maxElevation", elevation + 200)
            
            val listener = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching recommendations", error)
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                try {
                    val recommendations = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(TreeRecommendationData::class.java)
                    }?.filter { recommendation ->
                        // Do additional filtering in memory
                        val elevationMatch = recommendation.minElevation <= (elevation + 200)
                        
                        val soilMatch = recommendation.suitableSoilTypes.any { soil ->
                            soil.contains(soilType.lowercase()) || soilType.lowercase().contains(soil)
                        }
                        
                        val climateMatch = recommendation.suitableClimateZones.any { climate ->
                            climate.contains(climateZone.lowercase()) || 
                            climateZone.lowercase().contains(climate) ||
                            areClimatesCompatible(climate, climateZone)
                        }
                        
                        elevationMatch && soilMatch && climateMatch
                    }?.sortedWith(
                        compareByDescending<TreeRecommendationData> { tree ->
                            calculateRelevanceScore(
                                tree,
                                elevation,
                                soilType,
                                climateZone
                            )
                        }.thenByDescending { it.suitabilityScore }
                    ) ?: emptyList()

                    Log.d(TAG, "Found ${recommendations.size} matching trees")
                    trySend(Result.success(recommendations))
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing recommendations", e)
                    trySend(Result.failure(e))
                }
            }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up recommendations query", e)
            trySend(Result.failure(e))
            close(e)
        }
    }

    private fun calculateRelevanceScore(
        tree: TreeRecommendationData,
        elevation: Double,
        soilType: String,
        climateZone: String
    ): Double {
        var score = tree.suitabilityScore.toDouble()

        // Elevation match factor (closer to target elevation = higher score)
        val elevationMidpoint = (tree.maxElevation + tree.minElevation) / 2
        val elevationDifference = kotlin.math.abs(elevation - elevationMidpoint)
        val elevationFactor = 1.0 - (elevationDifference / 1000.0).coerceAtMost(1.0)
        score *= elevationFactor

        // Soil type match factor
        if (tree.suitableSoilTypes.any { it == soilType.lowercase() }) {
            score *= 1.2 // Direct soil match bonus
        }

        // Climate zone match factor
        if (tree.suitableClimateZones.any { it == climateZone.lowercase() }) {
            score *= 1.2 // Direct climate match bonus
        }

        return score
    }

    private fun areClimatesCompatible(climate1: String, climate2: String): Boolean {
        // Define climate zone compatibility groups
        val compatibilityGroups = listOf(
            setOf("tropical", "subtropical", "tropical highland"),
            setOf("arid", "semi-arid", "savanna"),
            setOf("highland", "temperate", "subtropical highland"),
            setOf("coastal", "tropical", "subtropical")
        )

        return compatibilityGroups.any { group ->
            group.contains(climate1.lowercase()) && group.contains(climate2.lowercase())
        }
    }

    override suspend fun getTreeById(treeId: String): Result<SavedTree?> = try {
        Log.d(TAG, "Getting tree by ID: $treeId")
        val userId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")

        val treeDoc = treesCollection
            .document(treeId)
            .get()
            .await()

        if (treeDoc.exists()) {
            Log.d(TAG, "Found tree document")
            val tree = treeDoc.toObject(SavedTree::class.java)
            Result.success(tree)
        } else {
            Log.w(TAG, "Tree document not found")
            Result.success(null)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error getting tree by ID", e)
        Result.failure(e)
    }

    override suspend fun updateUserProfile(displayName: String): Result<Unit> = try {
        val user = auth.currentUser ?: throw Exception("User not authenticated")

        // Update Firebase Auth display name
        user.updateProfile(
            userProfileChangeRequest {
                setDisplayName(displayName)
            }
        ).await()

        // Update Firestore user document
        firestore.collection("users")
            .document(user.uid)
            .set(
                mapOf("displayName" to displayName),
                SetOptions.merge()
            )
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun getUserProfile(): Flow<Result<SettingsViewModel.UserProfile>> = callbackFlow {
        val user = auth.currentUser ?: throw Exception("User not authenticated")

        val listener = firestore.collection("users")
            .document(user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                val profile = SettingsViewModel.UserProfile(
                    displayName = user.displayName ?: "",
                    email = user.email ?: "",
                    photoUrl = user.photoUrl?.toString()
                )

                trySend(Result.success(profile))
            }

        awaitClose { listener.remove() }
    }

    companion object {
        private const val TAG = "FirestoreRepositoryImpl"
    }
}