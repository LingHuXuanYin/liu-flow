package com.liuflow.app.ui.running

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.liuflow.app.data.prefs.SettingsRepository
import com.liuflow.app.data.prefs.UserSettings
import com.liuflow.app.data.repository.FlowRepository
import com.liuflow.app.timer.TimerController
import com.liuflow.app.ui.session.SessionStateMachine
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
    private val stateMachine: SessionStateMachine,
) : ViewModel() {

    val state = timer.state
    val settingsState: StateFlow<UserSettings> = settings.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    fun pause() = stateMachine.pause()
    fun resume() = stateMachine.resume()
    /**
     * User long-presses to give up. State machine: running -> start.
     *  - Persist the partial session to DB (fire-and-forget).
     *  - Synchronously freeze the timer: phase=ABANDONED, remainingSeconds
     *    preserved (so the source screen doesn't show 00:00 for a frame).
     *  - Synchronously invoke [onComplete] which the nav layer uses to
     *    pop back to Focus.
     */
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
        }
        stateMachine.abandon()
        onComplete()
    }

    fun restart() {
        stateMachine.restart()
    }

    /**
     * User completes a session naturally. State machine: running -> rest.
     *  - Trigger chime/vibrate feedback synchronously (user-perceived).
     *  - Persist to DB asynchronously.
     *  - Synchronously invoke [onCompleted] which the nav layer uses to
     *    kick off the rest countdown + navigate to the Rest screen.
     * The nav layer's onCompleted calls [startRestThen] which switches
     * the timer into RESTING mode and navigates in the same frame.
     */
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
        }
        onCompleted()
    }

    fun startRestThen(onRest: () -> Unit) {
        val rest = settingsState.value.restMinutes
        stateMachine.beginRest(rest)
        onRest()
    }

    /** Called by the navigation graph when the user leaves the Rest screen. */
    fun finishRest() {
        stateMachine.skipRest()
    }
}
