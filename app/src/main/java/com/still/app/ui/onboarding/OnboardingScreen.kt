package com.still.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.still.app.util.Constants

private const val TOTAL_PAGES  = 3
private const val ANIM_DURATION = Constants.ANIMATION_DURATION_MS

// Calm Luxury palette constants
private val Gold         = Color(0xFFB8A369)
private val GoldLight    = Color(0xFFD4B97A)
private val CardBg       = Color(0xFF1A1A24)
private val CardBgDim    = Color(0xFF14141C)
private val CardSelected = Color(0xFF1E1C14)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isCompleting) {
        if (state.isCompleting) onComplete()
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(52.dp))

            // Logo wordmark
            Text(
                text  = "still",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight    = FontWeight.Light,
                    letterSpacing = 6.sp,
                    color         = Gold.copy(alpha = 0.7f),
                ),
            )

            Spacer(Modifier.height(32.dp))

            // Page indicator
            PageIndicator(totalPages = TOTAL_PAGES, currentPage = state.currentPage)

            Spacer(Modifier.height(44.dp))

            // Page content
            AnimatedContent(
                targetState  = state.currentPage,
                transitionSpec = {
                    (slideInHorizontally(tween(ANIM_DURATION)) { it } + fadeIn(tween(ANIM_DURATION)))
                        .togetherWith(slideOutHorizontally(tween(ANIM_DURATION)) { -it } + fadeOut(tween(ANIM_DURATION)))
                },
                label    = "onboarding_page",
                modifier = Modifier.weight(1f),
            ) { page ->
                when (page) {
                    0 -> PageTheme(
                        selectedTheme    = state.selectedTheme,
                        onThemeSelected  = { viewModel.onEvent(OnboardingEvent.ThemeSelected(it)) },
                    )
                    1 -> PageColorScheme(
                        selectedScheme   = state.selectedColorScheme,
                        onSchemeSelected = { viewModel.onEvent(OnboardingEvent.ColorSchemeSelected(it)) },
                    )
                    2 -> PageFeatureMode(
                        aiEnabled       = state.aiEnabled,
                        onModeSelected  = { viewModel.onEvent(OnboardingEvent.AiModeSelected(it)) },
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // CTA — pill shaped, gold gradient
            val ctaLabel = when (state.currentPage) {
                0    -> "Temayı seç"
                1    -> "Görünümü onayla"
                else -> "Still'e başla"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Gold, GoldLight)))
                    .clickable {
                        if (state.currentPage < TOTAL_PAGES - 1) {
                            viewModel.onEvent(OnboardingEvent.NextPage)
                        } else {
                            viewModel.onEvent(OnboardingEvent.Complete)
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text  = ctaLabel,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color      = Color(0xFF1A1208),
                    ),
                )
            }

            Spacer(Modifier.height(44.dp))
        }
    }
}

// ── Page 1 — Theme ────────────────────────────────────────────────────────────

@Composable
private fun PageTheme(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.fillMaxSize(),
    ) {
        PageTitle(
            title    = "Nasıl görünsün?",
            subtitle = "Yazmak istediğin atmosferi seç.\nİstediğin zaman değiştirebilirsin.",
        )

        Spacer(Modifier.height(32.dp))

        ThemeCard(
            key         = "calm_luxury",
            name        = "Calm Luxury",
            tagline     = "Derin koyu, altın vurgular — sessiz güç hissi.",
            preview     = { CalmLuxuryPreview() },
            isSelected  = selectedTheme == "calm_luxury",
            isAvailable = true,
            onClick     = { onThemeSelected("calm_luxury") },
        )
        Spacer(Modifier.height(12.dp))
        ThemeCard(
            key         = "flat_minimal",
            name        = "Flat Minimal",
            tagline     = "Saf beyaz, sıfır dikkat dağıtıcı.",
            preview     = { FlatMinimalPreview() },
            isSelected  = selectedTheme == "flat_minimal",
            isAvailable = false,
            onClick     = {},
        )
        Spacer(Modifier.height(12.dp))
        ThemeCard(
            key         = "liquid_glass",
            name        = "Liquid Glass",
            tagline     = "Işık ve derinlik — kayan cam efekti.",
            preview     = { LiquidGlassPreview() },
            isSelected  = selectedTheme == "liquid_glass",
            isAvailable = false,
            onClick     = {},
        )
    }
}

// ── Theme card ─────────────────────────────────────────────────────────────────

@Composable
private fun ThemeCard(
    key: String,
    name: String,
    tagline: String,
    preview: @Composable () -> Unit,
    isSelected: Boolean,
    isAvailable: Boolean,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) Gold else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(ANIM_DURATION),
        label         = "border_$key",
    )
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) CardSelected else CardBg,
        animationSpec = tween(ANIM_DURATION),
        label         = "bg_$key",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(enabled = isAvailable, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Mini visual preview
        Box(
            modifier = Modifier
                .size(width = 56.dp, height = 44.dp)
                .clip(RoundedCornerShape(8.dp))
        ) { preview() }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text  = name,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = if (isAvailable) 1f else 0.35f
                        ),
                    ),
                )
                if (!isAvailable) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Yakında",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Gold.copy(alpha = 0.5f),
                        ),
                    )
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text  = tagline,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isAvailable) 0.7f else 0.25f
                    ),
                ),
            )
        }

        if (isSelected) {
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Gold, CircleShape),
            )
        }
    }
}

