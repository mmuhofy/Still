package com.still.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import com.still.app.util.Constants

private val PillBg       = Color(0xFF18181F)
private val PillActive   = Color(0xFFB8A369)
private val PillInactive = Color(0x66FFFFFF)

sealed class BottomNavTab(
    val route: String,
    val icon: ImageVector,
    val label: String,
) {
    data object Notes : BottomNavTab(
        route = "notes_list",
        icon = Lucide.House,
        label = "Notlar",
    )
    data object Settings : BottomNavTab(
        route = "settings",
        icon = Lucide.Settings,
        label = "Ayarlar",
    )

    companion object {
        val tabs = listOf(Notes, Settings)
    }
}

@Composable
fun StillBottomNav(
    currentRoute: String?,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    // fillMaxWidth ensures the Box spans full screen width so Alignment.Center
    // places the pill at the horizontal center, not the left edge.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = Color.Black.copy(alpha = 0.6f),
                )
                .clip(RoundedCornerShape(50))
                // Outer dark shell
                .background(PillBg)
                // 1dp border via inner padding + re-clip
                .padding(1.dp)
                .clip(RoundedCornerShape(50))
                .background(PillBg.copy(alpha = 0.96f))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavTab.tabs.forEach { tab ->
                PillTabItem(
                    icon = tab.icon,
                    label = tab.label,
                    selected = currentRoute == tab.route,
                    onClick = { onTabSelected(tab) },
                )
            }
        }
    }
}

@Composable
private fun PillTabItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconTint by animateColorAsState(
        targetValue = if (selected) PillActive else PillInactive,
        animationSpec = tween(Constants.ANIMATION_DURATION_MS),
        label = "pill_tint",
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) PillActive.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(Constants.ANIMATION_DURATION_MS),
        label = "pill_bg",
    )
    val itemSize by animateDpAsState(
        targetValue = if (selected) 54.dp else 48.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_size",
    )

    Box(
        modifier = Modifier
            .size(itemSize)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(22.dp),
        )
    }
}