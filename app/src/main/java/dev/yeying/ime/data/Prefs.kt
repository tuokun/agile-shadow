package dev.yeying.ime.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.yeying.ime.ui.keyboard.KeyboardType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class Prefs(private val context: Context) {

    companion object {
        private val KEY_DEFAULT_KEYBOARD = intPreferencesKey("default_keyboard")
        private val KEY_KEY_VIBRATION = booleanPreferencesKey("key_vibration")
        private val KEY_KEY_SOUND = booleanPreferencesKey("key_sound")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    val defaultKeyboard: Flow<KeyboardType> = context.dataStore.data.map { prefs ->
        val ordinal = prefs[KEY_DEFAULT_KEYBOARD] ?: KeyboardType.QWERTY.ordinal
        KeyboardType.entries.getOrElse(ordinal) { KeyboardType.QWERTY }
    }

    val keyVibration: Flow<Boolean> = context.dataStore.data.map { it[KEY_KEY_VIBRATION] ?: true }
    val keySound: Flow<Boolean> = context.dataStore.data.map { it[KEY_KEY_SOUND] ?: false }
    val darkTheme: Flow<Boolean> = context.dataStore.data.map { it[KEY_DARK_THEME] ?: false }

    suspend fun setDefaultKeyboard(type: KeyboardType) {
        context.dataStore.edit { it[KEY_DEFAULT_KEYBOARD] = type.ordinal }
    }

    suspend fun setKeyVibration(enabled: Boolean) {
        context.dataStore.edit { it[KEY_KEY_VIBRATION] = enabled }
    }

    suspend fun setKeySound(enabled: Boolean) {
        context.dataStore.edit { it[KEY_KEY_SOUND] = enabled }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }
}
