package com.still.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings

private val PillBg       = Color(0xFF14131C)
private val ActiveTint   = Color(0xFFB8A369)
private val InactiveTint = Color(0x50FFFFFF)
private val ActiveBg     = Color(0xFF211F14)
private val DotColor     = Color(0xFFB8A369)
private val FabGradient  = Brush.linearGradient(listOf(Color(0xFFD4B97A), Color(0xFFAF9355)))
private val FabIconColor = Color(0xFF1A1208)
private val FabGlow      = Color(0xFFB8A369)

sealed class BottomNavTab(val route: String, val icon: ImageVector, val label: String) {
    data object Notes    : BottomNavTab("notes_list", Lucide.House,    "Notlar")
    data object Settings : BottomNavTab("settings",   Lucide.Settings, "Ayarlar")
    companion object { val tabs = listOf(Notes, Settings) }
}

@Composable
fun StillBottomNav(
    currentRoute: String?,
    onTabSelected: (BottomNavTab) -> Unit,
    onNewNote: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation    = 40.dp,
                    shape        = RoundedCornerShape(50),
                    ambientColor = Color.Black.copy(alpha = 0.8f),
                    spotColor    = Color.Black.copy(alpha = 0.8f),
                )
                .clip(RoundedCornerShape(50))
                .background(PillBg)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            NavTabItem(
                icon     = BottomNavTab.Notes.icon,
                label    = BottomNavTab.Notes.label,
                selected = currentRoute == BottomNavTab.Notes.route,
                onClick  = { onTabSelected(BottomNavTab.Notes) },
            )
            CenterFab(onClick = onNewNote)
            NavTabItem(
                icon     = BottomNavTab.Settings.icon,
                label    = BottomNavTab.Settings.label,
                selected = currentRoute == BottomNavTab.Settings.route,
                onClick  = { onTabSelected(BottomNavTab.Settings) },
            )
        }
    }
}

// ── Tab Item ──────────────────────────────────────────────────────────────────

@Composable
private fun NavTabItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.82f else 1f,
        animationSpec = if (isPressed) tween(80)
                        else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "press_$label",
    )
    val iconTint by animateColorAsState(
        targetValue   = if (selected) ActiveTint else InactiveTint,
        animationSpec = tween(220),
        label         = "tint_$label",
    )
    val bgAlpha by animateFloatAsState(
        targetValue   = if (selected) 1f else 0f,
        animationSpec = tween(220),
        label         = "bg_$label",
    )
    val dotAlpha by animateFloatAsState(
        targetValue   = if (selected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "dot_$label",
    )
    val iconOffset by animateFloatAsState(
        targetValue   = if (selected) -2f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "offset_$label",
    )
    // Icon bg circle — "filled" feel when active
    val iconBgAlpha by animateFloatAsState(
        targetValue   = if (selected) 0.18f else 0f,
        animationSpec = tween(220),
        label         = "icon_bg_$label",
    )
    val iconScale by animateFloatAsState(
        targetValue   = if (selected) 1.12f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "icon_scale_$label",
    )

    Box(
        modifier = Modifier
            .size(54.dp)
            .scale(pressScale)
            .clip(CircleShape)
            .background(ActiveBg.copy(alpha = bgAlpha))
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // Icon with filled-feel bg circle
            Box(
                modifier         = Modifier
                    .size(32.dp)
                    .graphicsLayer { translationY = iconOffset }
                    .clip(CircleShape)
                    .background(ActiveTint.copy(alpha = iconBgAlpha)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector        = icon,
                    contentDescription = label,
                    tint               = iconTint,
                    modifier           = Modifier
                        .size(20.dp)
                        .scale(iconScale),
                )
            }
            // Active dot
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(DotColor.copy(alpha = dotAlpha)),
            )
        }
    }
}

// ── Center FAB ────────────────────────────────────────────────────────────────

@Composable
private fun CenterFab(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue   = if (isPressed) 0.88f else 1f,
        animationSpec = if (isPressed) tween(80)
                        else spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label         = "fab_scale",
    )
    val glowAlpha by animateFloatAsState(
        targetValue   = if (isPressed) 0.2f else 0.4f,
        animationSpec = tween(150),
        label         = "fab_glow",
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .offset(y = (-4).dp)
            .shadow(
                elevation    = 16.dp,
                shape        = CircleShape,
                ambientColor = FabGlow.copy(alpha = glowAlpha),
                spotColor    = FabGlow.copy(alpha = glowAlpha),
            )
            .size(52.dp)
            .scale(pressScale)
            .clip(CircleShape)
            .background(FabGradient)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = Lucide.Plus,
            contentDescription = "Yeni not",
            tint               = FabIconColor,
            modifier           = Modifier.size(22.dp),
        )
    }
}