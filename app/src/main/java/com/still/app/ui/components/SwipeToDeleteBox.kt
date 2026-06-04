package com.still.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import com.still.app.util.Constants

private val DeleteRed    = Color(0xFFCF3B4B)
private val DeleteRedDim = Color(0xFF2A1518)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteBox(
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        // confirmValueChange: return true ONLY when the user releases at or
        // past the threshold. While the finger is still on screen this lambda
        // is called with the *target* value — we check progress to decide
        // whether to confirm the snap.
        confirmValueChange = { value ->
            // Allow snap only when drag has passed 75% of the item width.
            // progress is 0→1 where 1 = fully dismissed.
            if (value == SwipeToDismissBoxValue.EndToStart) {
                dismissState.progress >= Constants.SWIPE_DELETE_THRESHOLD
            } else {
                false
            }
        },
    )

    // Fire onDeleted only after the item has fully settled (currentValue),
    // never on targetValue — prevents firing when user drags back.
    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDeleted()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val willDelete = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val bgColor by animateColorAsState(
                targetValue = if (willDelete) DeleteRed else DeleteRedDim,
                animationSpec = tween(Constants.ANIMATION_DURATION_MS),
                label = "swipe_bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(bgColor),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector = Lucide.Trash2,
                    contentDescription = "Sil",
                    tint = Color.White,
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .size(22.dp),
                )
            }
        },
        content = { content() },
    )
}