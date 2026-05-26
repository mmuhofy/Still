package com.still.app.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.still.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Color scheme ──────────────────────────────────────────────────────────────

enum class AppColorScheme { AUTO, DARK, LIGHT }

// ── UI State ──────────────────────────────────────────────────────────────────

data class SettingsUiState(
    val colorScheme: AppColorScheme = AppColorScheme.AUTO,
    val aiEnabled: Boolean = false,
    val focusModeEnabled: Boolean = false,
    val typewriterModeEnabled: Boolean = false,
    val isLoading: Boolean = true,
)

// ── Events ────────────────────────────────────────────────────────────────────

sealed interface SettingsEvent {
    data class SetColorScheme(val scheme: AppColorScheme) : SettingsEvent
    data class SetAiEnabled(val enabled: Boolean) : SettingsEvent
    data class SetFocusMode(val enabled: Boolean) : SettingsEvent
    data class SetTypewriterMode(val enabled: Boolean) : SettingsEvent
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val colorSchemeKey = stringPreferencesKey(Constants.PrefKeys.COLOR_SCHEME)
    private val aiEnabledKey = booleanPreferencesKey(Constants.PrefKeys.AI_ENABLED)
    private val focusModeKey = booleanPreferencesKey(Constants.PrefKeys.FOCUS_MODE_ENABLED)
    private val typewriterModeKey = booleanPreferencesKey(Constants.PrefKeys.TYPEWRITER_MODE_ENABLED)

    val uiState = dataStore.data.map { prefs ->
        SettingsUiState(
            colorScheme = when (prefs[colorSchemeKey]) {
                "dark" -> AppColorScheme.DARK
                "light" -> AppColorScheme.LIGHT
                else -> AppColorScheme.AUTO
            },
            aiEnabled = prefs[aiEnabledKey] ?: false,
            focusModeEnabled = prefs[focusModeKey] ?: false,
            typewriterModeEnabled = prefs[typewriterModeKey] ?: false,
            isLoading = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun onEvent(event: SettingsEvent) {
        viewModelScope.launch {
            dataStore.edit { prefs ->
                when (event) {
                    is SettingsEvent.SetColorScheme ->
                        prefs[colorSchemeKey] = when (event.scheme) {
                            AppColorScheme.AUTO -> "auto"
                            AppColorScheme.DARK -> "dark"
                            AppColorScheme.LIGHT -> "light"
                        }

                    is SettingsEvent.SetAiEnabled ->
                        prefs[aiEnabledKey] = event.enabled

                    is SettingsEvent.SetFocusMode ->
                        prefs[focusModeKey] = event.enabled

                    is SettingsEvent.SetTypewriterMode ->
                        prefs[typewriterModeKey] = event.enabled
                }
            }
        }
    }
}