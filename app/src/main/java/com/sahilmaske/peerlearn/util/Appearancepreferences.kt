package com.sahilmaske.peerlearn.util


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Requires: implementation "androidx.datastore:datastore-preferences:1.1.1"
private val Context.appearanceDataStore by preferencesDataStore(name = "appearance_prefs")

enum class ThemeMode {
    LIGHT, DARK, SYSTEM;

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.find { it.name == key } ?: SYSTEM
    }
}

private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

/**
 * Single source of truth for the App Appearance screen.
 * Default is SYSTEM (follows device theme) until the user picks otherwise.
 */
class AppearancePreferences(private val context: Context) {

    val themeMode: Flow<ThemeMode> =
        context.appearanceDataStore.data.map { prefs ->
            ThemeMode.fromKey(prefs[THEME_MODE_KEY])
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.appearanceDataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }
}