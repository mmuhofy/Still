package com.still.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.still.app.ui.notes.NotesListScreen
import com.still.app.ui.onboarding.OnboardingScreen
import com.still.app.util.Constants
import kotlinx.coroutines.flow.first
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import javax.inject.Inject

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val NOTES_LIST = "notes_list"
    const val NOTE_EDITOR = "note_editor/{noteId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"

    fun noteEditor(noteId: Long = -1L) = "note_editor/$noteId"
}

@Composable
fun StillNavHost(
    dataStore: DataStore<Preferences>,
) {
    val navController = rememberNavController()

    // Read onboarding state once at startup — no recomposition loop
    val onboardingCompleted by produceState<Boolean?>(initialValue = null) {
        val prefs = dataStore.data.first()
        value = prefs[booleanPreferencesKey(Constants.PrefKeys.ONBOARDING_COMPLETED)] ?: false
    }

    // Show nothing until DataStore resolves — avoids flash of wrong screen
    if (onboardingCompleted == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (onboardingCompleted == true) Routes.NOTES_LIST else Routes.ONBOARDING

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.NOTES_LIST) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }

        composable(Routes.NOTES_LIST) {
            NotesListScreen(
                onNoteClick = { noteId -> navController.navigate(Routes.noteEditor(noteId)) },
                onNewNote = { navController.navigate(Routes.noteEditor()) },
                onSearchClick = { navController.navigate(Routes.SEARCH) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(Routes.NOTE_EDITOR) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")?.toLongOrNull() ?: -1L
            // Placeholder — NoteEditorScreen added in editor step
        }

        composable(Routes.SEARCH) {
            // Placeholder — SearchScreen added in search step
        }

        composable(Routes.SETTINGS) {
            // Placeholder — SettingsScreen added in settings step
        }
    }
}