package com.mobiletreeplantingapp.ui.screen.navigation.settings

import com.mobiletreeplantingapp.data.model.NotificationPreferences


data class NotificationSettingsState(
    val preferences: NotificationPreferences = NotificationPreferences(),
    val isLoading: Boolean = true,
    val error: String? = null
) 