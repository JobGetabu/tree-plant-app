package com.mobiletreeplantingapp.ui.screen.planting

import android.content.Context
import android.net.Uri
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

    fun loadTreeProgress(treeId: String, species: String) {
        viewModelScope.launch {
            state = state.copy(isLoading = true)
            try {
                // Initialize new tree progress if it doesn't exist
                val initialProgress = TreeProgress(
                    treeId = treeId,
                    plantedDate = System.currentTimeMillis(),
                    species = species,
                    completedSteps = emptyList(),
                    photos = emptyList()
                )

                // Load or create tree progress
                firestoreRepository.getTreeProgress(treeId)
                    .collect { result ->
                        result.onSuccess { progress ->
                            state = state.copy(
                                progress = progress ?: initialProgress,
                                guideSteps = firestoreRepository.getGuideSteps(species),
                                isLoading = false
                            )
                        }.onFailure { error ->
                            state = state.copy(
                                error = error.message,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                state = state.copy(
                    error = e.message,
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

    fun addPhoto(uri: Uri) {
        viewModelScope.launch {
            state = state.copy(isUploadingPhoto = true)
            try {
                val photoUrl = storageRepository.uploadPhoto(
                    state.progress.treeId,
                    uri
                )
                
                // Update the tree progress with the new photo
                val updatedPhotos = state.progress.photos + photoUrl
                firestoreRepository.updateTreeProgress(
                    state.progress.copy(photos = updatedPhotos)
                )
                
                state = state.copy(
                    isUploadingPhoto = false,
                    progress = state.progress.copy(photos = updatedPhotos)
                )
            } catch (e: Exception) {
                state = state.copy(
                    error = e.message,
                    isUploadingPhoto = false
                )
            }
        }
    }

    fun deletePhoto(photoUrl: String) {
        viewModelScope.launch {
            try {
                storageRepository.deletePhoto(photoUrl)
                
                // Update the tree progress without the deleted photo
                val updatedPhotos = state.progress.photos - photoUrl
                firestoreRepository.updateTreeProgress(
                    state.progress.copy(photos = updatedPhotos)
                )
                
                state = state.copy(
                    progress = state.progress.copy(photos = updatedPhotos)
                )
            } catch (e: Exception) {
                state = state.copy(error = e.message)
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
} 