package com.still.app.util

object Constants {

    // Database
    const val DATABASE_NAME = "still_database"

    // DataStore
    const val DATASTORE_PREFERENCES_NAME = "still_preferences"

    // Autosave
    const val AUTOSAVE_DEBOUNCE_MS = 1500L

    // AI completion
    const val AI_TRIGGER_DEBOUNCE_MS = 400L
    const val AI_COMPLETION_VARIANTS = 3

    // Delete undo snackbar
    const val SNACKBAR_UNDO_DURATION_MS = 3000L

    // Animation
    const val ANIMATION_DURATION_MS = 300

    // Note preview
    const val NOTE_CARD_PREVIEW_LINES = 2
    const val NOTE_LIST_PREVIEW_LINES = 1

    // DataStore preference keys — defined as strings to avoid DataStore import here
    object PrefKeys {
        const val ONBOARDING_COMPLETED = "onboarding_completed"
        const val NOTE_VIEW_MODE = "note_view_mode"       // "card" | "list"
        const val COLOR_SCHEME = "color_scheme"           // "auto" | "dark" | "light"
        const val SELECTED_THEME = "selected_theme"       // "calm_luxury" | ...
        const val AI_ENABLED = "ai_enabled"
        const val FOCUS_MODE_ENABLED = "focus_mode_enabled"
        const val TYPEWRITER_MODE_ENABLED = "typewriter_mode_enabled"
    }
}