package com.still.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

private val MenuShape  = RoundedCornerShape(14.dp)
private val MenuBg     = Color(0xFF1C1C26)
private val MenuBorder = Color(0x22FFFFFF)

/**
 * Calm Luxury styled dropdown menu.
 * Drop-in replacement for Material3 DropdownMenu.
 */
@Composable
fun StillDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: DpOffset = DpOffset(0.dp, 4.dp),
    content: @Composable () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        modifier = Modifier
            .shadow(elevation = 24.dp, shape = MenuShape, ambientColor = Color.Black.copy(0.5f))
            .clip(MenuShape)
            .background(MenuBg)
            .border(width = 0.5.dp, color = MenuBorder, shape = MenuShape)
            .sizeIn(minWidth = 140.dp),
    ) {
        content()
    }
}

/**
 * Single item inside [StillDropdownMenu].
 */
@Composable
fun StillDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    DropdownMenuItem(
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = color,
            )
        },
        onClick = onClick,
        leadingIcon = leadingIcon,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        colors = MenuDefaults.itemColors(
            textColor = color,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}