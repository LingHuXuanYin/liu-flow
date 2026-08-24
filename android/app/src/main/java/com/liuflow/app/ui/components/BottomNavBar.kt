package com.liuflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.liuflow.app.R
import com.liuflow.app.ui.theme.LocalFlowColors

/**
 * 80dp MD3 NavigationBar with the active item rendered as a "Pill" shape,
 * matching the prototype (`bg-primary-container` rounded-full).
 *
 * Layout strategy for edge-to-edge:
 *  - The outer Box's background fills the full width down to the screen
 *    edge, so the bar's surface color extends behind the system gesture
 *    area (Material 3 pattern).
 *  - The inner Row applies `navigationBarsPadding` so the icons and labels
 *    sit above the system navigation bar.
 */
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (route: String) -> Unit,
) {
    val colors = LocalFlowColors.current
    val items = listOf(
        NavItem("focus", Icons.Filled.WaterDrop, R.string.nav_focus),
        NavItem("history", Icons.Filled.AccessTime, R.string.nav_record),
        NavItem("me", Icons.Filled.Person, R.string.nav_me),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(80.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val active = currentRoute == item.route
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigate(item.route) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (active) colors.primaryContainer else colors.surfaceContainer)
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = if (active) colors.primary else colors.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Text(
                        text = androidx.compose.ui.res.stringResource(item.labelRes),
                        color = if (active) colors.onSurface else colors.onSurfaceVariant,
                        style = if (active) MaterialTheme.typography.labelSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        else MaterialTheme.typography.labelSmall,
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(0.dp))
}

private data class NavItem(
    val route: String,
    val icon: ImageVector,
    val labelRes: Int,
)
