package com.still.app.ui.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
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
    val variantsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && state.noteId == -1L) {
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
                    if (state.isAiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
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
                onBold = { viewModel.onEvent(NoteEditorEvent.ApplyBold) },
                onItalic = { viewModel.onEvent(NoteEditorEvent.ApplyItalic) },
                onUnderline = { viewModel.onEvent(NoteEditorEvent.ApplyUnderline) },
                onHeading = { viewModel.onEvent(NoteEditorEvent.ApplyHeading) },
                onBullet = { viewModel.onEvent(NoteEditorEvent.ApplyBullet) },
                onUndo = { viewModel.onEvent(NoteEditorEvent.Undo) },
                onRedo = { viewModel.onEvent(NoteEditorEvent.Redo) },
            )
        },
    ) { innerPadding ->
        if (state.isLoading) return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            NoteTextField(
                value = state.content,
                ghostText = state.ghostText,
                realTextLength = state.realTextLength,
                aiError = state.aiError,
                onValueChange = { viewModel.onEvent(NoteEditorEvent.ContentChanged(it)) },
                onLongPressGhost = { viewModel.onEvent(NoteEditorEvent.RequestVariants) },
                focusRequester = focusRequester,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }

    if (state.showVariants) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onEvent(NoteEditorEvent.DismissVariants) },
            sheetState = variantsSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Alternatifler",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                state.variants.forEach { variant ->
                    TextButton(
                        onClick = { viewModel.onEvent(NoteEditorEvent.AcceptVariant(variant)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = variant,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(Modifier.padding(bottom = 16.dp))
            }
        }
    }
}

// ── Text Field ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteTextField(
    value: TextFieldValue,
    ghostText: String,
    realTextLength: Int,
    aiError: String?,
    onValueChange: (TextFieldValue) -> Unit,
    onLongPressGhost: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val text = value.text
    val firstNewline = text.indexOf('\n')
    val titleEnd = if (firstNewline == -1) text.length else firstNewline

    val ghostColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val titleFontSize = MaterialTheme.typography.headlineSmall.fontSize

    // Build AnnotatedString: real text (title bold) + ghost text (muted italic)
    val annotated = buildAnnotatedString {
        // Title line — bold + larger
        if (titleEnd > 0) {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = titleFontSize)) {
                append(text.substring(0, titleEnd))
            }
            append(text.substring(titleEnd))
        } else {
            append(text)
        }

        // Ghost inline after real text — only if cursor is at end
        if (ghostText.isNotBlank() && value.selection.start == text.length) {
            withStyle(SpanStyle(color = ghostColor, fontStyle = FontStyle.Italic)) {
                append(ghostText)
            }
        }
    }

    // We pass annotated string but strip ghost in onValueChange using realTextLength
    val displayValue = TextFieldValue(
        annotatedString = annotated,
        selection = value.selection,
        composition = value.composition,
    )

    Column(modifier = modifier) {
        BasicTextField(
            value = displayValue,
            onValueChange = { new ->
                // Strip ghost from incoming text — real text is at most realTextLength chars
                // (ghost was appended visually, user typed on top of it)
                val strippedText = if (new.text.length > realTextLength + ghostText.length) {
                    // User typed after ghost — take real part + new char
                    new.text.take(realTextLength) + new.text.drop(realTextLength + ghostText.length)
                } else {
                    new.text.take(realTextLength.coerceAtMost(new.text.length))
                }
                val strippedSelection = TextRange(
                    new.selection.start.coerceAtMost(strippedText.length)
                )
                onValueChange(
                    TextFieldValue(
                        text = strippedText,
                        selection = strippedSelection,
                        composition = new.composition,
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (text.isEmpty()) {
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

        // Long-press hint on ghost (tap = Enter to accept, long-press = variants)
        if (ghostText.isNotBlank() && value.selection.start == text.length) {
            Text(
                text = "↵ kabul",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                ),
                modifier = Modifier
                    .padding(top = 2.dp)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongPressGhost,
                    ),
            )
        }

        if (aiError != null) {
            Text(
                text = aiError,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic,
                ),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
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
                ToolbarButton(Icons.Outlined.FormatBold, "Kalın", onBold)
                ToolbarButton(Icons.Outlined.FormatItalic, "İtalik", onItalic)
                ToolbarButton(Icons.Outlined.FormatUnderlined, "Altı çizili", onUnderline)
                ToolbarButton(Icons.Outlined.Title, "Başlık", onHeading)
                ToolbarButton(Icons.Outlined.FormatListBulleted, "Liste", onBullet)
                Spacer(Modifier.weight(1f))
                ToolbarButton(Icons.AutoMirrored.Outlined.Undo, "Geri al", onUndo)
                ToolbarButton(Icons.AutoMirrored.Outlined.Redo, "Yinele", onRedo)
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun ToolbarButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}