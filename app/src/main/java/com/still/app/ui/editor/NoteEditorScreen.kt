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
import androidx.compose.material.icons.outlined.AutoAwesome
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
                    // AI loading indicator
                    if (state.isAiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 4.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                        )
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
                aiError = state.aiError,
                onValueChange = { viewModel.onEvent(NoteEditorEvent.ContentChanged(it)) },
                onAcceptGhost = { viewModel.onEvent(NoteEditorEvent.AcceptGhost) },
                onLongPressGhost = { viewModel.onEvent(NoteEditorEvent.RequestVariants) },
                focusRequester = focusRequester,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }

    // Variants bottom sheet
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
    aiError: String?,
    onValueChange: (TextFieldValue) -> Unit,
    onAcceptGhost: () -> Unit,
    onLongPressGhost: () -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val text = value.text
    val firstNewline = text.indexOf('\n')
    val titleEnd = if (firstNewline == -1) text.length else firstNewline

    // Build annotated string: title bold + body normal + ghost italic
    val annotated = buildAnnotatedString {
        if (titleEnd > 0) {
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                )
            ) {
                append(text.substring(0, titleEnd))
            }
            append(text.substring(titleEnd))
        } else {
            append(text)
        }

        // Ghost text appended inline after cursor
        if (ghostText.isNotBlank()) {
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    fontStyle = FontStyle.Italic,
                )
            ) {
                append(ghostText)
            }
        }

        // Inline AI error message
        if (aiError != null) {
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    fontStyle = FontStyle.Italic,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                )
            ) {
                append("  $aiError")
            }
        }
    }

    Column(modifier = modifier) {
        BasicTextField(
            value = TextFieldValue(
                annotatedString = annotated,
                selection = value.selection,
                composition = value.composition,
            ),
            onValueChange = { new ->
                // Strip ghost/error from stored value — only real text goes to ViewModel
                onValueChange(
                    TextFieldValue(
                        text = new.text.take(text.length),
                        selection = new.selection,
                        composition = new.composition,
                    )
                )
            },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onBackground,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
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

        // Ghost text tap target (tap = accept, long-press = variants)
        if (ghostText.isNotBlank()) {
            Text(
                text = "↵ Kabul et",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .combinedClickable(
                        onClick = onAcceptGhost,
                        onLongClick = onLongPressGhost,
                    ),
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