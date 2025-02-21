package com.mobiletreeplantingapp.ui.screen.navigation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobiletreeplantingapp.data.datastore.ThemeMode
import com.mobiletreeplantingapp.data.datastore.ThemePreferences
import com.mobiletreeplantingapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    var currentTheme by mutableStateOf(ThemeMode.SYSTEM)
        private set

    init {
        viewModelScope.launch {
            themePreferences.themeMode.collectLatest { theme ->
                currentTheme = theme
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