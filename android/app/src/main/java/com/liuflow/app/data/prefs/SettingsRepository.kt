package com.liuflow.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.liuflow.app.data.model.DarkMode
import com.liuflow.app.data.model.FlowTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "flow_settings")

/** User-tunable app settings persisted in DataStore. */
class SettingsRepository(private val context: Context) {

    private val ds = context.dataStore

    val settings: Flow<UserSettings> = ds.data.map { p -> p.toUserSettings() }

    suspend fun setTheme(theme: FlowTheme) =
        ds.edit { it[Keys.THEME] = theme.id }

    suspend fun setDarkMode(mode: DarkMode) =
        ds.edit { it[Keys.DARK_MODE] = mode.name }

    suspend fun setDefaultFocusMinutes(min: Int) =
        ds.edit { it[Keys.DEFAULT_FOCUS_MIN] = min }

    suspend fun setRestMinutes(min: Int) =
        ds.edit { it[Keys.REST_MIN] = min }

    suspend fun setDailyTarget(target: Int) =
        ds.edit { it[Keys.DAILY_TARGET] = target }

    suspend fun setSoundEnabled(enabled: Boolean) =
        ds.edit { it[Keys.SOUND] = enabled }

    suspend fun setVibrateEnabled(enabled: Boolean) =
        ds.edit { it[Keys.VIBRATE] = enabled }

    suspend fun clearAll() = ds.edit { it.clear() }

    private fun Preferences.toUserSettings(): UserSettings = UserSettings(
        theme = FlowTheme.fromId(this[Keys.THEME]),
        darkMode = runCatching { DarkMode.valueOf(this[Keys.DARK_MODE] ?: DarkMode.SYSTEM.name) }
            .getOrDefault(DarkMode.SYSTEM),
        defaultFocusMinutes = this[Keys.DEFAULT_FOCUS_MIN] ?: 25,
        restMinutes = this[Keys.REST_MIN] ?: 5,
        dailyTarget = this[Keys.DAILY_TARGET] ?: 4,
        soundEnabled = this[Keys.SOUND] ?: true,
        vibrateEnabled = this[Keys.VIBRATE] ?: true,
    )

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DARK_MODE = stringPreferencesKey("dark_mode")
        val DEFAULT_FOCUS_MIN = intPreferencesKey("default_focus_min")
        val REST_MIN = intPreferencesKey("rest_min")
        val DAILY_TARGET = intPreferencesKey("daily_target")
        val SOUND = booleanPreferencesKey("sound")
        val VIBRATE = booleanPreferencesKey("vibrate")
    }
}

data class UserSettings(
    val theme: FlowTheme = FlowTheme.CLASSIC,
    val darkMode: DarkMode = DarkMode.SYSTEM,
    val defaultFocusMinutes: Int = 25,
    val restMinutes: Int = 5,
    val dailyTarget: Int = 4,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
)
