package com.mobiletreeplantingapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobiletreeplantingapp.data.model.SavedArea
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : FirestoreRepository {

    private val areasCollection = firestore.collection("areas")

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
}