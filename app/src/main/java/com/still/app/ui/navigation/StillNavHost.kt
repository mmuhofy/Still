package com.still.app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.still.app.ui.editor.NoteEditorScreen
import com.still.app.ui.notes.NotesListScreen
import com.still.app.ui.onboarding.OnboardingScreen
import com.still.app.util.Constants
import kotlinx.coroutines.flow.first

object Routes {
    const val ARG_NOTE_ID = "noteId"
    const val ONBOARDING = "onboarding"
    const val NOTES_LIST = "notes_list"
    const val NOTE_EDITOR = "note_editor/{$ARG_NOTE_ID}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"

    fun noteEditor(noteId: Long = -1L) = "note_editor/$noteId"
}

@Composable
fun StillNavHost(
    dataStore: DataStore<Preferences>,
) {
    val navController = rememberNavController()

    val onboardingCompleted by produceState<Boolean?>(initialValue = null) {
        val prefs = dataStore.data.first()
        value = prefs[booleanPreferencesKey(Constants.PrefKeys.ONBOARDING_COMPLETED)] ?: false
    }

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

        composable(
            route = Routes.NOTE_EDITOR,
            arguments = listOf(
                navArgument(Routes.ARG_NOTE_ID) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
            ),
        ) {
            NoteEditorScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.SEARCH) {
            // Placeholder — SearchScreen added in search step
        }

        composable(Routes.SETTINGS) {
            // Placeholder — SettingsScreen added in settings step
        }
    }
}