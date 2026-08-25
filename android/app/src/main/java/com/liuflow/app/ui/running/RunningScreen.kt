package com.liuflow.app.ui.running

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * Immersive focus screen, redesigned to match the prototype:
 *  - Always-dark surface (regardless of theme) for focus-mode immersion
 *  - Two blurred breath circles behind the ring (3s cycle, 1.5s offset)
 *  - Top bar: pulsing dot + "正在专注" + close-X (abandon)
 *  - Subtitle row: "写作 · 25 分钟" (category · duration)
 *  - Bottom action buttons: pause/abandon (RUNNING), resume/restart/abandon (PAUSED)
 *  - The ring itself still supports tap (pause/resume) and long-press 3s (abandon),
 *    so the visible controls are affordances, not the only path.
 */
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

    val pressProgress = remember { mutableFloatStateOf(0f) }
    var pressing by remember { mutableStateOf(false) }
    var wasLongPress by remember { mutableStateOf(false) }
    var showAbandonConfirm by remember { mutableStateOf(false) }

    // Breath animation for the two glow halos behind the ring.
    val breathTransition = rememberInfiniteTransition(label = "running-breath")
    val breathScale1 by breathTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath1",
    )
    val breathScale2 by breathTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(1500),
        ),
        label = "breath2",
    )

    // Pulse for the "正在专注" status dot
    val pulseAlpha by breathTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

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

    LaunchedEffect(state.phase) {
        if (state.phase == TimerController.Phase.COMPLETED) {
            viewModel.checkCompletionAndSave(context, onCompleted)
        }
        if (state.phase != TimerController.Phase.RUNNING &&
            state.phase != TimerController.Phase.PAUSED
        ) {
            pressing = false
            pressProgress.floatValue = 0f
        }
    }

    BackHandler(enabled = state.phase == TimerController.Phase.RUNNING) { /* swallow */ }

    // Always-dark background, regardless of theme — focus mode is intentionally dim.
    Surface(color = Color(0xFF1C1B1F), modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            // Top bar: pulsing dot + 正在专注 + close X
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(pulseAlpha)
                            .background(colors.primary, CircleShape),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.running_focusing).uppercase(),
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    )
                }
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.abandon(context, onAbandoned) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = stringResource(R.string.action_abandon),
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Subtitle row: 写作 · 25 分钟
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = buildSubtitle(state),
                    color = Color.White.copy(alpha = 0.4f),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = state.task.ifBlank { "未命名专注" },
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
            }

            // Ring with breath glows behind it
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                // Two breath halos. Modifier.blur is API 31+ for the visual
                // effect; on older devices the blur is a no-op and the
                // circles render as soft alpha disks.
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .scale(breathScale1)
                        .alpha(0.20f)
                        .blur(64.dp)
                        .background(colors.primary, CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .scale(breathScale2)
                        .alpha(0.12f)
                        .blur(48.dp)
                        .background(colors.primary, CircleShape),
                )
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .pointerInput(state.phase) {
                            if (state.phase != TimerController.Phase.RUNNING &&
                                state.phase != TimerController.Phase.PAUSED
                            ) {
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
                                        else -> {}
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

            // Bottom action buttons (visual affordance for ring gestures)
            when (state.phase) {
                TimerController.Phase.RUNNING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        BottomActionButton(
                            icon = Icons.Filled.Pause,
                            label = stringResource(R.string.action_pause),
                            onClick = { viewModel.pause() },
                        )
                        BottomActionButton(
                            icon = Icons.Filled.Stop,
                            label = stringResource(R.string.action_abandon),
                            onClick = { showAbandonConfirm = true },
                        )
                    }
                }
                TimerController.Phase.PAUSED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        BottomActionButton(
                            icon = Icons.Filled.PlayArrow,
                            label = stringResource(R.string.action_resume),
                            onClick = { viewModel.resume() },
                        )
                        BottomActionButton(
                            icon = Icons.Filled.Refresh,
                            label = stringResource(R.string.running_paused_restart),
                            onClick = { viewModel.restart() },
                        )
                        BottomActionButton(
                            icon = Icons.Filled.Stop,
                            label = stringResource(R.string.action_abandon),
                            onClick = { showAbandonConfirm = true },
                        )
                    }
                }
                else -> {}
            }

            // Bottom hint text (subtle, full-width centered)
            Box(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.running_fullscreen_hint),
                    color = Color.White.copy(alpha = 0.3f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }

    if (showAbandonConfirm) {
        AlertDialog(
            onDismissRequest = { showAbandonConfirm = false },
            title = { Text(stringResource(R.string.running_abandon_confirm)) },
            text = { Text(stringResource(R.string.running_abandon_note)) },
            confirmButton = {
                TextButton(onClick = {
                    showAbandonConfirm = false
                    viewModel.abandon(context, onAbandoned)
                }) {
                    Text(stringResource(R.string.action_confirm), color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAbandonConfirm = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/** "写作 · 25 分钟" style subtitle, gracefully handles missing category/duration. */
@Composable
private fun buildSubtitle(state: TimerController.State): String {
    val parts = mutableListOf<String>()
    state.category?.let { c -> parts.add(stringResource(c.labelRes)) }
    if (state.totalSeconds > 0) parts.add("${state.totalSeconds / 60} 分钟")
    return parts.joinToString(" · ")
}

@Composable
private fun BottomActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
