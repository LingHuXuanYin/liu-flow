package com.liuflow.app.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.data.model.Category
import com.liuflow.app.data.prefs.UserSettings
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.data.prefs.SettingsRepository
import com.liuflow.app.timer.TimerController
import com.liuflow.app.ui.session.SessionStateMachine
import com.liuflow.app.util.DateUtils
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * State holder for the main Focus screen. Owns the [TimerController] via the
 * shared [com.liuflow.app.AppContainer] and exposes a UI-friendly state.
 */
class FocusViewModel(
    private val settings: SettingsRepository,
    private val repo: FlowRepository,
    private val timer: TimerController,
    private val stateMachine: SessionStateMachine,
) : ViewModel() {

    private val _taskInput = MutableStateFlow("")
    val taskInput: StateFlow<String> = _taskInput.asStateFlow()

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    private val _duration = MutableStateFlow(25f)
    val duration: StateFlow<Float> = _duration.asStateFlow()

    val timerState = timer.state
    val settingsState: StateFlow<UserSettings> = settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    fun setTask(value: String) { _taskInput.value = value }
    fun setCategory(value: Category?) { _category.value = value }
    fun setDuration(minutes: Float) { _duration.value = minutes.coerceIn(0.5f, 90f) }

    fun prepareAndStart(onStart: () -> Unit) {
        val seconds = (_duration.value * 60f).roundToInt().coerceAtLeast(30)
        stateMachine.startFocus(_taskInput.value, _category.value, seconds)
        onStart()
    }
}

/** Stub extension so the imports in screens compile when this file is read alone. */
@Suppress("unused")
private fun unused(@Suppress("UNUSED_PARAMETER") repo: FlowRepository) = Unit
