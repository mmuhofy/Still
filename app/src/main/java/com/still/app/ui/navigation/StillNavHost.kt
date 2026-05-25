package com.still.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.still.app.ui.notes.NotesListScreen

// Route constants — no magic strings anywhere else in the codebase
object Routes {
    const val NOTES_LIST = "notes_list"
    const val NOTE_EDITOR = "note_editor/{noteId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val ONBOARDING = "onboarding"

    fun noteEditor(noteId: Long = -1L) = "note_editor/$noteId"
}

@Composable
fun StillNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.NOTES_LIST
    ) {
        composable(Routes.NOTES_LIST) {
            // Placeholder — replaced when NotesListScreen is implemented
            NotesListScreen(
                onNoteClick = { noteId ->
                    navController.navigate(Routes.noteEditor(noteId))
                },
                onNewNote = {
                    navController.navigate(Routes.noteEditor())
                },
                onSearchClick = {
                    navController.navigate(Routes.SEARCH)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
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

        composable(Routes.ONBOARDING) {
            // Placeholder — OnboardingScreen added in onboarding step
        }
    }
}