package com.still.app.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.still.app.ui.components.StillBottomNav
import com.still.app.ui.editor.NoteEditorScreen
import com.still.app.ui.notes.NotesListScreen
import com.still.app.ui.onboarding.OnboardingScreen
import com.still.app.ui.search.SearchScreen
import com.still.app.ui.settings.SettingsScreen
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

    val bottomNavRoutes = setOf(NOTES_LIST, SETTINGS)
}

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
    val showBottomNav = currentRoute in Routes.bottomNavRoutes
    val showFab = currentRoute == Routes.NOTES_LIST

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                StillBottomNav(
                    currentRoute = currentRoute,
                    onTabSelected = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(Routes.NOTES_LIST) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.fillMaxSize(),
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
                        topPadding = innerPadding.calculateTopPadding(),
                        bottomPadding = innerPadding.calculateBottomPadding(),
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
                    SearchScreen(
                        onNoteClick = { noteId -> navController.navigate(Routes.noteEditor(noteId)) },
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        topPadding = innerPadding.calculateTopPadding(),
                        bottomPadding = innerPadding.calculateBottomPadding(),
                    )
                }
            }

            AnimatedVisibility(
                visible = showFab,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = innerPadding.calculateBottomPadding() + 20.dp),
            ) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Routes.noteEditor()) },
                    icon = { Icon(imageVector = Lucide.Plus, contentDescription = null) },
                    text = { Text(text = "Yeni Not", style = MaterialTheme.typography.labelLarge) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    shape = MaterialTheme.shapes.large,
                )
            }
        }
    }
}