package com.still.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Color Schemes ────────────────────────────────────────────────────────────

private val CalmLuxuryDark = darkColorScheme(
    primary = CalmGold,
    onPrimary = DeepDark,
    primaryContainer = CalmGoldSubtle,
    onPrimaryContainer = CalmGold,

    secondary = CalmGoldDim,
    onSecondary = DeepDark,
    secondaryContainer = SurfaceVariantDark,
    onSecondaryContainer = OnSurfaceVariantDark,

    background = DeepDark,
    onBackground = OnBackgroundDark,

    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,

    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,

    error = ErrorDark,
    onError = OnErrorDark,
)

private val CalmLuxuryLight = lightColorScheme(
    primary = CalmGold,
    onPrimary = BackgroundLight,
    primaryContainer = CalmGoldSubtle,
    onPrimaryContainer = CalmGoldDim,

    secondary = CalmGoldDim,
    onSecondary = BackgroundLight,
    secondaryContainer = SurfaceVariantLight,
    onSecondaryContainer = OnSurfaceVariantLight,

    background = BackgroundLight,
    onBackground = OnBackgroundLight,

    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,

    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,

    error = ErrorLight,
    onError = OnErrorLight,
)

// ─── Theme Entry Point ────────────────────────────────────────────────────────

@Composable
fun StillTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CalmLuxuryDark else CalmLuxuryLight

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = StillTypography,
        shapes = StillShapes,
        content = content,
    )
}