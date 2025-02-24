package com.mobiletreeplantingapp.data.repository

import com.mobiletreeplantingapp.data.model.NotificationPreferences


interface PreferencesRepository {
    suspend fun getNotificationPreferences(): NotificationPreferences
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences)
} 