// ── Theme mini previews ────────────────────────────────────────────────────────

@Composable
private fun CalmLuxuryPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF13121A))
    ) {
        // Simulated text lines
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(Modifier.fillMaxWidth(0.7f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFFB8A369).copy(alpha = 0.8f)))
            Box(Modifier.fillMaxWidth(0.9f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.25f)))
            Box(Modifier.fillMaxWidth(0.75f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.15f)))
            Box(Modifier.fillMaxWidth(0.85f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.15f)))
        }
        // Gold accent bottom line
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.4f)
                .height(1.dp)
                .background(Gold.copy(alpha = 0.4f))
        )
    }
}

@Composable
private fun FlatMinimalPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F5))
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(Modifier.fillMaxWidth(0.7f).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1A1A1A).copy(alpha = 0.8f)))
            Box(Modifier.fillMaxWidth(0.9f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1A1A1A).copy(alpha = 0.2f)))
            Box(Modifier.fillMaxWidth(0.75f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1A1A1A).copy(alpha = 0.12f)))
            Box(Modifier.fillMaxWidth(0.85f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF1A1A1A).copy(alpha = 0.12f)))
        }
    }
}

@Composable
private fun LiquidGlassPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1A1F35), Color(0xFF0D1526))
                )
            )
    ) {
        // Frosted glass card
        Box(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
        )
        Column(
            modifier = Modifier.padding(start = 8.dp, top = 10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Box(Modifier.fillMaxWidth(0.5f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.5f)))
            Box(Modifier.fillMaxWidth(0.7f).height(2.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.2f)))
        }
    }
}

// ── Page 2 — Color Scheme ─────────────────────────────────────────────────────

@Composable
private fun PageColorScheme(
    selectedScheme: String,
    onSchemeSelected: (String) -> Unit,
) {
    val systemIsDark = isSystemInDarkTheme()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.fillMaxSize(),
    ) {
        PageTitle(
            title    = "Aydınlık mı, karanlık mı?",
            subtitle = "Gözün ne ister? Sistem ayarını\ntakip edebilir ya da sabit seçebilirsin.",
        )

        Spacer(Modifier.height(32.dp))

        listOf(
            Triple("auto",  "Otomatik", "Şu an: ${if (systemIsDark) "koyu mod" else "açık mod"}"),
            Triple("dark",  "Koyu",     "Her zaman gece havası"),
            Triple("light", "Açık",     "Her zaman gün ışığı"),
        ).forEach { (key, name, desc) ->
            OptionCard(
                name       = name,
                description = desc,
                isSelected  = selectedScheme == key,
                isEnabled   = true,
                onClick     = { onSchemeSelected(key) },
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ── Page 3 — Feature Mode ─────────────────────────────────────────────────────

@Composable
private fun PageFeatureMode(
    aiEnabled: Boolean,
    onModeSelected: (Boolean) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.fillMaxSize(),
    ) {
        PageTitle(
            title    = "Nasıl yazarsın?",
            subtitle = "Sade bir başlangıç mı, yoksa\nAI destekli akış mı?",
        )

        Spacer(Modifier.height(32.dp))

        OptionCard(
            name        = "Sadece ben",
            description = "Düşüncelerine dokunulmadan, temiz bir sayfa.",
            isSelected  = !aiEnabled,
            isEnabled   = true,
            onClick     = { onModeSelected(false) },
        )
        Spacer(Modifier.height(12.dp))
        OptionCard(
            name        = "Ben + AI",
            description = "Yazarken öneriler gelir, kabul etmek sana kalmış.",
            isSelected  = aiEnabled,
            isEnabled   = true,
            onClick     = { onModeSelected(true) },
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text  = "Her iki seçenek de ayarlardan değiştirilebilir.",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            ),
            textAlign = TextAlign.Center,
        )
    }
}

// ── Option Card (generic — used in Page 2 & 3) ────────────────────────────────

@Composable
private fun OptionCard(
    name: String,
    description: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) Gold else Color.White.copy(alpha = 0.08f),
        animationSpec = tween(ANIM_DURATION),
        label         = "opt_border_$name",
    )
    val bgColor by animateColorAsState(
        targetValue   = if (isSelected) CardSelected else CardBg,
        animationSpec = tween(ANIM_DURATION),
        label         = "opt_bg_$name",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = name,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (isEnabled) 1f else 0.35f
                    ),
                ),
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text  = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isEnabled) 0.65f else 0.25f
                    ),
                ),
            )
        }
        if (isSelected) {
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Gold, CircleShape),
            )
        }
    }
}

// ── Page Title ─────────────────────────────────────────────────────────────────

@Composable
private fun PageTitle(title: String, subtitle: String) {
    Text(
        text      = title,
        style     = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.SemiBold,
        ),
        color     = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(10.dp))
    Text(
        text      = subtitle,
        style     = MaterialTheme.typography.bodyMedium,
        color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
    )
}

// ── Page Indicator ─────────────────────────────────────────────────────────────

@Composable
private fun PageIndicator(totalPages: Int, currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        repeat(totalPages) { i ->
            val isActive = i == currentPage
            val width by animateDpAsState(
                targetValue   = if (isActive) 28.dp else 6.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label         = "indicator_$i",
            )
            val color by animateColorAsState(
                targetValue   = if (isActive) Gold else Color.White.copy(alpha = 0.2f),
                animationSpec = tween(ANIM_DURATION),
                label         = "indicator_color_$i",
            )
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(width)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
    }
}