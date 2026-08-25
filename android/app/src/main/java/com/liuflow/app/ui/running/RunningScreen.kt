package com.liuflow.app.ui.running

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuflow.app.R
import com.liuflow.app.timer.TimerController
import com.liuflow.app.ui.components.FocusRing
import com.liuflow.app.ui.components.RingCenter
import com.liuflow.app.ui.theme.LocalFlowColors
import com.liuflow.app.util.TimeFormat
import kotlinx.coroutines.delay

/** Long-press duration required to trigger abandon. */
private const val ABANDON_HOLD_MS: Long = 3_000L
/** Anything released before this is treated as a tap. */
private const val TAP_MAX_MS: Long = 300L

@Composable
fun RunningScreen(
    viewModel: RunningViewModel,
    onCompleted: () -> Unit,
    onAbandoned: () -> Unit,
    onRest: () -> Unit,
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = LocalFlowColors.current

    // Press-tracking state for the big-ring gesture.
    val pressProgress = remember { mutableFloatStateOf(0f) }
    var pressing by remember { mutableStateOf(false) }
    var wasLongPress by remember { mutableStateOf(false) }

    // Drive the sweep every frame while the user is holding. At 3s the
    // session is abandoned immediately — the sweep itself is the
    // confirmation, mirroring the iOS "hold to delete" pattern.
    LaunchedEffect(pressing) {
        if (pressing) {
            wasLongPress = false
            pressProgress.floatValue = 0f
            val start = System.currentTimeMillis()
            while (pressing) {
                val elapsed = System.currentTimeMillis() - start
                val p = (elapsed.toFloat() / ABANDON_HOLD_MS.toFloat()).coerceIn(0f, 1f)
                pressProgress.floatValue = p
                if (p >= 1f) {
                    wasLongPress = true
                    viewModel.abandon(context, onAbandoned)
                    pressing = false
                    break
                }
                delay(16L)
            }
        } else {
            pressProgress.floatValue = 0f
        }
    }

    // Reset transient press state when the timer phase flips.
    LaunchedEffect(state.phase) {
        if (state.phase == TimerController.Phase.COMPLETED) {
            viewModel.checkCompletionAndSave(context, onCompleted)
        }
        if (state.phase != TimerController.Phase.RUNNING &&
            state.phase != TimerController.Phase.PAUSED) {
            pressing = false
            pressProgress.floatValue = 0f
        }
    }

    // Disable back during a live focus session.
    BackHandler(enabled = state.phase == TimerController.Phase.RUNNING) { /* swallow */ }

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.running_label_focus).uppercase(),
                    color = colors.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.task.ifBlank { "未命名专注" },
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }

            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .pointerInput(state.phase) {
                            // Only handle gestures while the timer is actively
                            // running or paused.
                            if (state.phase != TimerController.Phase.RUNNING &&
                                state.phase != TimerController.Phase.PAUSED) {
                                return@pointerInput
                            }
                            awaitEachGesture {
                                val down = awaitFirstDown(requireUnconsumed = false)
                                down.consume()
                                val downTime = System.currentTimeMillis()
                                pressing = true
                                val released = waitForUpOrCancellation()
                                val elapsed = System.currentTimeMillis() - downTime
                                pressing = false
                                if (released != null && !wasLongPress && elapsed < TAP_MAX_MS) {
                                    when (state.phase) {
                                        TimerController.Phase.RUNNING -> viewModel.pause()
                                        TimerController.Phase.PAUSED -> viewModel.resume()
                                        else -> { /* ignore */ }
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    FocusRing(
                        progress = state.progress,
                        diameter = 320.dp,
                        strokeWidth = 12.dp,
                        pressProgress = pressProgress.floatValue,
                    ) {
                        RingCenter(
                            statusLabel = when (state.phase) {
                                TimerController.Phase.PAUSED -> stringResource(R.string.focus_label_status_paused)
                                else -> stringResource(R.string.focus_label_status_running)
                            },
                            timeText = TimeFormat.mmss(state.remainingSeconds),
                            hint = null,
                        )
                    }
                }
            }

            // Subtle gesture hint; sits inside a rounded chip so the text is
            // readable on any background. When paused, a "重新开始" button
            // is shown below the chip to give the user a way to reset the
            // timer without abandoning the session.
            val hintText = when (state.phase) {
                TimerController.Phase.PAUSED -> "点击继续 · 长按 3 秒停止"
                else -> "点击暂停 · 长按 3 秒停止"
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = hintText,
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                if (state.phase == TimerController.Phase.PAUSED) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(colors.surfaceContainer)
                            .clickable { viewModel.restart() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = null,
                            tint = colors.onSurface,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = stringResource(R.string.running_restart_button),
                            color = colors.onSurface,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }
            }
        }
    }
}
