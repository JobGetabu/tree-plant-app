package com.mobiletreeplantingapp.ui.screen.planting

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiletreeplantingapp.data.model.TreeProgress
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import com.mobiletreeplantingapp.data.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlantingGuideViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val storageRepository: StorageRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    var state by mutableStateOf(PlantingGuideState())
        private set

    fun initializeTreeProgress(treeId: String, species: String) {
        if (treeId.isBlank() || species.isBlank()) {
            state = state.copy(error = "Invalid tree ID or species")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing tree progress for treeId: $treeId, species: $species")
                state = state.copy(isLoading = true, error = null)

                // Create initial progress
                val initialProgress = TreeProgress(
                    treeId = treeId,
                    species = species,
                    plantedDate = System.currentTimeMillis(),
                    completedSteps = emptyList(),
                    photos = emptyList()
                )

                // Save initial progress if it doesn't exist
                firestoreRepository.saveTreeProgress(initialProgress)
                    .onSuccess {
                        state = state.copy(
                            progress = initialProgress,
                            isLoading = false
                        )
                        Log.d(TAG, "Tree progress initialized: $initialProgress")
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Error initializing tree progress", error)
                        state = state.copy(
                            error = "Failed to initialize tree: ${error.message}",
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Error in initializeTreeProgress", e)
                state = state.copy(
                    error = "Failed to initialize tree: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun markStepCompleted(stepId: Int) {
        viewModelScope.launch {
            val updatedSteps = state.progress.completedSteps + stepId
            firestoreRepository.updateTreeProgress(
                state.progress.copy(completedSteps = updatedSteps)
            )
        }
    }

    fun addPhoto(photoUri: Uri) {
        viewModelScope.launch {
            try {
                state = state.copy(isUploading = true)
                Log.d(TAG, "Starting photo upload process")

                val treeId = state.progress.treeId
                if (treeId.isBlank()) {
                    throw IllegalStateException("No tree ID available. Current progress: ${state.progress}")
                }

                Log.d(TAG, "Uploading photo for tree: $treeId")
                val photoUrl = storageRepository.uploadTreePhoto(
                    treeId = treeId,
                    photoUri = photoUri
                )
                Log.d(TAG, "Photo uploaded successfully: $photoUrl")

                // Update tree progress with new photo
                val updatedPhotos = state.progress.photos + photoUrl
                val updatedProgress = state.progress.copy(photos = updatedPhotos)
                
                firestoreRepository.updateTreeProgress(updatedProgress)
                Log.d(TAG, "Tree progress updated with new photo")

                state = state.copy(
                    progress = updatedProgress,
                    isUploading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload photo", e)
                state = state.copy(
                    error = "Failed to upload photo: ${e.message}",
                    isUploading = false
                )
            }
        }
    }

    fun deletePhoto(photoUrl: String) {
        viewModelScope.launch {
            try {
                // Delete from storage
                storageRepository.deleteTreePhoto(photoUrl)

                // Update tree progress without the deleted photo
                val updatedPhotos = state.progress.photos - photoUrl
                val updatedProgress = state.progress.copy(photos = updatedPhotos)
                
                firestoreRepository.updateTreeProgress(updatedProgress)
            } catch (e: Exception) {
                state = state.copy(
                    error = "Failed to delete photo: ${e.message}"
                )
            }
        }
    }

    fun loadPhotos(treeId: String) {
        if (treeId.isBlank()) {
            Log.e(TAG, "Cannot load photos: Tree ID is blank")
            return
        }

        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting to load photos for tree: $treeId")
                
                val photos = storageRepository.getPhotosForTree(treeId)
                Log.d(TAG, "Successfully loaded ${photos.size} photos")
                
                state = state.copy(
                    progress = state.progress.copy(photos = photos)
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error loading photos", e)
                state = state.copy(
                    error = "Failed to load photos: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any temporary files in the cache directory
        viewModelScope.launch(Dispatchers.IO) {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("compressed_")) {
                    file.delete()
                }
            }
        }
    }

    companion object {
        private const val TAG = "PlantingGuideViewModel"
    }
} 