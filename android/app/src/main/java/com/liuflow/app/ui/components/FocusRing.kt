package com.liuflow.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liuflow.app.ui.theme.LocalFlowColors

/**
 * The signature component: a circular progress ring with optional content
 * in the middle. Matches the prototype's 280dp ring at 12dp stroke.
 *
 * When [pressProgress] is non-null and > 0, an outer "sweep" arc is drawn
 * on top of the regular progress ring. The sweep is slightly thicker and
 * uses [pressColor] (defaults to [Color] red, mapped from the error token
 * at the call site) so the long-press-to-stop feedback is clearly visible
 * against the primary-colored timer ring.
 */
@Composable
fun FocusRing(
    progress: Float,                 // 0f..1f
    modifier: Modifier = Modifier,
    diameter: Dp = 280.dp,
    strokeWidth: Dp = 12.dp,
    trackColor: Color? = null,
    progressColor: Color? = null,
    trackAlpha: Float = 1f,
    pressProgress: Float? = null,    // 0f..1f, drawn as outer sweep
    pressColor: Color? = null,
    content: @Composable () -> Unit = {},
) {
    val colors = LocalFlowColors.current
    val target = progress.coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 600),
        label = "ringProgress",
    )

    val track = trackColor ?: colors.surfaceContainerHigh
    val progressStroke = progressColor ?: colors.primary
    val pressStroke = pressColor ?: colors.error

    val sweep = pressProgress?.coerceIn(0f, 1f)?.takeIf { it > 0f }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(diameter),
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
            val topLeft = Offset(inset, inset)
            // Background ring
            drawArc(
                color = track.copy(alpha = trackAlpha),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            )
            // Progress ring
            drawArc(
                color = progressStroke,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            )
            // Press sweep: drawn on top, slightly outside so it doesn't
            // visually fight the primary progress ring.
            if (sweep != null) {
                val pressWidth = strokeWidth.toPx() + 6.dp.toPx()
                val pressInset = pressWidth / 2f
                val pressArcSize = Size(
                    this.size.width - pressWidth,
                    this.size.height - pressWidth,
                )
                val pressTopLeft = Offset(pressInset, pressInset)
                drawArc(
                    color = pressStroke,
                    startAngle = -90f,
                    sweepAngle = 360f * sweep,
                    useCenter = false,
                    topLeft = pressTopLeft,
                    size = pressArcSize,
                    style = Stroke(width = pressWidth, cap = StrokeCap.Round),
                )
            }
        }
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

/** Convenience center content: status label + time + (optional) hint. */
@Composable
fun RingCenter(
    statusLabel: String,
    timeText: String,
    hint: String? = null,
) {
    val colors = LocalFlowColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = statusLabel,
            color = colors.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
        )
        Text(
            text = timeText,
            color = colors.onSurface,
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Light,
                fontSize = 64.sp,
            ),
            textAlign = TextAlign.Center,
        )
        if (hint != null) {
            Text(
                text = hint,
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
