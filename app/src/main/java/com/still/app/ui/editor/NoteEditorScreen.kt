package com.still.app.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var overflowExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    // Pop back when note is deleted
    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    // FIX: Auto-focus only fires once (Unit key) after loading completes.
    // Previously keyed on state.isLoading which re-triggered on every recomposition
    // that changed isLoading, potentially resetting cursor position.
    LaunchedEffect(Unit) {
        if (state.noteId == -1L) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                title = {},
                actions = {
                    IconButton(onClick = { viewModel.onEvent(NoteEditorEvent.TogglePin) }) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = if (state.isPinned) "Sabitlemeyi kaldır" else "Sabitle",
                            tint = if (state.isPinned)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Box {
                        IconButton(onClick = { overflowExpanded = true }) {
                            Icon(
                                Icons.Outlined.MoreVert,
                                contentDescription = "Daha fazla",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        DropdownMenu(
                            expanded = overflowExpanded,
                            onDismissRequest = { overflowExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Sil",
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                },
                                onClick = {
                                    overflowExpanded = false
                                    viewModel.onEvent(NoteEditorEvent.DeleteNote)
                                },
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            FormattingToolbar(
                onBold      = { viewModel.onEvent(NoteEditorEvent.ApplyBold) },
                onItalic    = { viewModel.onEvent(NoteEditorEvent.ApplyItalic) },
                onUnderline = { viewModel.onEvent(NoteEditorEvent.ApplyUnderline) },
                onHeading   = { viewModel.onEvent(NoteEditorEvent.ApplyHeading) },
                onBullet    = { viewModel.onEvent(NoteEditorEvent.ApplyBullet) },
                onUndo      = { viewModel.onEvent(NoteEditorEvent.Undo) },
                onRedo      = { viewModel.onEvent(NoteEditorEvent.Redo) },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) return@Scaffold

        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .imePadding(),
        ) {
            NoteTextField(
                value = state.content,
                onValueChange = { viewModel.onEvent(NoteEditorEvent.ContentChanged(it)) },
                focusRequester = focusRequester,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}

// ── Text Field ────────────────────────────────────────────────────────────────

@Composable
private fun NoteTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    // FIX: The annotated string is built inside the decorationBox visualisation
    // path only, NOT passed as the value to BasicTextField. Passing an
    // AnnotatedString-based TextFieldValue while also setting selection causes
    // Compose to re-evaluate composition state on every keystroke and can reset
    // the cursor to the end of the text.
    //
    // Instead: BasicTextField owns the raw TextFieldValue (preserving selection
    // exactly as the user left it), and we apply the title style via a custom
    // VisualTransformation so the first line renders with headline weight
    // without touching the underlying selection at all.
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        // UNTESTED — verify before use
        visualTransformation = remember {
            TitleLineVisualTransformation()
        },
        decorationBox = { innerTextField ->
            Box {
                if (value.text.isEmpty()) {
                    Text(
                        text = "Yazmaya başla...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }
                innerTextField()
            }
        },
    )
}

// ── Visual Transformation — title line styled as headline ─────────────────────
//
// Applies SemiBold + headlineSmall fontSize to the first line only.
// Uses VisualTransformation so the underlying TextFieldValue (and its
// selection/cursor) is never touched — the cursor stays exactly where the
// user placed it.

private class TitleLineVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val raw = text.text
        val firstNewline = raw.indexOf('\n')
        val titleEnd = if (firstNewline == -1) raw.length else firstNewline

        val annotated = buildAnnotatedString {
            append(raw)
            if (titleEnd > 0) {
                addStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        // Hardcoded sp value — avoids capturing a Composable-scoped
                        // MaterialTheme reference inside a non-Composable class.
                        // headlineSmall is typically 24sp; adjust if your theme differs.
                        fontSize = androidx.compose.ui.unit.TextUnit(
                            24f,
                            androidx.compose.ui.unit.TextUnitType.Sp,
                        ),
                    ),
                    start = 0,
                    end = titleEnd,
                )
            }
        }

        // 1-to-1 offset mapping — no character substitution, just styling
        return androidx.compose.ui.text.input.TransformedText(
            text = annotated,
            offsetMapping = androidx.compose.ui.text.input.OffsetMapping.Identity,
        )
    }
}

// ── Formatting Toolbar ────────────────────────────────────────────────────────

@Composable
private fun FormattingToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onHeading: () -> Unit,
    onBullet: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolbarButton(icon = Icons.Outlined.FormatBold,         label = "Kalın",        onClick = onBold)
                ToolbarButton(icon = Icons.Outlined.FormatItalic,       label = "İtalik",       onClick = onItalic)
                ToolbarButton(icon = Icons.Outlined.FormatUnderlined,   label = "Altı çizili",  onClick = onUnderline)
                ToolbarButton(icon = Icons.Outlined.Title,              label = "Başlık",       onClick = onHeading)
                ToolbarButton(icon = Icons.Outlined.FormatListBulleted, label = "Liste",        onClick = onBullet)

                Spacer(Modifier.weight(1f))

                ToolbarButton(icon = Icons.AutoMirrored.Outlined.Undo, label = "Geri al", onClick = onUndo)
                ToolbarButton(icon = Icons.AutoMirrored.Outlined.Redo, label = "Yinele",  onClick = onRedo)
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun ToolbarButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}