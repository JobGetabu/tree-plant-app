package com.mobiletreeplantingapp.ui.screen.navigation.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import com.mobiletreeplantingapp.data.repository.StorageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    var currentDisplayName = FirebaseAuth.getInstance().currentUser?.displayName ?: ""
        private set
        
    var currentPhotoUrl = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString()
        private set

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    
    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri
    
    fun setSelectedImageUri(uri: Uri?) {
        _selectedImageUri.value = uri
    }

    fun updateProfile(newDisplayName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Update display name
                firestoreRepository.updateUserProfile(newDisplayName)
                    .onSuccess {
                        currentDisplayName = newDisplayName
                    }
                    .onFailure { 
                        _error.value = it.message ?: "Failed to update profile name"
                        _isLoading.value = false
                        return@launch
                    }
                
                // Update profile image if selected
                _selectedImageUri.value?.let { uri ->
                    try {
                        val photoUrl = storageRepository.uploadProfileImage(uri)
                        firestoreRepository.updateUserProfileImage(photoUrl)
                            .onSuccess {
                                currentPhotoUrl = photoUrl
                            }
                            .onFailure {
                                _error.value = it.message ?: "Failed to update profile image"
                                _isLoading.value = false
                                return@launch
                            }
                    } catch (e: Exception) {
                        _error.value = e.message ?: "Failed to upload profile image"
                        _isLoading.value = false
                        return@launch
                    }
                }
                
                onSuccess()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
