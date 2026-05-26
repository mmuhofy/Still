package com.still.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val appVersion = remember {
        try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: "—"
        } catch (_: Exception) { "—" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ayarlar",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {

            // ── Appearance ────────────────────────────────────────────────────
            SettingsSectionHeader("Görünüm")

            // Color scheme selector
            SettingsItem(label = "Tema modu") {
                ColorSchemeSelector(
                    current = uiState.colorScheme,
                    onSelect = { viewModel.onEvent(SettingsEvent.SetColorScheme(it)) },
                )
            }

            SettingsDivider()

            // ── Writing (Phase 2 placeholders — all OFF) ──────────────────────
            SettingsSectionHeader("Yazma")

            SettingsSwitchItem(
                label = "AI tamamlama",
                description = "Yazarken öneri göster",
                checked = uiState.aiEnabled,
                onCheckedChange = { viewModel.onEvent(SettingsEvent.SetAiEnabled(it)) },
            )

            SettingsSwitchItem(
                label = "Odak modu",
                description = "Yalnızca metni göster",
                checked = uiState.focusModeEnabled,
                onCheckedChange = { viewModel.onEvent(SettingsEvent.SetFocusMode(it)) },
            )

            SettingsSwitchItem(
                label = "Daktilo modu",
                description = "Aktif satırı ortada tut",
                checked = uiState.typewriterModeEnabled,
                onCheckedChange = { viewModel.onEvent(SettingsEvent.SetTypewriterMode(it)) },
            )

            SettingsDivider()

            // ── About ─────────────────────────────────────────────────────────
            SettingsSectionHeader("Hakkında")

            SettingsReadOnlyItem(label = "Sürüm", value = appVersion)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
    )
}

// ── Generic row with end-slot ─────────────────────────────────────────────────

@Composable
private fun SettingsItem(
    label: String,
    description: String? = null,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        content()
    }
}

// ── Switch row ────────────────────────────────────────────────────────────────

@Composable
private fun SettingsSwitchItem(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsItem(label = label, description = description) {
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ── Read-only value row ───────────────────────────────────────────────────────

@Composable
private fun SettingsReadOnlyItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Color scheme segmented button ─────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorSchemeSelector(
    current: AppColorScheme,
    onSelect: (AppColorScheme) -> Unit,
) {
    val options = listOf(
        AppColorScheme.AUTO to "Otomatik",
        AppColorScheme.DARK to "Koyu",
        AppColorScheme.LIGHT to "Açık",
    )
    SingleChoiceSegmentedButtonRow {
        options.forEachIndexed { idx, (scheme, label) ->
            SegmentedButton(
                selected = current == scheme,
                onClick = { onSelect(scheme) },
                shape = SegmentedButtonDefaults.itemShape(index = idx, count = options.size),
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

// ── Divider ───────────────────────────────────────────────────────────────────

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp,
    )
}