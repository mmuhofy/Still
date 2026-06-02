package com.still.app.ui.editor

import androidx.compose.foundation.background
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
import com.still.app.ui.components.StillDropdownMenu
import com.still.app.ui.components.StillDropdownMenuItem

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
                        StillDropdownMenu(
                            expanded = overflowExpanded,
                            onDismissRequest = { overflowExpanded = false },
                        ) {
                            StillDropdownMenuItem(
                                text = "Sil",
                                color = MaterialTheme.colorScheme.error,
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
                onHeading   = { level -> viewModel.onEvent(NoteEditorEvent.ApplyHeading(level)) },
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
// Hides markdown markers (**, __, _, #, - ) and applies styles to inner text.
// Custom OffsetMapping keeps cursor at correct raw position at all times.

private class MarkdownVisualTransformation : VisualTransformation {

    private sealed class Segment {
        data class Visible(val rawStart: Int, val rawEnd: Int, val style: SpanStyle? = null) : Segment()
        data class Hidden(val rawStart: Int, val rawEnd: Int) : Segment()
    }

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val segments = buildSegments(raw)

        val rawToDisplay = IntArray(raw.length + 1)
        val displayToRaw = mutableListOf<Int>()
        val displayBuilder = StringBuilder()

        for (seg in segments) {
            when (seg) {
                is Segment.Hidden -> {
                    val displayPos = displayBuilder.length
                    for (i in seg.rawStart until seg.rawEnd) rawToDisplay[i] = displayPos
                }
                is Segment.Visible -> {
                    for (i in seg.rawStart until seg.rawEnd) {
                        rawToDisplay[i] = displayBuilder.length
                        displayToRaw.add(i)
                        displayBuilder.append(raw[i])
                    }
                }
            }
        }
        rawToDisplay[raw.length] = displayBuilder.length
        displayToRaw.add(raw.length)

        val annotated = buildAnnotatedString {
            append(displayBuilder.toString())
            for (seg in segments) {
                if (seg is Segment.Visible && seg.style != null) {
                    val dStart = rawToDisplay[seg.rawStart]
                    val dEnd   = rawToDisplay[seg.rawEnd.coerceAtMost(raw.length)]
                    if (dStart < dEnd) addStyle(seg.style, dStart, dEnd)
                }
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                rawToDisplay[offset.coerceIn(0, raw.length)]
            override fun transformedToOriginal(offset: Int): Int =
                displayToRaw.getOrElse(offset.coerceIn(0, displayToRaw.lastIndex)) { raw.length }
        }

        return TransformedText(annotated, offsetMapping)
    }

    private fun buildSegments(raw: String): List<Segment> {
        val result = mutableListOf<Segment>()
        val lines = raw.lines()
        var rawCursor = 0

        lines.forEachIndexed { lineIndex, line ->
            val lineStart = rawCursor

            if (lineIndex == 0) {
                // Title line — no markdown parsing, always SemiBold 24sp
                if (line.isNotEmpty()) {
                    result += Segment.Visible(
                        rawStart = lineStart,
                        rawEnd   = lineStart + line.length,
                        style    = SpanStyle(
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = TextUnit(24f, TextUnitType.Sp),
                        ),
                    )
                }
            } else {
                val headingMatch = Regex("""^(#{1,3}) """).find(line)
                val isBullet     = line.startsWith("- ")

                when {
                    headingMatch != null -> {
                        val prefixLen = headingMatch.value.length
                        val fs = when (headingMatch.groupValues[1].length) {
                            1    -> TextUnit(22f, TextUnitType.Sp)
                            2    -> TextUnit(20f, TextUnitType.Sp)
                            else -> TextUnit(18f, TextUnitType.Sp)
                        }
                        // Hide "# " prefix
                        result += Segment.Hidden(lineStart, lineStart + prefixLen)
                        if (prefixLen < line.length) {
                            result += Segment.Visible(
                                rawStart = lineStart + prefixLen,
                                rawEnd   = lineStart + line.length,
                                style    = SpanStyle(fontWeight = FontWeight.Bold, fontSize = fs),
                            )
                        }
                    }
                    isBullet -> {
                        // Hide "- " and parse inline markdown in the rest
                        result += Segment.Hidden(lineStart, lineStart + 2)
                        parseInline(raw, lineStart + 2, lineStart + line.length, result)
                    }
                    else -> parseInline(raw, lineStart, lineStart + line.length, result)
                }
            }

            rawCursor += line.length
            // Newline character — always visible, no style
            if (rawCursor < raw.length) {
                result += Segment.Visible(rawStart = rawCursor, rawEnd = rawCursor + 1)
                rawCursor += 1
            }
        }

        return result
    }

    private val underlineRe = Regex("""__(.*?)__""")
    private val boldRe      = Regex("""\*\*(.*?)\*\*""")
    private val italicRe    = Regex("""(?<!_)_(?!_)(.*?)(?<!_)_(?!_)""")

    private fun parseInline(
        raw: String,
        regionStart: Int,
        regionEnd: Int,
        out: MutableList<Segment>,
    ) {
        if (regionStart >= regionEnd) return
        val slice = raw.substring(regionStart, regionEnd)

        data class Span(
            val start: Int, val end: Int,
            val innerStart: Int, val innerEnd: Int,
            val style: SpanStyle,
        )

        val spans = mutableListOf<Span>()

        underlineRe.findAll(slice).forEach { m ->
            spans += Span(m.range.first, m.range.last + 1,
                m.range.first + 2, m.range.last - 1,
                SpanStyle(textDecoration = TextDecoration.Underline))
        }
        boldRe.findAll(slice).forEach { m ->
            if (spans.none { s -> m.range.first in s.start until s.end })
                spans += Span(m.range.first, m.range.last + 1,
                    m.range.first + 2, m.range.last - 1,
                    SpanStyle(fontWeight = FontWeight.Bold))
        }
        italicRe.findAll(slice).forEach { m ->
            if (spans.none { s -> m.range.first in s.start until s.end })
                spans += Span(m.range.first, m.range.last + 1,
                    m.range.first + 1, m.range.last,
                    SpanStyle(fontStyle = FontStyle.Italic))
        }

        spans.sortBy { it.start }

        var cursor = 0
        for (span in spans) {
            if (span.start > cursor)
                out += Segment.Visible(regionStart + cursor, regionStart + span.start)
            out += Segment.Hidden(regionStart + span.start, regionStart + span.innerStart)
            if (span.innerStart < span.innerEnd)
                out += Segment.Visible(regionStart + span.innerStart, regionStart + span.innerEnd, span.style)
            out += Segment.Hidden(regionStart + span.innerEnd, regionStart + span.end)
            cursor = span.end
        }
        if (cursor < slice.length)
            out += Segment.Visible(regionStart + cursor, regionEnd)
    }
}

// ── Formatting Toolbar — Liquid Glass ────────────────────────────────────────

private val GlassBase   = Color(0xFF1E1E2A)
private val GlassBorder = Color(0x33FFFFFF)

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
    var headingMenuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .background(
                brush = Brush.verticalGradient(
                    listOf(GlassBase.copy(alpha = 0.92f), GlassBase.copy(alpha = 0.98f)),
                ),
            ),
    ) {
        HorizontalDivider(color = GlassBorder, thickness = 0.5.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolbarButton(icon = Lucide.Bold,      label = "Kalın",       onClick = onBold)
            ToolbarButton(icon = Lucide.Italic,    label = "İtalik",      onClick = onItalic)
            ToolbarButton(icon = Lucide.Underline, label = "Altı çizili", onClick = onUnderline)

            Box {
                IconButton(
                    onClick = { headingMenuExpanded = true },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(
                        imageVector = Lucide.Heading2,
                        contentDescription = "Başlık",
                        tint = if (headingMenuExpanded)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                StillDropdownMenu(
                    expanded = headingMenuExpanded,
                    onDismissRequest = { headingMenuExpanded = false },
                ) {
                    listOf("H1" to 1, "H2" to 2, "H3" to 3).forEach { (label, level) ->
                        StillDropdownMenuItem(
                            text = label,
                            onClick = {
                                onHeading(level)
                                headingMenuExpanded = false
                            },
                        )
                    }
                }
            }

            ToolbarButton(icon = Lucide.List,  label = "Liste",    onClick = onBullet)

            Spacer(Modifier.weight(1f))

            ToolbarButton(icon = Lucide.Undo2, label = "Geri al",  onClick = onUndo)
            ToolbarButton(icon = Lucide.Redo2, label = "Yinele",   onClick = onRedo)
            Spacer(Modifier.width(4.dp))
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