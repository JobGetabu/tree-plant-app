package com.mobiletreeplantingapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_preferences")

class OnboardingPreferences(private val context: Context) {
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    val isOnboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[onboardingCompletedKey] ?: false
        }

    suspend fun setOnboardingCompleted() {
        context.onboardingDataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = true
        }
    }
} 