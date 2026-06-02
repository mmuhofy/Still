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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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

// Pill palette — dark glass surface with gold-tinted active state
private val PillBg       = Color(0xFF18181F)         // deep dark, slightly blue-tinted
private val PillBorder   = Color(0x28FFFFFF)          // subtle white rim
private val PillActive   = Color(0xFFB8A369)          // mat altın — Calm Luxury accent
private val PillInactive = Color(0x66FFFFFF)          // muted white

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
    // Outer wrapper — sits above system nav area, centered horizontally
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Floating pill container
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 24.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f),
                )
                .clip(RoundedCornerShape(50))
                .background(PillBg)
                // Thin border via padding + inner clip — no extra composable needed
                .padding(1.dp)
                .clip(RoundedCornerShape(50))
                .background(PillBg.copy(alpha = 0.95f))
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BottomNavTab.tabs.forEach { tab ->
                val selected = currentRoute == tab.route
                PillTabItem(
                    icon = tab.icon,
                    label = tab.label,
                    selected = selected,
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
    val bgAlpha by animateColorAsState(
        targetValue = if (selected) PillActive.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(Constants.ANIMATION_DURATION_MS),
        label = "pill_bg",
    )
    val itemSize by animateDpAsState(
        targetValue = if (selected) 52.dp else 48.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_size",
    )

    Box(
        modifier = Modifier
            .size(itemSize)
            .clip(CircleShape)
            .background(bgAlpha)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // no ripple — Calm Luxury
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