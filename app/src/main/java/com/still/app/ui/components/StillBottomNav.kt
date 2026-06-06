package com.still.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings

private val PillBg         = Color(0xFF16161F)
private val PillBorder     = Color(0x18FFFFFF)
private val ActiveBg       = Color(0xFF1E1C14)
private val ActiveTint     = Color(0xFFB8A369)
private val InactiveTint   = Color(0x60FFFFFF)
private val FabGradient    = Brush.linearGradient(listOf(Color(0xFFD4B97A), Color(0xFFB8A369)))
private val FabShadow      = Color(0xFFB8A369)

private const val ANIM_MS = 280

sealed class BottomNavTab(
    val route: String,
    val icon: ImageVector,
    val label: String,
) {
    data object Notes : BottomNavTab("notes_list", Lucide.House, "Notlar")
    data object Settings : BottomNavTab("settings", Lucide.Settings, "Ayarlar")

    companion object {
        val tabs = listOf(Notes, Settings)
    }
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
                    elevation    = 24.dp,
                    shape        = RoundedCornerShape(50),
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor    = Color.Black.copy(alpha = 0.6f),
                )
                .clip(RoundedCornerShape(50))
                .background(PillBg)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            // Left — Notes
            NavTabItem(
                icon     = BottomNavTab.Notes.icon,
                label    = BottomNavTab.Notes.label,
                selected = currentRoute == BottomNavTab.Notes.route,
                onClick  = { onTabSelected(BottomNavTab.Notes) },
            )

            // Center — FAB
            CenterFab(onClick = onNewNote)

            // Right — Settings
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
    val bgAlpha by animateFloatAsState(
        targetValue   = if (selected) 1f else 0f,
        animationSpec = tween(ANIM_MS),
        label         = "tab_bg_$label",
    )
    val iconTint by androidx.compose.animation.animateColorAsState(
        targetValue   = if (selected) ActiveTint else InactiveTint,
        animationSpec = tween(ANIM_MS),
        label         = "tab_tint_$label",
    )
    val scale by animateFloatAsState(
        targetValue   = if (selected) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "tab_scale_$label",
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(ActiveBg.copy(alpha = bgAlpha))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .scale(scale),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = iconTint,
                modifier           = Modifier.size(20.dp),
            )
            // Label slides in horizontally when selected
            AnimatedVisibility(
                visible = selected,
                enter   = expandHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium,
                    )
                ) + fadeIn(tween(ANIM_MS)),
                exit    = shrinkHorizontally(tween(ANIM_MS / 2)) + fadeOut(tween(ANIM_MS / 2)),
            ) {
                Row {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text       = label,
                        color      = ActiveTint,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines   = 1,
                    )
                }
            }
        }
    }
}

// ── Center FAB ────────────────────────────────────────────────────────────────

@Composable
private fun CenterFab(onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label         = "fab_scale",
    )

    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .offset(y = (-5).dp)
            .shadow(
                elevation    = 14.dp,
                shape        = CircleShape,
                ambientColor = FabShadow.copy(alpha = 0.45f),
                spotColor    = FabShadow.copy(alpha = 0.45f),
            )
            .size(50.dp)
            .clip(CircleShape)
            .background(FabGradient)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = Lucide.Plus,
            contentDescription = "Yeni not",
            tint               = Color(0xFF1A1208),
            modifier           = Modifier.size(22.dp),
        )
    }
}