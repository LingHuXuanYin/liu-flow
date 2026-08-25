package com.liuflow.app.ui.session

import com.liuflow.app.data.model.Category
import com.liuflow.app.timer.TimerController
import kotlinx.coroutines.flow.StateFlow

/**
 * End-to-end state machine for the Focus / Rest session.
 *
 * Three states matching the three core screens:
 *   - start   <-> [TimerController.Phase.IDLE]            (01 Focus screen)
 *   - running <-> [TimerController.Phase.RUNNING/PAUSED]  (02 Running screen)
 *   - rest    <-> [TimerController.Phase.RESTING]          (03 Rest screen)
 *
 * Transitions are synchronous w.r.t. the caller. The DB persistence half
 * (saveCompleted / saveAbandoned) lives in the view models — this class
 * only mutates the in-memory timer so the navigation call site can fire
 * the screen switch in the same frame.
 */
class SessionStateMachine(
    private val timer: TimerController,
) {

    val state: StateFlow<TimerController.State> = timer.state

    /**
     * start -> running. Called when the user taps the Focus ring.
     *  - [seconds] is clamped to >= 30 by [TimerController.prepareFocusSeconds].
     *  - The 1s tick coroutine is launched inside [TimerController.startFocus].
     */
    fun startFocus(task: String, category: Category?, seconds: Int) {
        timer.prepareFocusSeconds(task, category, seconds)
        timer.startFocus()
    }

    /**
     * running -> start (long press abandon).
     * Freezes the timer in place: phase becomes ABANDONED, remainingSeconds
     * is preserved. The call site is expected to fire popBackStack.
     */
    fun abandon() {
        timer.abandon()
    }

    /**
     * running -> rest (natural completion).
     * The 1s tick coroutine already drove phase=COMPLETED with
     * remainingSeconds=0. The call site must feed in the configured
     * rest duration (minutes) — we don't depend on settings here so the
     * class stays pure.
     */
    fun beginRest(minutes: Int) {
        timer.startRest(minutes)
    }

    /**
     * rest -> start (user taps Skip OR the rest countdown drains to 0).
     * Freezes the timer: phase becomes IDLE, remainingSeconds preserved.
     */
    fun skipRest() {
        timer.finishRest()
    }

    /** running <-> paused. */
    fun pause() = timer.pause()
    fun resume() = timer.resume()

    /**
     * Re-arm the running state with the same task / category / totalSeconds
     * without disturbing the configured duration. Used by the "restart"
     * affordance in the Running screen.
     */
    fun restart() {
        val s = timer.state.value
        timer.prepareFocus(s.task, s.category, s.totalSeconds / 60)
        timer.startFocus()
    }
}
