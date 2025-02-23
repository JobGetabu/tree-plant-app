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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

@Singleton
class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FirestoreRepository {

    private val areasCollection = firestore.collection("areas")
    private val treeProgressCollection = firestore.collection("tree_progress")

    override suspend fun saveArea(area: SavedArea): Result<Unit> = try {
        Log.d("FirestoreRepo", "Starting to save area: ${area.name}")
        
        // Create a map of the area data
        val areaMap = hashMapOf(
            "userId" to area.userId,
            "name" to area.name,
            "points" to area.points,
            "areaSize" to area.areaSize,
            "soilType" to area.soilType,
            "elevation" to area.elevation,
            "climateZone" to area.climateZone,
            "timestamp" to area.timestamp
        )

        // Use coroutines with Tasks API
        suspendCancellableCoroutine { continuation ->
            areasCollection.add(areaMap)
                .addOnSuccessListener { documentRef ->
                    Log.d("FirestoreRepo", "Area saved successfully with ID: ${documentRef.id}")
                    continuation.resume(Result.success(Unit)) {}
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreRepo", "Error saving area", e)
                    continuation.resume(Result.failure(e)) {}
                }
        }
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

    override fun getTreeProgress(treeId: String): Flow<Result<TreeProgress>> = callbackFlow {
        try {
            val progressRef = treeProgressCollection.document(treeId)
            
            val listener = progressRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val progress = snapshot.toObject(TreeProgress::class.java)
                    if (progress != null) {
                        trySend(Result.success(progress))
                    } else {
                        trySend(Result.failure(Exception("Failed to parse tree progress")))
                    }
                } else {
                    trySend(Result.failure(Exception("Tree progress not found")))
                }
            }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            trySend(Result.failure(e))
            close(e)
        }
    }

    override suspend fun updateTreeProgress(progress: TreeProgress): Result<Unit> = try {
        treeProgressCollection.document(progress.treeId)
            .set(progress)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
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
}