package com.still.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
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
import com.composables.icons.lucide.PinOff
import com.composables.icons.lucide.Redo2
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.Underline
import com.composables.icons.lucide.Undo2
import com.still.app.ui.components.StillDropdownMenu
import com.still.app.ui.components.StillDropdownMenuItem

private val SheetBg     = Color(0xFF16161F)
private val SheetBorder = Color(0x1AFFFFFF)
private val GlassBase   = Color(0xFF1E1E2A)
private val GlassBorder = Color(0x33FFFFFF)
private val Gold        = Color(0xFFB8A369)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val titleFocusRequester = remember { FocusRequester() }
    val bodyFocusRequester  = remember { FocusRequester() }

    LaunchedEffect(state.isDeleted) {
        if (state.isDeleted) onBack()
    }

    LaunchedEffect(Unit) {
        if (state.noteId == -1L) titleFocusRequester.requestFocus()
    }

    // Split raw text into title (first line) and body (rest)
    val rawText   = state.content.text
    val newlineIdx = rawText.indexOf('\n')
    val titleText  = if (newlineIdx == -1) rawText else rawText.substring(0, newlineIdx)
    val bodyText   = if (newlineIdx == -1) "" else rawText.substring(newlineIdx + 1)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector        = Lucide.ArrowLeft,
                            contentDescription = "Geri",
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                title = {},
                actions = {
                    IconButton(onClick = { showSheet = true }) {
                        Icon(
                            imageVector        = Lucide.EllipsisVertical,
                            contentDescription = "Daha fazla",
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
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
            // ── Title field ───────────────────────────────────────────────────
            BasicTextField(
                value         = TextFieldValue(
                    text      = titleText,
                    selection = state.content.selection.let { sel ->
                        // Clamp selection to title range
                        TextRange(
                            sel.start.coerceIn(0, titleText.length),
                            sel.end.coerceIn(0, titleText.length),
                        )
                    },
                ),
                onValueChange = { tfv ->
                    // Merge title change back into full raw text
                    val newRaw = if (bodyText.isEmpty()) tfv.text else "${tfv.text}\n$bodyText"
                    viewModel.onEvent(
                        NoteEditorEvent.ContentChanged(
                            TextFieldValue(
                                text      = newRaw,
                                selection = TextRange(tfv.selection.start, tfv.selection.end),
                            )
                        )
                    )
                },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp)
                    .focusRequester(titleFocusRequester),
                textStyle     = MaterialTheme.typography.headlineSmall.copy(
                    color      = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                ),
                cursorBrush   = SolidColor(Gold),
                singleLine    = false,
                decorationBox = { inner ->
                    Box {
                        if (titleText.isEmpty()) {
                            Text(
                                text  = "Başlık",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                            )
                        }
                        inner()
                    }
                },
            )

            Spacer(Modifier.height(12.dp))

            // ── Gold gradient divider ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Gold.copy(alpha = 0.6f),
                                Gold.copy(alpha = 0.15f),
                                Color.Transparent,
                            )
                        )
                    )
            )

            Spacer(Modifier.height(14.dp))

            // ── Body field ────────────────────────────────────────────────────
            // Append ghost text to body for rendering — ghost is styled via VisualTransformation
            val ghostText  = state.ghostText
            val bodyWithGhost = bodyText + ghostText
            val bodyTfv = TextFieldValue(
                text      = bodyWithGhost,
                selection = state.content.selection.let { sel ->
                    val offset = if (newlineIdx == -1) 0 else newlineIdx + 1
                    val start  = (sel.start - offset).coerceIn(0, bodyText.length)
                    val end    = (sel.end   - offset).coerceIn(0, bodyText.length)
                    TextRange(start, end)
                },
            )
            val bodyTransformation = remember(ghostText) {
                MarkdownVisualTransformation(ghostLength = ghostText.length)
            }

            BasicTextField(
                value         = bodyTfv,
                onValueChange = { tfv ->
                    // If user tapped inside ghost region → accept ghost
                    if (ghostText.isNotEmpty() && tfv.selection.start > bodyText.length) {
                        viewModel.onEvent(NoteEditorEvent.AcceptGhost)
                        return@BasicTextField
                    }
                    // Strip ghost suffix before merging — ghost must not enter raw storage
                    val realBody = if (ghostText.isNotEmpty() && tfv.text.endsWith(ghostText))
                        tfv.text.dropLast(ghostText.length)
                    else
                        tfv.text
                    // Merge body change back into full raw text
                    val newRaw = if (realBody.isEmpty() && titleText.isEmpty()) ""
                                 else "$titleText\n$realBody"
                    val offset = if (newlineIdx == -1) 0 else newlineIdx + 1
                    viewModel.onEvent(
                        NoteEditorEvent.ContentChanged(
                            TextFieldValue(
                                text      = newRaw,
                                selection = TextRange(
                                    (tfv.selection.start + offset).coerceAtMost(newRaw.length),
                                    (tfv.selection.end   + offset).coerceAtMost(newRaw.length),
                                ),
                            )
                        )
                    )
                },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 80.dp)
                    .focusRequester(bodyFocusRequester),
                textStyle     = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                cursorBrush          = SolidColor(Gold),
                visualTransformation = bodyTransformation,
                decorationBox = { inner ->
                    Box {
                        if (bodyText.isEmpty()) {
                            Text(
                                text  = "Yazmaya başla...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            )
                        }
                        inner()
                    }
                },
            )
        }
    }

    // ── Bottom sheet ──────────────────────────────────────────────────────────
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState       = sheetState,
            containerColor   = SheetBg,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(2.dp))
                )
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp),
            ) {
                SheetItem(
                    icon    = if (state.isPinned) Lucide.PinOff else Lucide.Pin,
                    label   = if (state.isPinned) "Sabitlemeyi kaldır" else "Sabitle",
                    tint    = MaterialTheme.colorScheme.onSurface,
                    onClick = {
                        viewModel.onEvent(NoteEditorEvent.TogglePin)
                        showSheet = false
                    },
                )
                HorizontalDivider(
                    modifier  = Modifier.padding(horizontal = 20.dp),
                    color     = SheetBorder,
                    thickness = 0.5.dp,
                )
                SheetItem(
                    icon    = Lucide.Trash2,
                    label   = "Sil",
                    tint    = MaterialTheme.colorScheme.error,
                    onClick = {
                        showSheet = false
                        viewModel.onEvent(NoteEditorEvent.DeleteNote)
                    },
                )
            }
        }
    }
}

