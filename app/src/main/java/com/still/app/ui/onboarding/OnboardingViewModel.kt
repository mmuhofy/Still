package com.still.app.ui.onboarding

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.still.app.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val selectedTheme: String = "calm_luxury",
    val selectedColorScheme: String = "auto",
    val aiEnabled: Boolean = false,
    val isCompleting: Boolean = false,
)

sealed interface OnboardingEvent {
    data class ThemeSelected(val theme: String) : OnboardingEvent
    data class ColorSchemeSelected(val scheme: String) : OnboardingEvent
    data class AiModeSelected(val enabled: Boolean) : OnboardingEvent
    data object NextPage : OnboardingEvent
    data object Complete : OnboardingEvent
}

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.ThemeSelected ->
                _uiState.update { it.copy(selectedTheme = event.theme) }

            is OnboardingEvent.ColorSchemeSelected ->
                _uiState.update { it.copy(selectedColorScheme = event.scheme) }

            is OnboardingEvent.AiModeSelected ->
                _uiState.update { it.copy(aiEnabled = event.enabled) }

            OnboardingEvent.NextPage ->
                _uiState.update { it.copy(currentPage = it.currentPage + 1) }

            OnboardingEvent.Complete -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        _uiState.update { it.copy(isCompleting = true) }
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey(Constants.PrefKeys.ONBOARDING_COMPLETED)] = true
                prefs[stringPreferencesKey(Constants.PrefKeys.SELECTED_THEME)] =
                    _uiState.value.selectedTheme
                prefs[stringPreferencesKey(Constants.PrefKeys.COLOR_SCHEME)] =
                    _uiState.value.selectedColorScheme
                prefs[booleanPreferencesKey(Constants.PrefKeys.AI_ENABLED)] =
                    _uiState.value.aiEnabled
            }
        }
    }
}