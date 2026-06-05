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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.still.app.util.MarkdownRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var overflowExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    LaunchedEffect(state.isLoading) {
        if (!state.isLoading && state.noteId == -1L) focusRequester.requestFocus()
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
                                        "Sil",
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            NoteTextField(
                rawText        = state.rawText,
                selection      = state.selection,
                onValueChange  = { tfv ->
                    viewModel.onEvent(NoteEditorEvent.ContentChanged(tfv.text, tfv.selection))
                },
                focusRequester = focusRequester,
                modifier       = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }
    }
}

// ── Text Field ────────────────────────────────────────────────────────────────

@Composable
private fun NoteTextField(
    rawText: String,
    selection: TextRange,
    onValueChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val headingSize = MaterialTheme.typography.headlineSmall.fontSize
    val bodySize    = MaterialTheme.typography.bodyLarge.fontSize

    // Marker color — same hue as text but very faint
    val markerColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)

    // rawText == annotated.text — offsets are 1:1, no mapping needed
    val annotated = remember(rawText, markerColor) {
        MarkdownRenderer.render(
            raw             = rawText,
            headingFontSize = headingSize,
            bodyFontSize    = bodySize,
            markerColor     = markerColor,
        )
    }

    // Title line — first line always gets headline weight on top of markdown styles
    val firstNewline = rawText.indexOf('\n')
    val titleEnd     = if (firstNewline == -1) rawText.length else firstNewline

    val withTitleStyle = remember(annotated, titleEnd) {
        buildAnnotatedString {
            append(annotated)
            if (titleEnd > 0) {
                addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = headingSize,
                    ),
                    start = 0,
                    end   = titleEnd.coerceAtMost(annotated.length),
                )
            }
        }
    }

    // selection is on rawText — safe because rawText.length == annotated.length
    val tfv = TextFieldValue(
        annotatedString = withTitleStyle,
        selection       = TextRange(
            selection.start.coerceIn(0, rawText.length),
            selection.end.coerceIn(0, rawText.length),
        ),
    )

    BasicTextField(
        value         = tfv,
        onValueChange = onValueChange,
        modifier      = modifier.focusRequester(focusRequester),
        textStyle     = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box {
                if (rawText.isEmpty()) {
                    Text(
                        text  = "Yazmaya başla...",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    )
                }
                innerTextField()
            }
        },
    )
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
        color          = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier       = Modifier.fillMaxWidth().imePadding(),
    ) {
        Column {
            HorizontalDivider(
                color     = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp,
            )
            Row(
                modifier          = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolbarButton(Icons.Outlined.FormatBold,         "Kalın",       onBold)
                ToolbarButton(Icons.Outlined.FormatItalic,       "İtalik",      onItalic)
                ToolbarButton(Icons.Outlined.FormatUnderlined,   "Altı çizili", onUnderline)
                ToolbarButton(Icons.Outlined.Title,              "Başlık",      onHeading)
                ToolbarButton(Icons.Outlined.FormatListBulleted, "Liste",       onBullet)
                Spacer(Modifier.weight(1f))
                ToolbarButton(Icons.AutoMirrored.Outlined.Undo,  "Geri al",    onUndo)
                ToolbarButton(Icons.AutoMirrored.Outlined.Redo,  "Yinele",     onRedo)
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun ToolbarButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(20.dp),
        )
    }
}