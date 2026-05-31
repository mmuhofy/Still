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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Bold
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Heading2
import com.composables.icons.lucide.Italic
import com.composables.icons.lucide.List
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pin
import com.composables.icons.lucide.Redo2
import com.composables.icons.lucide.Underline
import com.composables.icons.lucide.Undo2

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

    // Fires exactly once — auto-focus only for new notes
    LaunchedEffect(Unit) {
        if (state.noteId == -1L) focusRequester.requestFocus()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Lucide.ArrowLeft,
                            contentDescription = "Geri",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                title = {},
                actions = {
                    IconButton(onClick = { viewModel.onEvent(NoteEditorEvent.TogglePin) }) {
                        Icon(
                            imageVector = Lucide.Pin,
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
                                imageVector = Lucide.EllipsisVertical,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
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
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MaterialTheme.colorScheme.onBackground,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        // MarkdownVisualTransformation styles the raw text in-place:
        // markers (**) remain visible but the spanned region is also styled.
        // Identity offset mapping is valid because no characters are hidden.
        visualTransformation = remember { MarkdownVisualTransformation() },
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

// ── Markdown Visual Transformation ────────────────────────────────────────────
//
// Applies visual styling to raw markdown text WITHOUT removing any characters.
// Because the displayed string has the same length as the raw string,
// OffsetMapping.Identity is correct — cursor positions are never affected.
//
// Supported:
//   First line          → SemiBold 24sp  (title)
//   **text**            → Bold
//   __text__            → Underline      (matched before _italic_)
//   _text_              → Italic
//   ## text  (line)     → Bold 20sp
//   - item   (line)     → no extra style (bullet char inserted by ViewModel)

private class MarkdownVisualTransformation : VisualTransformation {

    // Same patterns as MarkdownRenderer — order matters: __ before _
    private val underlineRegex = Regex("""__(.*?)__""")
    private val boldRegex      = Regex("""\*\*(.*?)\*\*""")
    private val italicRegex    = Regex("""(?<!_)_(?!_)(.*?)(?<!_)_(?!_)""")
    private val headingRegex   = Regex("""^#{1,3} .+""", RegexOption.MULTILINE)

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val firstNewline = raw.indexOf('\n')
        val titleEnd = if (firstNewline == -1) raw.length else firstNewline

        val annotated = buildAnnotatedString {
            append(raw)

            // ── Title line — first line always gets headline weight ────────────
            if (titleEnd > 0) {
                addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = TextUnit(24f, TextUnitType.Sp),
                    ),
                    start = 0,
                    end = titleEnd,
                )
            }

            // ── Body markdown — only process text after the title line ─────────
            val bodyStart = if (firstNewline == -1) raw.length else firstNewline + 1
            if (bodyStart >= raw.length) return@buildAnnotatedString

            // Underline — must run before italic so __ is consumed first
            underlineRegex.findAll(raw, bodyStart).forEach { m ->
                addStyle(
                    SpanStyle(textDecoration = TextDecoration.Underline),
                    start = m.range.first,
                    end = m.range.last + 1,
                )
            }

            // Bold
            boldRegex.findAll(raw, bodyStart).forEach { m ->
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold),
                    start = m.range.first,
                    end = m.range.last + 1,
                )
            }

            // Italic
            italicRegex.findAll(raw, bodyStart).forEach { m ->
                addStyle(
                    SpanStyle(fontStyle = FontStyle.Italic),
                    start = m.range.first,
                    end = m.range.last + 1,
                )
            }

            // Headings — full line styled as bold + larger text
            headingRegex.findAll(raw, bodyStart).forEach { m ->
                addStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = TextUnit(20f, TextUnitType.Sp),
                    ),
                    start = m.range.first,
                    end = m.range.last + 1,
                )
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
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
                ToolbarButton(icon = Lucide.Bold,      label = "Kalın",       onClick = onBold)
                ToolbarButton(icon = Lucide.Italic,    label = "İtalik",      onClick = onItalic)
                ToolbarButton(icon = Lucide.Underline, label = "Altı çizili", onClick = onUnderline)
                ToolbarButton(icon = Lucide.Heading2,  label = "Başlık",      onClick = onHeading)
                ToolbarButton(icon = Lucide.List,      label = "Liste",       onClick = onBullet)

                Spacer(Modifier.weight(1f))

                ToolbarButton(icon = Lucide.Undo2, label = "Geri al", onClick = onUndo)
                ToolbarButton(icon = Lucide.Redo2, label = "Yinele",  onClick = onRedo)
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