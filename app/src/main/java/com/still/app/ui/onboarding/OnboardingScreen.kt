package com.still.app.ui.onboarding

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.still.app.util.Constants

private const val TOTAL_PAGES = 3
private const val ANIM_DURATION = Constants.ANIMATION_DURATION_MS

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Navigate out once DataStore write is triggered
    LaunchedEffect(state.isCompleting) {
        if (state.isCompleting) onComplete()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))

            // Page indicator
            PageIndicator(
                totalPages = TOTAL_PAGES,
                currentPage = state.currentPage,
            )

            Spacer(Modifier.height(48.dp))

            // Page content — slides left on advance
            AnimatedContent(
                targetState = state.currentPage,
                transitionSpec = {
                    (slideInHorizontally(tween(ANIM_DURATION)) { it } + fadeIn(tween(ANIM_DURATION)))
                        .togetherWith(slideOutHorizontally(tween(ANIM_DURATION)) { -it } + fadeOut(tween(ANIM_DURATION)))
                },
                label = "onboarding_page",
                modifier = Modifier.weight(1f),
            ) { page ->
                when (page) {
                    0 -> PageTheme(
                        selectedTheme = state.selectedTheme,
                        onThemeSelected = { viewModel.onEvent(OnboardingEvent.ThemeSelected(it)) },
                    )
                    1 -> PageColorScheme(
                        selectedScheme = state.selectedColorScheme,
                        onSchemeSelected = { viewModel.onEvent(OnboardingEvent.ColorSchemeSelected(it)) },
                    )
                    2 -> PageFeatureMode(
                        aiEnabled = state.aiEnabled,
                        onModeSelected = { viewModel.onEvent(OnboardingEvent.AiModeSelected(it)) },
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // CTA button
            Button(
                onClick = {
                    if (state.currentPage < TOTAL_PAGES - 1) {
                        viewModel.onEvent(OnboardingEvent.NextPage)
                    } else {
                        viewModel.onEvent(OnboardingEvent.Complete)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                ),
            ) {
                Text(
                    text = if (state.currentPage < TOTAL_PAGES - 1) "Devam" else "Başla",
                    style = MaterialTheme.typography.labelLarge,
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

// ── Page 1 — Theme Selection ───────────────────────────────────────────────────

@Composable
private fun PageTheme(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "Temanı seç",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "İstediğin zaman ayarlardan değiştirebilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))

        // Theme options — only Calm Luxury active in Phase 1
        val themes = listOf(
            Triple("calm_luxury", "Calm Luxury", "Derin, altın vurgulu"),
            Triple("flat_minimal", "Flat Minimal", "Yakında"),
            Triple("liquid_glass", "Liquid Glass", "Yakında"),
        )

        themes.forEach { (key, name, desc) ->
            val isAvailable = key == "calm_luxury"
            ThemeOptionCard(
                name = name,
                description = desc,
                isSelected = selectedTheme == key,
                isEnabled = isAvailable,
                onClick = { if (isAvailable) onThemeSelected(key) },
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ThemeOptionCard(
    name: String,
    description: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    val alpha = if (isEnabled) 1f else 0.4f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.medium,
            )
            .background(MaterialTheme.colorScheme.surface)
            .clickable(enabled = isEnabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha),
            )
        }
        if (isSelected) {
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    }
}

// ── Page 2 — Dark / Light ─────────────────────────────────────────────────────

@Composable
private fun PageColorScheme(
    selectedScheme: String,
    onSchemeSelected: (String) -> Unit,
) {
    val systemIsDark = isSystemInDarkTheme()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "Görünüm",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Sisteminle senkronize ya da sabit bir mod seç.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))

        val schemes = listOf(
            Triple("auto", "Otomatik", "Sistem ayarını takip eder (şu an: ${if (systemIsDark) "koyu" else "açık"})"),
            Triple("dark", "Koyu", "Her zaman koyu mod"),
            Triple("light", "Açık", "Her zaman açık mod"),
        )

        schemes.forEach { (key, name, desc) ->
            ThemeOptionCard(
                name = name,
                description = desc,
                isSelected = selectedScheme == key,
                isEnabled = true,
                onClick = { onSchemeSelected(key) },
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
        modifier = Modifier.fillMaxSize(),
    ) {
        Text(
            text = "Nasıl başlamak istersin?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Her zaman ayarlardan değiştirebilirsin.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))

        ThemeOptionCard(
            name = "Sade başla",
            description = "Sadece not al. Sade, hızlı, odaklı.",
            isSelected = !aiEnabled,
            isEnabled = true,
            onClick = { onModeSelected(false) },
        )
        Spacer(Modifier.height(12.dp))
        ThemeOptionCard(
            name = "AI ile başla",
            description = "Yazarken AI önerileri göster. İnternet gerektirir.",
            isSelected = aiEnabled,
            isEnabled = true,
            onClick = { onModeSelected(true) },
        )
    }
}

// ── Page Indicator ─────────────────────────────────────────────────────────────

@Composable
private fun PageIndicator(
    totalPages: Int,
    currentPage: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(totalPages) { i ->
            val isActive = i == currentPage
            Box(
                modifier = Modifier
                    .height(3.dp)
                    .width(if (isActive) 24.dp else 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
            )
        }
    }
}