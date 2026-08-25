package com.liuflow.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.liuflow.app.AppContainer
import com.liuflow.app.ui.focus.FocusViewModel
import com.liuflow.app.ui.history.HistoryViewModel
import com.liuflow.app.ui.me.MeViewModel
import com.liuflow.app.ui.heatmap.HeatmapViewModel
import com.liuflow.app.ui.running.RunningViewModel
import com.liuflow.app.ui.settings.SettingsViewModel
import com.liuflow.app.ui.stats.StatsViewModel
import com.liuflow.app.ui.weekly.WeeklyViewModel

/** Single factory exposing all ViewModels with their [AppContainer] dependencies. */
fun flowViewModelFactory(container: AppContainer) = viewModelFactory {
    initializer { FocusViewModel(container.settingsRepository, container.flowRepository, container.timer, container.sessionStateMachine) }
    initializer { HistoryViewModel(container.flowRepository) }
    initializer { StatsViewModel(container.flowRepository, container.settingsRepository) }
    initializer { HeatmapViewModel(container.flowRepository) }
    initializer { WeeklyViewModel(container.flowRepository) }
    initializer { MeViewModel(container.flowRepository, container.settingsRepository) }
    initializer { SettingsViewModel(container.settingsRepository) }
    initializer { RunningViewModel(container.timer, container.flowRepository, container.settingsRepository, container.sessionStateMachine) }
}
