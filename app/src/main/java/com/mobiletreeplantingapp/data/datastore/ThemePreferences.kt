package com.mobiletreeplantingapp.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

class ThemePreferences(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .catch { exception ->
            // Log any errors
            Log.e("ThemePreferences", "Error reading theme preferences", exception)
            emit(emptyPreferences())
        }
        .map { preferences ->
            val themeName = preferences[themeKey] ?: ThemeMode.SYSTEM.name
            Log.d("ThemePreferences", "Current theme: $themeName")
            ThemeMode.valueOf(themeName)
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        Log.d("ThemePreferences", "Setting theme to: ${mode.name}")
        context.dataStore.edit { preferences ->
            preferences[themeKey] = mode.name
        }
    }
} 