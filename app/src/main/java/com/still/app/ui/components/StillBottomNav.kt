package com.still.app.ui.components

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings
import com.still.app.util.Constants

private val PillBg       = Color(0xFF18181F)
private val PillActive   = Color(0xFFB8A369)
private val PillInactive = Color(0x55FFFFFF)

// Gold gradient for the center FAB button
private val FabGradient = Brush.radialGradient(
    colors = listOf(Color(0xFFD4B97A), Color(0xFFB8A369)),
)
private val FabShadowColor = Color(0xFFB8A369)

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
    onNewNote: () -> Unit,           // center FAB action
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Pill container — Notes | [FAB] | Settings
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 32.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color.Black.copy(alpha = 0.7f),
                    spotColor  = Color.Black.copy(alpha = 0.7f),
                )
                .clip(RoundedCornerShape(50))
                .background(PillBg)
                .padding(1.dp)
                .clip(RoundedCornerShape(50))
                .background(PillBg.copy(alpha = 0.96f))
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left tab — Notes
            PillTabItem(
                icon  = BottomNavTab.Notes.icon,
                label = BottomNavTab.Notes.label,
                selected = currentRoute == BottomNavTab.Notes.route,
                onClick  = { onTabSelected(BottomNavTab.Notes) },
            )

            // Center FAB — new note
            CenterFabItem(onClick = onNewNote)

            // Right tab — Settings
            PillTabItem(
                icon  = BottomNavTab.Settings.icon,
                label = BottomNavTab.Settings.label,
                selected = currentRoute == BottomNavTab.Settings.route,
                onClick  = { onTabSelected(BottomNavTab.Settings) },
            )
        }
    }
}

// ── Center FAB ────────────────────────────────────────────────────────────────

@Composable
private fun CenterFabItem(onClick: () -> Unit) {
    val scale = remember { Animatable(1f) }

    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            // Lifts the FAB slightly above the pill
            .offset(y = (-6).dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = FabShadowColor.copy(alpha = 0.5f),
                spotColor   = FabShadowColor.copy(alpha = 0.5f),
            )
            .size(52.dp)
            .clip(CircleShape)
            .background(FabGradient)
            .scale(scale.value)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    onClick()
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Lucide.Plus,
            contentDescription = "Yeni not",
            tint = Color(0xFF1A1A22),   // dark icon on gold bg
            modifier = Modifier.size(24.dp),
        )
    }
}

// ── Tab Item ──────────────────────────────────────────────────────────────────

@Composable
private fun PillTabItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val iconTint by animateColorAsState(
        targetValue  = if (selected) PillActive else PillInactive,
        animationSpec = tween(Constants.ANIMATION_DURATION_MS),
        label = "pill_tint",
    )
    val bgColor by animateColorAsState(
        targetValue  = if (selected) PillActive.copy(alpha = 0.16f) else Color.Transparent,
        animationSpec = tween(Constants.ANIMATION_DURATION_MS),
        label = "pill_bg",
    )
    val itemSize by animateDpAsState(
        targetValue  = if (selected) 54.dp else 48.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pill_size",
    )
    val floatOffset by animateDpAsState(
        targetValue  = if (selected) (-2).dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pill_float",
    )

    val scale = remember { Animatable(1f) }

    LaunchedEffect(selected) {
        if (selected) {
            scale.animateTo(0.82f, spring(stiffness = Spring.StiffnessHigh))
            scale.animateTo(
                targetValue  = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium,
                ),
            )
        }
    }

    Box(
        modifier = Modifier
            .size(itemSize)
            .offset { IntOffset(0, floatOffset.roundToPx()) }
            .clip(CircleShape)
            .background(bgColor)
            .scale(scale.value)
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