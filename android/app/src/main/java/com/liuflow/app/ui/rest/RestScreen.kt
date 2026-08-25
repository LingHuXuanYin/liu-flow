package com.liuflow.app.ui.rest

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liuflow.app.R
import com.liuflow.app.timer.TimerController
import com.liuflow.app.ui.theme.LocalFlowColors
import com.liuflow.app.util.TimeFormat

/**
 * Rest screen — appears after a focus session completes.
 *
 * Layout (per user 2026-08-26 mock):
 *  - Top:    "REST TIME" small caps + "休息一下" large title + right "跳过"
 *  - Middle: breathing leaf icon (3s loop, scale + alpha) + mm:ss countdown
 *  - Body:   3-line hint ("站起来走一走 / 看 5 米外的绿植 / 让眼睛休息一下")
 *  - Bottom: summary card — "已完成一次专注" + "{category} · {minutes} 分钟"
 *
 * "跳过" is wired to the same onFinish callback (it just means "end rest now").
 * The natural rest-completion path (RESTING phase + remainingSeconds=0)
 * also fires onFinish via LaunchedEffect — the same end-state, different
 * trigger.
 */
@Composable
fun RestScreen(
    timerState: TimerController.State,
    onFinish: () -> Unit,
) {
    val colors = LocalFlowColors.current

    // Breathing animation: scale 0.88 -> 1.08, alpha 0.65 -> 1.0, 3s loop, ease in-out.
    val transition = rememberInfiniteTransition(label = "rest-leaf")
    val scale by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rest-leaf-scale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rest-leaf-alpha",
    )

    // Natural rest completion: phase=RESTING + remainingSeconds=0 -> onFinish.
    // User tap "跳过" -> same onFinish callback. Both paths converge here.
    LaunchedEffect(timerState.phase, timerState.remainingSeconds) {
        if (timerState.phase == TimerController.Phase.RESTING && timerState.remainingSeconds == 0) {
            onFinish()
        }
    }

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            // Top bar: small caps "REST TIME" + title "休息一下" + right "跳过"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.rest_label_focus),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.rest_title),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                Text(
                    text = stringResource(R.string.rest_skip),
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clickable { onFinish() }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }

            // Center: breathing leaf + countdown
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.Eco,
                        contentDescription = null,
                        tint = colors.onSurface,
                        modifier = Modifier
                            .size(56.dp)
                            .scale(scale)
                            .alpha(alpha),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = TimeFormat.mmss(timerState.remainingSeconds),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Light,
                        ),
                    )
                }
            }

            // Body: 3-line hint
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RestHintLines()
            }

            // Bottom: summary card — "已完成一次专注" + "{category} · {minutes} 分钟"
            val categoryName = timerState.category?.let { stringResource(it.labelRes) }
            val plannedMinutes = (timerState.totalSeconds + 30) / 60  // round up; >= 30s -> 1m
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.onSurface,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.size(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.rest_summary_title),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (categoryName != null) "$categoryName · $plannedMinutes 分钟" else "$plannedMinutes 分钟",
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun RestHintLines() {
    val colors = LocalFlowColors.current
    val lines = stringResource(R.string.rest_hint_lines).split("\n")
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        lines.forEach { line ->
            if (line.isNotBlank()) {
                Text(
                    text = line,
                    color = colors.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}