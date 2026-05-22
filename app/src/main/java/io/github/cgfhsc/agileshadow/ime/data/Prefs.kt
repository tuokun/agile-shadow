package io.github.cgfhsc.agileshadow.ime.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.github.cgfhsc.agileshadow.ime.ui.keyboard.KeyboardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class Prefs(private val context: Context) {

    companion object {
        private val KEY_DEFAULT_KEYBOARD = intPreferencesKey("default_keyboard")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_FOLLOW_SYSTEM_THEME = booleanPreferencesKey("follow_system_theme")
        private val KEY_EXCLUDE_FROM_RECENTS = booleanPreferencesKey("exclude_from_recents")
    }

    val defaultKeyboard: Flow<KeyboardType> = context.dataStore.data.map { prefs ->
        val ordinal = prefs[KEY_DEFAULT_KEYBOARD] ?: KeyboardType.QWERTY.ordinal
        KeyboardType.entries.getOrElse(ordinal) { KeyboardType.QWERTY }
    }

    val darkTheme: Flow<Boolean> = context.dataStore.data.map { it[KEY_DARK_THEME] ?: false }
    val followSystemTheme: Flow<Boolean> = context.dataStore.data.map { it[KEY_FOLLOW_SYSTEM_THEME] ?: false }
    val excludeFromRecents: Flow<Boolean> = context.dataStore.data.map { it[KEY_EXCLUDE_FROM_RECENTS] ?: false }

    suspend fun setDefaultKeyboard(type: KeyboardType) {
        context.dataStore.edit { it[KEY_DEFAULT_KEYBOARD] = type.ordinal }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    suspend fun setFollowSystemTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_FOLLOW_SYSTEM_THEME] = enabled }
    }

    suspend fun setExcludeFromRecents(enabled: Boolean) {
        context.dataStore.edit { it[KEY_EXCLUDE_FROM_RECENTS] = enabled }
    }
}
