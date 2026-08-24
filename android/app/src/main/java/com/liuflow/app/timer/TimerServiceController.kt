package com.liuflow.app.timer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Watches the [TimerController] state and starts/stops [FocusTimerService]
 * based on transitions. Lives for the lifetime of the application process
 * (created from [com.liuflow.app.AppContainer]).
 *
 * Only acts on phase *transitions*, not on every emission, to avoid
 * redundant startService / stopService calls.
 */
class TimerServiceController(
    private val context: Context,
    private val timer: TimerController,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastPhase: TimerController.Phase? = null

    init {
        scope.launch {
            timer.state.collect { state ->
                val needsService = state.phase in SERVICE_PHASES
                val wasService = lastPhase in SERVICE_PHASES
                when {
                    needsService && !wasService -> startService()
                    !needsService && wasService -> stopService()
                }
                lastPhase = state.phase
            }
        }
    }

    private fun startService() {
        val intent = Intent(context, FocusTimerService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    private fun stopService() {
        context.stopService(Intent(context, FocusTimerService::class.java))
    }

    private companion object {
        val SERVICE_PHASES = setOf(
            TimerController.Phase.RUNNING,
            TimerController.Phase.PAUSED,
            TimerController.Phase.RESTING,
        )
    }
}
