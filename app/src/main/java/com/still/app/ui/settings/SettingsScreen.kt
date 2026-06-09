package com.still.app.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.Bot
import com.composables.icons.lucide.ChevronRight
import com.composables.icons.lucide.CloudUpload
import com.composables.icons.lucide.Focus
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.LogOut
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageSquare
import com.composables.icons.lucide.Moon
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.ScrollText
import com.composables.icons.lucide.Shield
import com.composables.icons.lucide.Type
import com.still.app.ui.theme.CalmGold
import com.still.app.ui.theme.CalmGoldSubtle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    topPadding: Dp = 0.dp,
    bottomPadding: Dp = 0.dp,
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
                        fontWeight = FontWeight.SemiBold,
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
                .padding(top = innerPadding.calculateTopPadding().coerceAtLeast(topPadding))
                .padding(bottom = innerPadding.calculateBottomPadding().coerceAtLeast(bottomPadding))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // ── Görünüm ───────────────────────────────────────────────────────
            SettingsGroup(title = "Görünüm") {
                SettingsRowWithContent(
                    icon = Lucide.Moon,
                    label = "Tema modu",
                ) {
                    ColorSchemeSelector(
                        current = uiState.colorScheme,
                        onSelect = { viewModel.onEvent(SettingsEvent.SetColorScheme(it)) },
                    )
                }
                SettingsGroupDivider()
                SettingsRowWithContent(
                    icon = Lucide.Type,
                    label = "Yazı tipi",
                    description = uiState.selectedFont.label,
                ) {
                    FontSelector(
                        current = uiState.selectedFont,
                        onSelect = { viewModel.onEvent(SettingsEvent.SetFont(it)) },
                    )
                }
            }

            // ── Yazma ─────────────────────────────────────────────────────────
            SettingsGroup(title = "Yazma") {
                SettingsSwitchRow(
                    icon = Lucide.Bot,
                    label = "AI tamamlama",
                    description = "Yazarken akıllı öneri göster",
                    checked = uiState.aiEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.SetAiEnabled(it)) },
                )
                SettingsGroupDivider()
                SettingsSwitchRow(
                    icon = Lucide.Focus,
                    label = "Odak modu",
                    description = "Yalnızca metni göster",
                    checked = uiState.focusModeEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.SetFocusMode(it)) },
                    badge = "Yakında",
                    enabled = false,
                )
                SettingsGroupDivider()
                SettingsSwitchRow(
                    icon = Lucide.ScrollText,
                    label = "Daktilo modu",
                    description = "Aktif satırı ortada tut",
                    checked = uiState.typewriterModeEnabled,
                    onCheckedChange = { viewModel.onEvent(SettingsEvent.SetTypewriterMode(it)) },
                    badge = "Yakında",
                    enabled = false,
                )
            }

            // ── Senkronizasyon ────────────────────────────────────────────────
            SettingsGroup(title = "Senkronizasyon") {
                if (uiState.driveAccountEmail == null) {
                    // Not signed in — show sign-in row
                    SettingsTappableRow(
                        icon = Lucide.CloudUpload,
                        label = "Google Drive ile bağlan",
                        description = "Notlarını otomatik yedekle",
                        loading = uiState.driveSignInLoading,
                        onClick = { viewModel.onEvent(SettingsEvent.SignInDrive) },
                    )
                } else {
                    // Signed in — show account + sync toggle + last sync + sign out
                    SettingsSwitchRow(
                        icon = Lucide.CloudUpload,
                        label = "Drive sync",
                        description = uiState.driveAccountEmail,
                        checked = uiState.driveSyncEnabled,
                        onCheckedChange = { viewModel.onEvent(SettingsEvent.SetDriveSyncEnabled(it)) },
                    )
                    SettingsGroupDivider()
                    SettingsTappableRow(
                        icon = Lucide.RefreshCw,
                        label = "Şimdi senkronize et",
                        description = uiState.driveLastSyncMs.toLastSyncLabel(),
                        loading = uiState.driveSyncLoading,
                        onClick = { viewModel.onEvent(SettingsEvent.SyncNow) },
                    )
                    SettingsGroupDivider()
                    SettingsTappableRow(
                        icon = Lucide.LogOut,
                        label = "Hesabın bağlantısını kes",
                        tint = MaterialTheme.colorScheme.error,
                        onClick = { viewModel.onEvent(SettingsEvent.SignOutDrive) },
                    )
                }

                // Error message if any
                val driveError = uiState.driveError
                if (driveError != null) {
                    SettingsGroupDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = driveError,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { viewModel.onEvent(SettingsEvent.DismissDriveError) }) {
                            Text("Tamam", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // ── Hakkında ──────────────────────────────────────────────────────
            SettingsGroup(title = "Hakkında") {
                SettingsReadOnlyRow(
                    icon = Lucide.Info,
                    label = "Sürüm",
                    value = appVersion,
                )
                SettingsGroupDivider()
                SettingsTappableRow(
                    icon = Lucide.MessageSquare,
                    label = "Geri bildirim gönder",
                    onClick = { /* TODO */ },
                )
                SettingsGroupDivider()
                SettingsTappableRow(
                    icon = Lucide.Shield,
                    label = "Gizlilik politikası",
                    onClick = { /* TODO */ },
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun Long.toLastSyncLabel(): String {
    if (this == 0L) return "Henüz senkronize edilmedi"
    val fmt = SimpleDateFormat("d MMM, HH:mm", Locale("tr"))
    return "Son: ${fmt.format(Date(this))}"
}

// ── Group card ────────────────────────────────────────────────────────────────

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = TextUnit(value = 1.2f, type = TextUnitType.Sp),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) { content() }
    }
}

// ── Divider ───────────────────────────────────────────────────────────────────

@Composable
private fun SettingsGroupDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 52.dp)
            .height(0.5.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
    )
}

