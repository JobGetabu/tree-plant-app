package com.mobiletreeplantingapp.ui.screen.navigation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.mobiletreeplantingapp.data.datastore.ThemeMode
import com.mobiletreeplantingapp.data.datastore.ThemePreferences
import com.mobiletreeplantingapp.data.model.UserStats
import com.mobiletreeplantingapp.data.repository.FirestoreRepository
import com.mobiletreeplantingapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themePreferences: ThemePreferences,
    private val firestoreRepository: FirestoreRepository
) : ViewModel() {

    data class UserProfile(
        val displayName: String = "",
        val email: String = "",
        val photoUrl: String? = null
    )

    var currentTheme by mutableStateOf(ThemeMode.SYSTEM)
        private set
        
    var userProfile by mutableStateOf(UserProfile())
        private set

    var userStats by mutableStateOf(UserStats())
        private set

    init {
        viewModelScope.launch {
            themePreferences.themeMode.collectLatest { theme ->
                currentTheme = theme
            }
        }
        observeUserProfile()
        loadUserStats()
    }

    private fun observeUserProfile() {
        viewModelScope.launch {
            firestoreRepository.getUserProfile()
                .catch { e ->
                    // Handle error
                }
                .collect { result ->
                    result.onSuccess { profile ->
                        userProfile = profile
                    }
                }
        }
    }

    private fun loadUserStats() {
        viewModelScope.launch {
            firestoreRepository.getUserStats()
                .catch { e ->
                    // Handle error
                }
                .collect { result ->
                    result.onSuccess { stats ->
                        userStats = stats
                    }
                }
        }
    }

    fun setTheme(theme: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(theme)
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.signOut()
        }
    }
}