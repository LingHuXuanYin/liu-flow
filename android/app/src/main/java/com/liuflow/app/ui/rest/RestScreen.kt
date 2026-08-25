package com.liuflow.app.ui.rest

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.liuflow.app.R
import com.liuflow.app.timer.TimerController
import com.liuflow.app.ui.theme.LocalFlowColors
import com.liuflow.app.util.TimeFormat

/**
 * Rest screen, redesigned to match the prototype:
 *  - Top app bar with "REST TIME" label + title + 跳过 button
 *  - Centered gradient breath circle (primary-container → secondary-container)
 *    with a 5s scale animation, a leaf icon, and the remaining time
 *  - 3-line hint text
 *  - Bottom summary card ("已完成一次专注 / 写作 · 25 分钟")
 */
@Composable
fun RestScreen(
    timerState: TimerController.State,
    onFinish: () -> Unit,
) {
    val colors = LocalFlowColors.current

    // 5-second breath cycle for the gradient circle, matches prototype.
    val transition = rememberInfiniteTransition(label = "rest-breath")
    val breathScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rest-breath-scale",
    )

    val categoryLabelRes: Int? = timerState.category?.labelRes

    Surface(color = colors.background, modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
            // Top app bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.rest_label_focus).uppercase(),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                    )
                    Text(
                        text = stringResource(R.string.rest_title),
                        color = colors.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { onFinish() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.rest_skip),
                        color = colors.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }

            // Center area: gradient breath circle + hint
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(256.dp)
                            .scale(breathScale)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        colors.primaryContainer,
                                        colors.secondaryContainer,
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.material3.Icon(
                                Icons.Filled.LocalFlorist,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(32.dp),
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = TimeFormat.mmss(timerState.remainingSeconds),
                                color = colors.onPrimaryContainer,
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Light,
                                ),
                            )
                        }
                    }
                    Spacer(Modifier.height(48.dp))
                    Text(
                        text = stringResource(R.string.rest_hint_lines),
                        color = colors.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }

            // Bottom summary card
            SummaryCard(
                categoryLabelRes = categoryLabelRes,
                durationMinutes = if (timerState.totalSeconds > 0) timerState.totalSeconds / 60 else 0,
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SummaryCard(categoryLabelRes: Int?, durationMinutes: Int) {
    val colors = LocalFlowColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.tertiary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = colors.tertiary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.rest_summary_title),
                color = colors.onSurface,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            )
            val catName = categoryLabelRes?.let { stringResource(it) } ?: "未分类"
            Text(
                text = "$catName · $durationMinutes 分钟",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
