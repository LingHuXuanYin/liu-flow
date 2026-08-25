package com.liuflow.app.timer

import com.liuflow.app.data.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the focus / rest countdown. UI observes [state].
 *
 * State machine:
 *  IDLE  --start(focus)-->  RUNNING
 *  RUNNING --pause()-->     PAUSED
 *  PAUSED  --resume()-->    RUNNING
 *  RUNNING/PAUSED --complete()--> COMPLETED (one-shot)
 *  RUNNING/PAUSED --abandon()-->  ABANDONED (one-shot)
 *  COMPLETED/ABANDONED --startRest()-->  RESTING
 *  RESTING --finish()-->   IDLE
 */
class TimerController(
    private val scope: CoroutineScope,
) {

    enum class Phase { IDLE, RUNNING, PAUSED, RESTING, COMPLETED, ABANDONED }

    data class State(
        val phase: Phase = Phase.IDLE,
        val mode: Mode = Mode.FOCUS,
        val totalSeconds: Int = 25 * 60,
        val remainingSeconds: Int = 25 * 60,
        val task: String = "",
        val category: Category? = null,
        val startedAt: Long = 0L,
        val pausedAt: Long = 0L,
    ) {
        val progress: Float
            get() = if (totalSeconds == 0) 0f
            else 1f - (remainingSeconds.coerceAtLeast(0).toFloat() / totalSeconds)
    }

    enum class Mode { FOCUS, REST }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    private var tickJob: Job? = null

    fun prepareFocus(task: String, category: Category?, minutes: Int) {
        cancelTick()
        _state.value = State(
            phase = Phase.IDLE,
            mode = Mode.FOCUS,
            totalSeconds = minutes * 60,
            remainingSeconds = minutes * 60,
            task = task,
            category = category,
        )
    }

    fun prepareFocusSeconds(task: String, category: Category?, seconds: Int) {
        cancelTick()
        _state.value = State(
            phase = Phase.IDLE,
            mode = Mode.FOCUS,
            totalSeconds = seconds.coerceAtLeast(30),
            remainingSeconds = seconds.coerceAtLeast(30),
            task = task,
            category = category,
        )
    }

    fun startFocus() {
        val s = _state.value
        if (s.phase != Phase.IDLE && s.phase != Phase.PAUSED) return
        val now = System.currentTimeMillis()
        _state.update {
            it.copy(
                phase = Phase.RUNNING,
                startedAt = if (it.startedAt == 0L) now else it.startedAt,
                pausedAt = 0L,
            )
        }
        launchTick()
    }

    fun pause() {
        val s = _state.value
        if (s.phase != Phase.RUNNING) return
        cancelTick()
        _state.update { it.copy(phase = Phase.PAUSED, pausedAt = System.currentTimeMillis()) }
    }

    fun resume() {
        val s = _state.value
        if (s.phase != Phase.PAUSED) return
        _state.update { it.copy(phase = Phase.RUNNING, pausedAt = 0L) }
        launchTick()
    }

    /** Natural completion: total elapsed == totalSeconds. */
    fun complete() {
        cancelTick()
        _state.update { it.copy(phase = Phase.COMPLETED, remainingSeconds = 0) }
    }

    /** User abandons. */
    fun abandon() {
        cancelTick()
        _state.update { it.copy(phase = Phase.ABANDONED) }
    }

    fun startRest(minutes: Int) {
        cancelTick()
        _state.value = State(
            phase = Phase.RESTING,
            mode = Mode.REST,
            totalSeconds = minutes * 60,
            remainingSeconds = minutes * 60,
        )
        launchTick()
    }

    /**
     * Leave rest mode but keep the displayed countdown frozen.
     *  - cancels the 1s tick so the number stops moving
     *  - sets phase=IDLE so the Rest screen / Running screen render correctly
     *  - keeps [State.totalSeconds] / [State.remainingSeconds] / [State.task] /
     *    [State.category] so a downstream "back to Focus" jump doesn't show
     *    00:00 / no-task on the source screen for a frame.
     */
    fun finishRest() {
        cancelTick()
        _state.update { it.copy(phase = Phase.IDLE) }
    }

    private fun launchTick() {
        cancelTick()
        tickJob = scope.launch {
            while (_state.value.phase == Phase.RUNNING || _state.value.phase == Phase.RESTING) {
                delay(1000L)
                val cur = _state.value
                if (cur.remainingSeconds <= 1) {
                    _state.update { it.copy(remainingSeconds = 0) }
                    if (cur.phase == Phase.RUNNING) complete()
                    return@launch
                }
                _state.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
        }
    }

    private fun cancelTick() {
        tickJob?.cancel()
        tickJob = null
    }

    fun reset() {
        cancelTick()
        _state.value = State()
    }
}
