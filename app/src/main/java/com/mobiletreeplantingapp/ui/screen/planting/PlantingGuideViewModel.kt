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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.SavedStateHandle

@HiltViewModel
class PlantingGuideViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val storageRepository: StorageRepository,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val treeId: String = checkNotNull(savedStateHandle.get<String>("treeId"))
    private val species: String = checkNotNull(savedStateHandle.get<String>("species"))

    private val _state = MutableStateFlow(PlantingGuideState())
    val state = _state.asStateFlow()

    init {
        Log.d(TAG, "Initializing ViewModel with treeId: $treeId, species: $species")
        loadTreeProgress()
    }

    fun initializeTreeProgress(treeId: String, species: String) {
        Log.d(TAG, "Reinitializing tree progress with treeId: $treeId, species: $species")
        loadTreeProgress()
    }

    private fun loadTreeProgress() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Get guide steps for the species
                val steps = firestoreRepository.getGuideSteps(species)
                Log.d(TAG, "Loaded ${steps.size} guide steps")

                // Start collecting tree progress
                firestoreRepository.getTreeProgress(treeId).collect { result ->
                    result.fold(
                        onSuccess = { progress ->
                            val currentProgress = progress ?: TreeProgress(
                                treeId = treeId,
                                species = species,
                                startDate = System.currentTimeMillis()
                            )
                            Log.d(TAG, "Loaded progress with ${currentProgress.completedSteps.size} completed steps")
                            
                            _state.value = _state.value.copy(
                                progress = currentProgress,
                                guideSteps = steps,
                                isLoading = false
                            )

                            // If this is a new progress, save it to Firestore
                            if (progress == null) {
                                firestoreRepository.updateTreeProgress(currentProgress)
                            }
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error loading progress", error)
                            _state.value = _state.value.copy(
                                error = "Failed to load progress: ${error.message}",
                                isLoading = false
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in loadTreeProgress", e)
                _state.value = _state.value.copy(
                    error = "Failed to load tree progress: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun markStepCompleted(stepId: Int) {
        viewModelScope.launch {
            try {
                val currentProgress = _state.value.progress
                val updatedSteps = currentProgress.completedSteps.toMutableList()
                updatedSteps.add(stepId)

                val updatedProgress = currentProgress.copy(
                    completedSteps = updatedSteps,
                    lastUpdated = System.currentTimeMillis()
                )

                // Update local state immediately
                _state.value = _state.value.copy(progress = updatedProgress)

                // Update Firestore
                firestoreRepository.updateTreeProgress(updatedProgress).fold(
                    onSuccess = {
                        Log.d(TAG, "Successfully updated progress with step $stepId")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to update progress", error)
                        // Revert local state on failure
                        _state.value = _state.value.copy(
                            progress = currentProgress,
                            error = "Failed to update progress: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error in markStepCompleted", e)
                _state.value = _state.value.copy(
                    error = "Failed to mark step as completed: ${e.message}"
                )
            }
        }
    }

    fun addPhoto(photoUri: Uri) {
        viewModelScope.launch {
            try {
                _state.value = _state.value.copy(isUploading = true)
                Log.d(TAG, "Starting photo upload process")

                val treeId = _state.value.progress.treeId
                if (treeId.isBlank()) {
                    throw IllegalStateException("No tree ID available. Current progress: ${_state.value.progress}")
                }

                Log.d(TAG, "Uploading photo for tree: $treeId")
                val photoUrl = storageRepository.uploadTreePhoto(
                    treeId = treeId,
                    photoUri = photoUri
                )
                Log.d(TAG, "Photo uploaded successfully: $photoUrl")

                // Update tree progress with new photo
                val updatedPhotos = _state.value.progress.photos + photoUrl
                val updatedProgress = _state.value.progress.copy(photos = updatedPhotos)
                
                firestoreRepository.updateTreeProgress(updatedProgress)
                Log.d(TAG, "Tree progress updated with new photo")

                _state.value = _state.value.copy(
                    progress = updatedProgress,
                    isUploading = false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to upload photo", e)
                _state.value = _state.value.copy(
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
                val updatedPhotos = _state.value.progress.photos - photoUrl
                val updatedProgress = _state.value.progress.copy(photos = updatedPhotos)
                
                firestoreRepository.updateTreeProgress(updatedProgress)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to delete photo: ${e.message}"
                )
            }
        }
    }

    fun loadPhotos(treeId: String) {
        viewModelScope.launch {
            try {
                // The photos are already being loaded as part of the tree progress
                // This function is here for completeness and future expansion
                Log.d(TAG, "Photos already loaded with tree progress")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading photos", e)
                _state.value = _state.value.copy(
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