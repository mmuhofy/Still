package com.still.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Calm Luxury shapes — soft but not bubbly
val StillShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Chips, small tags
    small = RoundedCornerShape(8.dp),        // Buttons, text fields
    medium = RoundedCornerShape(12.dp),      // Note cards
    large = RoundedCornerShape(16.dp),       // Bottom sheets, dialogs
    extraLarge = RoundedCornerShape(24.dp),  // FAB, snackbar
)