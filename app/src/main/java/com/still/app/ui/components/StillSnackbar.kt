package com.still.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.still.app.util.Constants
import kotlinx.coroutines.delay

/**
 * Calm Luxury snackbar — frosted glass feel via surface tonal elevation.
 * Appears from bottom, auto-dismisses after [Constants.SNACKBAR_UNDO_DURATION_MS].
 *
 * Usage: place inside a Box at the bottom of the screen, pass [visible] from ViewModel state.
 */
@Composable
fun StillSnackbar(
    visible: Boolean,
    message: String,
    actionLabel: String = "Geri Al",
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Auto-dismiss timer — resets if visible toggles
    LaunchedEffect(visible) {
        if (visible) {
            delay(Constants.SNACKBAR_UNDO_DURATION_MS)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(tween(Constants.ANIMATION_DURATION_MS)) { it } +
                fadeIn(tween(Constants.ANIMATION_DURATION_MS)),
        exit = slideOutVertically(tween(Constants.ANIMATION_DURATION_MS)) { it } +
                fadeOut(tween(Constants.ANIMATION_DURATION_MS)),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp)
                .navigationBarsPadding(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp,
            shadowElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    onAction()
                }) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}