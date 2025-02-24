
package com.mobiletreeplantingapp.ui.screen.navigation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    var currentDisplayName = FirebaseAuth.getInstance().currentUser?.displayName ?: ""
        private set

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun updateProfile(newDisplayName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            firestoreRepository.updateUserProfile(newDisplayName)
                .onSuccess {
                    currentDisplayName = newDisplayName
                    onSuccess()
                }
                .onFailure { 
                    _error.value = it.message ?: "Failed to update profile"
                }

            _isLoading.value = false
        }
    }
}
