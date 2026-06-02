package com.still.app.ui.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                onBold        = { viewModel.onEvent(NoteEditorEvent.ApplyBold) },
                onItalic      = { viewModel.onEvent(NoteEditorEvent.ApplyItalic) },
                onUnderline   = { viewModel.onEvent(NoteEditorEvent.ApplyUnderline) },
                onHeading     = { level -> viewModel.onEvent(NoteEditorEvent.ApplyHeading(level)) },
                onBullet      = { viewModel.onEvent(NoteEditorEvent.ApplyBullet) },
                onUndo        = { viewModel.onEvent(NoteEditorEvent.Undo) },
                onRedo        = { viewModel.onEvent(NoteEditorEvent.Redo) },
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

private class MarkdownVisualTransformation : VisualTransformation {

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

            // Title line — SemiBold 24sp
            if (titleEnd > 0) {
                addStyle(
                    SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = TextUnit(24f, TextUnitType.Sp)),
                    start = 0, end = titleEnd,
                )
            }

            val bodyStart = if (firstNewline == -1) raw.length else firstNewline + 1
            if (bodyStart >= raw.length) return@buildAnnotatedString

            // Underline before italic so __ is consumed first
            underlineRegex.findAll(raw, bodyStart).forEach { m ->
                addStyle(SpanStyle(textDecoration = TextDecoration.Underline), m.range.first, m.range.last + 1)
            }
            boldRegex.findAll(raw, bodyStart).forEach { m ->
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), m.range.first, m.range.last + 1)
            }
            italicRegex.findAll(raw, bodyStart).forEach { m ->
                addStyle(SpanStyle(fontStyle = FontStyle.Italic), m.range.first, m.range.last + 1)
            }
            headingRegex.findAll(raw, bodyStart).forEach { m ->
                addStyle(
                    SpanStyle(fontWeight = FontWeight.Bold, fontSize = TextUnit(20f, TextUnitType.Sp)),
                    m.range.first, m.range.last + 1,
                )
            }
        }

        return TransformedText(annotated, OffsetMapping.Identity)
    }
}

// ── Formatting Toolbar — Liquid Glass ────────────────────────────────────────

// Liquid glass palette — slightly luminous tinted glass over the dark surface
private val GlassBase    = Color(0xFF1E1E2A)
private val GlassBorder  = Color(0x33FFFFFF)
private val GlassSheen   = Color(0x0DFFFFFF)

@Composable
private fun FormattingToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onHeading: (level: Int) -> Unit,
    onBullet: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    var headingExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        // ── Heading picker — slides in above toolbar ───────────────────────
        AnimatedVisibility(
            visible = headingExpanded,
            enter = expandVertically(tween(220)) + fadeIn(tween(220)),
            exit  = shrinkVertically(tween(180)) + fadeOut(tween(180)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(GlassBase.copy(alpha = 0.85f), GlassBase.copy(alpha = 0.95f)),
                        ),
                    )
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(1, 2, 3).forEach { level ->
                    HeadingChip(
                        label = "H$level",
                        onClick = {
                            onHeading(level)
                            headingExpanded = false
                        },
                    )
                }
            }
        }

        // ── Thin glass border line ─────────────────────────────────────────
        HorizontalDivider(color = GlassBorder, thickness = 0.5.dp)

        // ── Main toolbar row ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                // Blur layer — simulates frosted glass
                .blur(0.dp), // real blur requires RenderEffect (API 31+); kept as extension point
        ) {
            // Glass background: dark base + subtle vertical sheen
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                GlassBase.copy(alpha = 0.92f),
                                GlassBase.copy(alpha = 0.98f),
                            ),
                        ),
                    ),
            )
            // Top sheen stripe
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(height = 1.dp, width = 0.dp) // hairline sheen
                    .background(GlassSheen),
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolbarButton(icon = Lucide.Bold,      label = "Kalın",       onClick = onBold)
                ToolbarButton(icon = Lucide.Italic,    label = "İtalik",      onClick = onItalic)
                ToolbarButton(icon = Lucide.Underline, label = "Altı çizili", onClick = onUnderline)

                // Heading button — toggles H1/H2/H3 picker
                IconButton(
                    onClick = { headingExpanded = !headingExpanded },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Lucide.Heading2,
                        contentDescription = "Başlık",
                        tint = if (headingExpanded)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }

                ToolbarButton(icon = Lucide.List, label = "Liste", onClick = onBullet)

                Spacer(Modifier.weight(1f))

                ToolbarButton(icon = Lucide.Undo2, label = "Geri al", onClick = onUndo)
                ToolbarButton(icon = Lucide.Redo2, label = "Yinele",  onClick = onRedo)
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
private fun HeadingChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(GlassBorder)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
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