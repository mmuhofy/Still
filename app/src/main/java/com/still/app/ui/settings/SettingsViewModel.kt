package com.still.app.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.still.app.domain.repository.DriveRepository
import com.still.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Color scheme ──────────────────────────────────────────────────────────────

enum class AppColorScheme { AUTO, DARK, LIGHT }

// ── Font ──────────────────────────────────────────────────────────────────────

enum class AppFont(val label: String, val prefValue: String) {
    INTER(label = "Inter", prefValue = "inter"),
    LORA(label = "Lora", prefValue = "lora"),
    MONO(label = "Mono", prefValue = "mono"),
}

// ── UI State ──────────────────────────────────────────────────────────────────

data class SettingsUiState(
    val colorScheme: AppColorScheme = AppColorScheme.AUTO,
    val selectedFont: AppFont = AppFont.INTER,
    val aiEnabled: Boolean = false,
    val focusModeEnabled: Boolean = false,
    val typewriterModeEnabled: Boolean = false,
    // Drive
    val driveSyncEnabled: Boolean = false,
    val driveAccountEmail: String? = null,
    val driveLastSyncMs: Long = 0L,
    val driveSignInLoading: Boolean = false,
    val driveSyncLoading: Boolean = false,
    val driveError: String? = null,
    val isLoading: Boolean = true,
)

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface SettingsEvent {
    data class SetColorScheme(val scheme: AppColorScheme) : SettingsEvent
    data class SetFont(val font: AppFont) : SettingsEvent
    data class SetAiEnabled(val enabled: Boolean) : SettingsEvent
    data class SetFocusMode(val enabled: Boolean) : SettingsEvent
    data class SetTypewriterMode(val enabled: Boolean) : SettingsEvent
    // Drive
    data class SetDriveSyncEnabled(val enabled: Boolean) : SettingsEvent
    data object SignInDrive : SettingsEvent
    data object SignOutDrive : SettingsEvent
    data object SyncNow : SettingsEvent
    data object DismissDriveError : SettingsEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val driveRepository: DriveRepository,
) : ViewModel() {

    private val colorSchemeKey = stringPreferencesKey(Constants.PrefKeys.COLOR_SCHEME)
    private val fontKey = stringPreferencesKey(Constants.PrefKeys.SELECTED_FONT)
    private val aiEnabledKey = booleanPreferencesKey(Constants.PrefKeys.AI_ENABLED)
    private val focusModeKey = booleanPreferencesKey(Constants.PrefKeys.FOCUS_MODE_ENABLED)
    private val typewriterModeKey = booleanPreferencesKey(Constants.PrefKeys.TYPEWRITER_MODE_ENABLED)
    private val driveSyncEnabledKey = booleanPreferencesKey(Constants.PrefKeys.DRIVE_SYNC_ENABLED)
    private val driveAccountEmailKey = stringPreferencesKey(Constants.PrefKeys.DRIVE_ACCOUNT_EMAIL)
    private val driveLastSyncKey = longPreferencesKey(Constants.PrefKeys.DRIVE_LAST_SYNC_MS)

    // Mutable drive-specific loading/error state that DataStore doesn't own
    private val _driveUiState = MutableStateFlow(
        Triple(false, false, null as String?) // signInLoading, syncLoading, error
    )

    val uiState = combine(
        dataStore.data,
        _driveUiState,
    ) { prefs, (signInLoading, syncLoading, error) ->
        SettingsUiState(
            colorScheme = when (prefs[colorSchemeKey]) {
                "dark"  -> AppColorScheme.DARK
                "light" -> AppColorScheme.LIGHT
                else    -> AppColorScheme.AUTO
            },
            selectedFont = AppFont.entries.firstOrNull { it.prefValue == prefs[fontKey] }
                ?: AppFont.INTER,
            aiEnabled = prefs[aiEnabledKey] ?: false,
            focusModeEnabled = prefs[focusModeKey] ?: false,
            typewriterModeEnabled = prefs[typewriterModeKey] ?: false,
            driveSyncEnabled = prefs[driveSyncEnabledKey] ?: false,
            driveAccountEmail = prefs[driveAccountEmailKey],
            driveLastSyncMs = prefs[driveLastSyncKey] ?: 0L,
            driveSignInLoading = signInLoading,
            driveSyncLoading = syncLoading,
            driveError = error,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun onEvent(event: SettingsEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsEvent.SetColorScheme ->
                    dataStore.edit { prefs ->
                        prefs[colorSchemeKey] = when (event.scheme) {
                            AppColorScheme.AUTO  -> "auto"
                            AppColorScheme.DARK  -> "dark"
                            AppColorScheme.LIGHT -> "light"
                        }
                    }

                is SettingsEvent.SetFont ->
                    dataStore.edit { it[fontKey] = event.font.prefValue }

                is SettingsEvent.SetAiEnabled ->
                    dataStore.edit { it[aiEnabledKey] = event.enabled }

                is SettingsEvent.SetFocusMode ->
                    dataStore.edit { it[focusModeKey] = event.enabled }

                is SettingsEvent.SetTypewriterMode ->
                    dataStore.edit { it[typewriterModeKey] = event.enabled }

                is SettingsEvent.SetDriveSyncEnabled ->
                    dataStore.edit { it[driveSyncEnabledKey] = event.enabled }

                is SettingsEvent.SignInDrive -> {
                    _driveUiState.update { it.copy(first = true) }
                    driveRepository.signIn().fold(
                        onSuccess = { email ->
                            dataStore.edit { it[driveAccountEmailKey] = email }
                            dataStore.edit { it[driveSyncEnabledKey] = true }
                            _driveUiState.update { Triple(false, false, null) }
                        },
                        onFailure = { e ->
                            _driveUiState.update { Triple(false, false, e.message ?: "Giriş başarısız") }
                        },
                    )
                }

                is SettingsEvent.SignOutDrive -> {
                    driveRepository.signOut()
                    dataStore.edit { prefs ->
                        prefs.remove(driveAccountEmailKey)
                        prefs.remove(driveLastSyncKey)
                        prefs[driveSyncEnabledKey] = false
                    }
                }

                is SettingsEvent.SyncNow -> {
                    // SyncNow triggered manually — actual note serialization
                    // is handled by a higher-level use case, not here.
                    // This event is a placeholder for the manual sync button.
                    // UNTESTED — verify before use
                }

                is SettingsEvent.DismissDriveError ->
                    _driveUiState.update { it.copy(third = null) }
            }
        }
    }
}