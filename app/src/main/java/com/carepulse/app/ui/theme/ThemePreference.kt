package com.carepulse.app.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

private val Context.themeDataStore by preferencesDataStore(name = "carepulse_theme")
private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")

class ThemePreference(private val context: Context) {
    val themeMode: Flow<ThemeMode> = context.themeDataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.themeDataStore.edit { it[THEME_MODE_KEY] = mode.name }
    }
}
