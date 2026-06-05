package com.still.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.still.app.R

// Google Fonts provider
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

// Inter — clean, neutral, designed for screens. Not tied to system font weight.
private val InterFont = GoogleFont("Inter")

val InterFamily = FontFamily(
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFont, fontProvider = provider, weight = FontWeight.Bold),
)

// Lora — serif, calm, luxury feel for titles
private val LoraFont = GoogleFont("Lora")

val LoraFamily = FontFamily(
    Font(googleFont = LoraFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = LoraFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = LoraFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = LoraFont, fontProvider = provider, weight = FontWeight.Bold),
)

val StillTypography = Typography(

    // ── Display ────────────────────────────────────────────────────────────────
    displayLarge = TextStyle(
        fontFamily   = LoraFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 57.sp,
        lineHeight   = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontFamily   = LoraFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 45.sp,
        lineHeight   = 52.sp,
        letterSpacing = 0.sp,
    ),
    displaySmall = TextStyle(
        fontFamily   = LoraFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 36.sp,
        lineHeight   = 44.sp,
        letterSpacing = 0.sp,
    ),

    // ── Headline — note titles ─────────────────────────────────────────────────
    headlineLarge = TextStyle(
        fontFamily   = LoraFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 32.sp,
        lineHeight   = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily   = LoraFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 28.sp,
        lineHeight   = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily   = LoraFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 24.sp,
        lineHeight   = 32.sp,
        letterSpacing = 0.sp,
    ),

    // ── Title ─────────────────────────────────────────────────────────────────
    titleLarge = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.SemiBold,
        fontSize     = 22.sp,
        lineHeight   = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 16.sp,
        lineHeight   = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ── Body — editor content, previews ───────────────────────────────────────
    // Inter Light/Normal — soft, readable, never system-font-weight dependent
    bodyLarge = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Light,   // lighter than system default — easier on eyes
        fontSize     = 16.sp,
        lineHeight   = 27.sp,
        letterSpacing = 0.3.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 14.sp,
        lineHeight   = 22.sp,
        letterSpacing = 0.2.sp,
    ),
    bodySmall = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Normal,
        fontSize     = 12.sp,
        lineHeight   = 18.sp,
        letterSpacing = 0.3.sp,
    ),

    // ── Label ─────────────────────────────────────────────────────────────────
    labelLarge = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 14.sp,
        lineHeight   = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 12.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily   = InterFamily,
        fontWeight   = FontWeight.Medium,
        fontSize     = 11.sp,
        lineHeight   = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)