package com.still.app.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.still.app.ui.components.StillBottomNav
import com.still.app.ui.editor.NoteEditorScreen
import com.still.app.ui.notes.NotesListScreen
import com.still.app.ui.onboarding.OnboardingScreen
import com.still.app.ui.search.SearchScreen
import com.still.app.ui.settings.SettingsScreen
import com.still.app.util.Constants
import kotlinx.coroutines.flow.first

private const val FADE_DURATION  = 220
private const val SLIDE_DURATION = 300

object Routes {
    const val ARG_NOTE_ID = "noteId"
    const val ONBOARDING  = "onboarding"
    const val NOTES_LIST  = "notes_list"
    const val NOTE_EDITOR = "note_editor/{$ARG_NOTE_ID}"
    const val SEARCH      = "search"
    const val SETTINGS    = "settings"

    fun noteEditor(noteId: Long = -1L) = "note_editor/$noteId"

    val bottomNavRoutes = setOf(NOTES_LIST, SETTINGS)
}

// Push forward — new screen slides in from right, old slides out left
private val pushEnter = slideInHorizontally(tween(SLIDE_DURATION)) { it / 4 } + fadeIn(tween(SLIDE_DURATION))
private val pushExit  = slideOutHorizontally(tween(SLIDE_DURATION)) { -it / 6 } + fadeOut(tween(SLIDE_DURATION))

// Pop back — current slides out right, previous slides in from left
private val popEnter  = slideInHorizontally(tween(SLIDE_DURATION)) { -it / 6 } + fadeIn(tween(SLIDE_DURATION))
private val popExit   = slideOutHorizontally(tween(SLIDE_DURATION)) { it / 4 } + fadeOut(tween(SLIDE_DURATION))

// Tab switch — pure fade, no slide (same hierarchy level)
private val tabEnter  = fadeIn(tween(FADE_DURATION))
private val tabExit   = fadeOut(tween(FADE_DURATION))

@Composable
fun StillNavHost(
    dataStore: DataStore<Preferences>,
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

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
    val showBottomNav    = currentRoute in Routes.bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                StillBottomNav(
                    currentRoute  = currentRoute,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(Routes.NOTES_LIST) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    },
                    onNewNote = { navController.navigate(Routes.noteEditor()) },
                )
            }
        },
    ) { _ ->
        // Each screen manages its own innerPadding via its own Scaffold.
        // Do NOT pass innerPadding here — it would double-apply the bottom nav height.
        NavHost(
            navController       = navController,
            startDestination    = startDestination,
            enterTransition     = { pushEnter },
            exitTransition      = { pushExit },
            popEnterTransition  = { popEnter },
            popExitTransition   = { popExit },
        ) {
            // Onboarding → Notes: fade only — one-time transition, no back
            composable(
                route               = Routes.ONBOARDING,
                enterTransition     = { fadeIn(tween(FADE_DURATION)) },
                exitTransition      = { fadeOut(tween(FADE_DURATION)) },
                popEnterTransition  = { fadeIn(tween(FADE_DURATION)) },
                popExitTransition   = { fadeOut(tween(FADE_DURATION)) },
            ) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(Routes.NOTES_LIST) {
                            popUpTo(Routes.ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }

            // Notes list — tab level, fade only
            composable(
                route               = Routes.NOTES_LIST,
                enterTransition     = { tabEnter },
                exitTransition      = { tabExit },
                popEnterTransition  = { tabEnter },
                popExitTransition   = { tabExit },
            ) {
                NotesListScreen(
                    onNoteClick     = { noteId -> navController.navigate(Routes.noteEditor(noteId)) },
                    onSearchClick   = { navController.navigate(Routes.SEARCH) },
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) },
                )
            }

            // Note editor — push/pop (graph default)
            composable(
                route     = Routes.NOTE_EDITOR,
                arguments = listOf(
                    navArgument(Routes.ARG_NOTE_ID) {
                        type         = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) {
                NoteEditorScreen(onBack = { navController.popBackStack() })
            }

            // Search — push/pop (graph default)
            composable(Routes.SEARCH) {
                SearchScreen(
                    onNoteClick = { noteId -> navController.navigate(Routes.noteEditor(noteId)) },
                    onBack      = { navController.popBackStack() },
                )
            }

            // Settings — tab level, fade only
            composable(
                route               = Routes.SETTINGS,
                enterTransition     = { tabEnter },
                exitTransition      = { tabExit },
                popEnterTransition  = { tabEnter },
                popExitTransition   = { tabExit },
            ) {
                SettingsScreen()
            }
        }
    }
}