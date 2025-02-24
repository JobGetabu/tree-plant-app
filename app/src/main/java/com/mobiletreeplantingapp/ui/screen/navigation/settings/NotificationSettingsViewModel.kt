package com.mobiletreeplantingapp.ui.screen.navigation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiletreeplantingapp.data.model.NotificationPreferences
import com.mobiletreeplantingapp.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationSettingsState())
    val state = _state.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            try {
                val preferences = preferencesRepository.getNotificationPreferences()
                _state.value = _state.value.copy(
                    preferences = preferences,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to load preferences: ${e.message}",
                    isLoading = false
                )
            }
        }
    }

    fun updatePreference(update: (NotificationPreferences) -> NotificationPreferences) {
        viewModelScope.launch {
            try {
                val updatedPreferences = update(_state.value.preferences)
                preferencesRepository.updateNotificationPreferences(updatedPreferences)
                _state.value = _state.value.copy(preferences = updatedPreferences)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    error = "Failed to update preferences: ${e.message}"
                )
            }
        }
    }
} 