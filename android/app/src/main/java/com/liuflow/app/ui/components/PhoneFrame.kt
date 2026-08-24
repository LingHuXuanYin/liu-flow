package com.liuflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.liuflow.app.ui.theme.LocalFlowColors

/**
 * Wraps a single screen in a Pixel-class device frame (412 × 892 dp with
 * a 44dp outer radius), a 24dp center-punch camera, and 3-button nav.
 *
 * Intended for use in the design-preview surface (when [preview] is true).
 * On real devices, the system provides these elements; we render the
 * full-screen content directly.
 */
@Composable
fun PhoneFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = LocalFlowColors.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(width = 412.dp, height = 892.dp)
                    .background(Color.Black, RoundedCornerShape(44.dp))
                    .padding(8.dp)
                    .border(2.dp, Color(0xFF1F1F1F), RoundedCornerShape(40.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(36.dp))
                        .background(colors.background),
                ) {
                    // Top center punch-hole camera
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                            .size(28.dp)
                            .background(Color.Black, CircleShape),
                    )
                    // Bottom 3-button nav (mock)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(36.dp)
                            .background(colors.surfaceContainer),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 60.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            NavGlyph()
                            NavGlyph()
                            NavGlyph()
                        }
                    }
                    Box(
                        modifier = Modifier.fillMaxSize().padding(36.dp),
                    ) { content() }
                }
            }
        }
    }
}

@Composable
private fun NavGlyph() {
    Box(
        modifier = Modifier
            .size(width = 60.dp, height = 4.dp)
            .background(Color(0xFF3C3C3C), RoundedCornerShape(2.dp)),
    )
}
