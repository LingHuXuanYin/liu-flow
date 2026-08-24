package com.liuflow.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.data.model.DarkMode
import com.liuflow.app.data.model.FlowTheme
import com.liuflow.app.data.prefs.SettingsRepository
import com.liuflow.app.data.prefs.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repo: SettingsRepository,
) : ViewModel() {

    val state: StateFlow<UserSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    fun setTheme(t: FlowTheme) = viewModelScope.launch { repo.setTheme(t) }
    fun setDarkMode(m: DarkMode) = viewModelScope.launch { repo.setDarkMode(m) }
    fun setDefaultFocus(min: Int) = viewModelScope.launch { repo.setDefaultFocusMinutes(min) }
    fun setRest(min: Int) = viewModelScope.launch { repo.setRestMinutes(min) }
    fun setDailyTarget(n: Int) = viewModelScope.launch { repo.setDailyTarget(n) }
    fun setSound(b: Boolean) = viewModelScope.launch { repo.setSoundEnabled(b) }
    fun setVibrate(b: Boolean) = viewModelScope.launch { repo.setVibrateEnabled(b) }
}
