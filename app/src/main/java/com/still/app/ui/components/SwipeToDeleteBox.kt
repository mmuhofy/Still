package com.still.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Trash2
import com.still.app.util.Constants

// Must match NoteListItem's shape exactly so bg doesn't bleed outside rounded corners
private val ItemShape    = RoundedCornerShape(14.dp)
private val DeleteRed    = Color(0xFFCF3B4B)
private val DeleteRedDim = Color(0xFF2A1518)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteBox(
    onDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var currentProgress by remember { mutableFloatStateOf(0f) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart)
                currentProgress >= Constants.SWIPE_DELETE_THRESHOLD
            else false
        },
    )

    currentProgress = dismissState.progress

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) onDeleted()
    }

    SwipeToDismissBox(
        state                       = dismissState,
        modifier                    = modifier,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val willDelete = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart
            val bgColor by animateColorAsState(
                targetValue   = if (willDelete) DeleteRed else DeleteRedDim,
                animationSpec = tween(Constants.ANIMATION_DURATION_MS),
                label         = "swipe_bg",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(ItemShape)          // same shape as item — no bleed
                    .background(bgColor),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    imageVector        = Lucide.Trash2,
                    contentDescription = "Sil",
                    tint               = Color.White.copy(alpha = if (willDelete) 1f else 0.5f),
                    modifier           = Modifier
                        .padding(end = 20.dp)
                        .size(20.dp),
                )
            }
        },
        content = { content() },
    )
}