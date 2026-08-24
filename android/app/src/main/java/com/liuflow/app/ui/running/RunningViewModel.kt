package com.liuflow.app.ui.running

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.data.prefs.SettingsRepository
import com.liuflow.app.data.prefs.UserSettings
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.timer.TimerController
import com.liuflow.app.util.ChimePlayer
import com.liuflow.app.util.VibrateUtil
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RunningViewModel(
    private val timer: TimerController,
    private val repo: FlowRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    val state = timer.state
    val settingsState: StateFlow<UserSettings> = settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    fun pause() = timer.pause()
    fun resume() = timer.resume()
    fun abandon(context: Context, onComplete: () -> Unit) {
        val s = timer.state.value
        if (s.startedAt == 0L) { onComplete(); return }
        val actualMin = ((s.totalSeconds - s.remainingSeconds).coerceAtLeast(0) + 59) / 60
        viewModelScope.launch {
            repo.saveAbandoned(
                task = s.task,
                category = s.category,
                plannedMinutes = s.totalSeconds / 60,
                actualMinutes = actualMin,
                startedAt = s.startedAt,
                endedAt = System.currentTimeMillis(),
            )
            timer.reset()
            onComplete()
        }
    }

    fun restart() {
        val s = timer.state.value
        timer.prepareFocus(s.task, s.category, s.totalSeconds / 60)
        timer.startFocus()
    }

    /** Returns true if natural completion happened and a session was saved. */
    fun checkCompletionAndSave(context: Context, onCompleted: () -> Unit) {
        val s = timer.state.value
        if (s.phase != TimerController.Phase.COMPLETED) return
        if (s.startedAt == 0L) { onCompleted(); return }
        val settingsNow = settingsState.value
        if (settingsNow.vibrateEnabled) VibrateUtil.pattern(context)
        if (settingsNow.soundEnabled) ChimePlayer.play(context.applicationContext)
        viewModelScope.launch {
            repo.saveCompleted(
                task = s.task,
                category = s.category,
                plannedMinutes = s.totalSeconds / 60,
                actualMinutes = s.totalSeconds / 60,
                startedAt = s.startedAt,
                endedAt = System.currentTimeMillis(),
            )
            onCompleted()
        }
    }

    fun startRestThen(onRest: () -> Unit) {
        val rest = settingsState.value.restMinutes
        timer.startRest(rest)
        onRest()
    }

    /** Called by the navigation graph when the user leaves the Rest screen. */
    fun finishRest() {
        timer.finishRest()
    }
}
