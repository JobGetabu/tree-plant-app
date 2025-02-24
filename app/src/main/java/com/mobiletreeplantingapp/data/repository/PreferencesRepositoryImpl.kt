package com.mobiletreeplantingapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.mobiletreeplantingapp.data.model.NotificationPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    override suspend fun getNotificationPreferences(): NotificationPreferences {
        return context.dataStore.data.map { preferences ->
            NotificationPreferences(
                wateringEnabled = preferences[WATERING_ENABLED] ?: true,
                pruningEnabled = preferences[PRUNING_ENABLED] ?: true,
                fertilizingEnabled = preferences[FERTILIZING_ENABLED] ?: true,
                inspectionEnabled = preferences[INSPECTION_ENABLED] ?: true,
                growthCheckEnabled = preferences[GROWTH_CHECK_ENABLED] ?: true
            )
        }.first()
    }

    override suspend fun updateNotificationPreferences(preferences: NotificationPreferences) {
        context.dataStore.edit { prefs ->
            prefs[WATERING_ENABLED] = preferences.wateringEnabled
            prefs[PRUNING_ENABLED] = preferences.pruningEnabled
            prefs[FERTILIZING_ENABLED] = preferences.fertilizingEnabled
            prefs[INSPECTION_ENABLED] = preferences.inspectionEnabled
            prefs[GROWTH_CHECK_ENABLED] = preferences.growthCheckEnabled
        }
    }

    companion object {
        private val WATERING_ENABLED = booleanPreferencesKey("watering_enabled")
        private val PRUNING_ENABLED = booleanPreferencesKey("pruning_enabled")
        private val FERTILIZING_ENABLED = booleanPreferencesKey("fertilizing_enabled")
        private val INSPECTION_ENABLED = booleanPreferencesKey("inspection_enabled")
        private val GROWTH_CHECK_ENABLED = booleanPreferencesKey("growth_check_enabled")
    }
} 