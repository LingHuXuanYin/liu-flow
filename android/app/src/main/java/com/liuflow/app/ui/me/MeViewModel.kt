package com.liuflow.app.ui.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.BuildConfig
import com.liuflow.app.data.prefs.SettingsRepository
import com.liuflow.app.data.prefs.UserSettings
import com.liuflow.app.data.repository.FlowRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeViewModel(
    private val repo: FlowRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    val version: String get() = BuildConfig.VERSION_NAME

    val settingsState: StateFlow<UserSettings> = settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    fun clearAllData() = viewModelScope.launch { repo.deleteAll() }
}