// ── Sheet Item ────────────────────────────────────────────────────────────────

@Composable
private fun SheetItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}

// ── Markdown Visual Transformation ────────────────────────────────────────────

private class MarkdownVisualTransformation(
    private val ghostLength: Int = 0,
) : VisualTransformation {

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
            // Ghost text — italic, muted, non-interactive feel
            if (ghostLength > 0) {
                val ghostDisplayStart = rawToDisplay[(raw.length - ghostLength).coerceAtLeast(0)]
                val ghostDisplayEnd   = displayBuilder.length
                if (ghostDisplayStart < ghostDisplayEnd) {
                    addStyle(
                        SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color     = Color(0x60B8A369), // muted gold
                        ),
                        ghostDisplayStart,
                        ghostDisplayEnd,
                    )
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
        val result    = mutableListOf<Segment>()
        val lines     = raw.lines()
        var rawCursor = 0

        lines.forEachIndexed { _, line ->
            val lineStart    = rawCursor
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
                    result += Segment.Hidden(lineStart, lineStart + prefixLen)
                    if (prefixLen < line.length)
                        result += Segment.Visible(lineStart + prefixLen, lineStart + line.length,
                            SpanStyle(fontWeight = FontWeight.Bold, fontSize = fs))
                }
                isBullet -> {
                    result += Segment.Hidden(lineStart, lineStart + 2)
                    parseInline(raw, lineStart + 2, lineStart + line.length, result)
                }
                else -> parseInline(raw, lineStart, lineStart + line.length, result)
            }

            rawCursor += line.length
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

    private fun parseInline(raw: String, regionStart: Int, regionEnd: Int, out: MutableList<Segment>) {
        if (regionStart >= regionEnd) return
        val slice = raw.substring(regionStart, regionEnd)
        data class Span(val start: Int, val end: Int, val innerStart: Int, val innerEnd: Int, val style: SpanStyle)
        val spans = mutableListOf<Span>()
        underlineRe.findAll(slice).forEach { m ->
            spans += Span(m.range.first, m.range.last + 1, m.range.first + 2, m.range.last - 1,
                SpanStyle(textDecoration = TextDecoration.Underline))
        }
        boldRe.findAll(slice).forEach { m ->
            if (spans.none { s -> m.range.first in s.start until s.end })
                spans += Span(m.range.first, m.range.last + 1, m.range.first + 2, m.range.last - 1,
                    SpanStyle(fontWeight = FontWeight.Bold))
        }
        italicRe.findAll(slice).forEach { m ->
            if (spans.none { s -> m.range.first in s.start until s.end })
                spans += Span(m.range.first, m.range.last + 1, m.range.first + 1, m.range.last,
                    SpanStyle(fontStyle = FontStyle.Italic))
        }
        spans.sortBy { it.start }
        var cursor = 0
        for (span in spans) {
            if (span.start > cursor) out += Segment.Visible(regionStart + cursor, regionStart + span.start)
            out += Segment.Hidden(regionStart + span.start, regionStart + span.innerStart)
            if (span.innerStart < span.innerEnd)
                out += Segment.Visible(regionStart + span.innerStart, regionStart + span.innerEnd, span.style)
            out += Segment.Hidden(regionStart + span.innerEnd, regionStart + span.end)
            cursor = span.end
        }
        if (cursor < slice.length) out += Segment.Visible(regionStart + cursor, regionEnd)
    }
}

// ── Formatting Toolbar ────────────────────────────────────────────────────────

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
            .background(Brush.verticalGradient(
                listOf(GlassBase.copy(alpha = 0.92f), GlassBase.copy(alpha = 0.98f))
            )),
    ) {
        HorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolbarButton(Lucide.Bold,      "Kalın",       onBold)
            ToolbarButton(Lucide.Italic,    "İtalik",      onItalic)
            ToolbarButton(Lucide.Underline, "Altı çizili", onUnderline)
            Box {
                IconButton(onClick = { headingMenuExpanded = true }, modifier = Modifier.size(40.dp)) {
                    Icon(Lucide.Heading2, "Başlık",
                        tint     = if (headingMenuExpanded) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                }
                StillDropdownMenu(expanded = headingMenuExpanded, onDismissRequest = { headingMenuExpanded = false }) {
                    listOf("H1" to 1, "H2" to 2, "H3" to 3).forEach { (label, level) ->
                        StillDropdownMenuItem(text = label, onClick = { onHeading(level); headingMenuExpanded = false })
                    }
                }
            }
            ToolbarButton(Lucide.List,  "Liste",   onBullet)
            Spacer(Modifier.weight(1f))
            ToolbarButton(Lucide.Undo2, "Geri al", onUndo)
            ToolbarButton(Lucide.Redo2, "Yinele",  onRedo)
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun ToolbarButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
        Icon(icon, label, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}