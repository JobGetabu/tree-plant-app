package com.mobiletreeplantingapp.data.repository

import com.mobiletreeplantingapp.data.model.NotificationPreferences
import kotlinx.coroutines.flow.Flow


interface PreferencesRepository {
    suspend fun getNotificationPreferences(): NotificationPreferences
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences)
    fun getNotificationPreferencesFlow(): Flow<NotificationPreferences>
} 