// ── Row: icon + label + end slot ─────────────────────────────────────────────

@Composable
private fun SettingsRowWithContent(
    icon: ImageVector,
    label: String,
    description: String? = null,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon)
        Spacer(modifier = Modifier.width(12.dp))
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
        Spacer(modifier = Modifier.width(12.dp))
        content()
    }
}

// ── Row: switch ───────────────────────────────────────────────────────────────

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badge: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon, dimmed = !enabled)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    BadgeChip(text = badge)
                }
            }
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = CalmGold,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = Color.Transparent,
                disabledCheckedTrackColor = CalmGold.copy(alpha = 0.3f),
                disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            ),
        )
    }
}

// ── Row: tappable ─────────────────────────────────────────────────────────────

@Composable
private fun SettingsTappableRow(
    icon: ImageVector,
    label: String,
    description: String? = null,
    loading: Boolean = false,
    tint: Color = CalmGold,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !loading, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon, tint = tint)
        Spacer(modifier = Modifier.width(12.dp))
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
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = CalmGold,
            )
        } else {
            Icon(
                imageVector = Lucide.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

// ── Row: read-only ────────────────────────────────────────────────────────────

@Composable
private fun SettingsReadOnlyRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIcon(icon)
        Spacer(modifier = Modifier.width(12.dp))
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

// ── Icon circle ───────────────────────────────────────────────────────────────

@Composable
private fun SettingsIcon(
    icon: ImageVector,
    dimmed: Boolean = false,
    tint: Color = CalmGold,
) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(CalmGoldSubtle),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (dimmed) tint.copy(alpha = 0.35f) else tint,
            modifier = Modifier.size(16.dp),
        )
    }
}

// ── Badge chip ────────────────────────────────────────────────────────────────

@Composable
private fun BadgeChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(CalmGoldSubtle)
            .border(0.5.dp, CalmGold.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = CalmGold,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ── Color scheme selector ─────────────────────────────────────────────────────

@Composable
private fun ColorSchemeSelector(current: AppColorScheme, onSelect: (AppColorScheme) -> Unit) {
    val options = listOf(
        AppColorScheme.AUTO  to "Oto",
        AppColorScheme.DARK  to "Koyu",
        AppColorScheme.LIGHT to "Açık",
    )
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { (scheme, label) ->
            val selected = current == scheme
            val bgColor by animateColorAsState(
                targetValue = if (selected) CalmGold else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(200), label = "bg_$label",
            )
            val textColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.background
                              else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200), label = "txt_$label",
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onSelect(scheme) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}

// ── Font selector ─────────────────────────────────────────────────────────────

@Composable
private fun FontSelector(current: AppFont, onSelect: (AppFont) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        AppFont.entries.forEach { font ->
            val selected = current == font
            val bgColor by animateColorAsState(
                targetValue = if (selected) CalmGold else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(200), label = "fontbg_${font.name}",
            )
            val textColor by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.background
                              else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200), label = "fonttxt_${font.name}",
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(bgColor)
                    .clickable { onSelect(font) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = font.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
    }
}