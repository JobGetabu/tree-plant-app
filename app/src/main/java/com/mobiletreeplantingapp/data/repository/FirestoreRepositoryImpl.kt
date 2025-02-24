package com.mobiletreeplantingapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.mobiletreeplantingapp.data.model.SavedTree
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.mobiletreeplantingapp.data.model.UserStats
import com.mobiletreeplantingapp.data.model.TreeRecommendationData
import com.mobiletreeplantingapp.ui.screen.navigation.settings.SettingsViewModel

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
        
        // Update user stats
        val userRef = firestore.collection("users").document(userId)
        batch.set(
            userRef,
            mapOf(
                "treesPlanted" to FieldValue.increment(1),
                "co2Offset" to FieldValue.increment(20), // Assuming average CO2 offset per tree
                "lastPlantingDate" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
        )
        
        // Commit the batch
        batch.commit().await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirestoreRepository", "Error saving tree", e)
        Result.failure(e)
    }

    override suspend fun deleteTree(treeId: String): Result<Unit> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        
        // First verify the tree belongs to the user
        val treeDoc = treesCollection.document(treeId).get().await()
        if (treeDoc.getString("userId") != userId) {
            throw Exception("Unauthorized to delete this tree")
        }
        
        treesCollection.document(treeId).delete().await()
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

    override fun getUserStats(): Flow<Result<UserStats>> = callbackFlow {
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
        val query = firestore.collection("tree_recommendations")
            .whereArrayContains("suitableSoilTypes", soilType.lowercase())
            .whereLessThanOrEqualTo("maxElevation", elevation)
            .orderBy("maxElevation")

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }

            val recommendations = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(TreeRecommendationData::class.java)
            }?.filter { recommendation ->
                recommendation.minElevation <= elevation &&
                recommendation.suitableClimateZones.contains(climateZone.lowercase())
            }?.sortedByDescending { it.suitabilityScore } ?: emptyList()

            trySend(Result.success(recommendations))
        }

        awaitClose { listener.remove() }
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