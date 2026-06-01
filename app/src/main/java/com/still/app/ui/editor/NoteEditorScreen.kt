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
        // MarkdownVisualTransformation:
        //   - Hides markers (**bold** → bold, ## H → H)
        //   - Applies corresponding SpanStyles on visible content
        //   - Custom OffsetMapping keeps cursor in the correct raw position
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
// Hides markdown marker characters and applies styles to the visible content.
// Since display text length ≠ raw text length, a custom OffsetMapping is built
// from two parallel arrays so that cursor and selection always point to the
// correct position in the underlying raw TextFieldValue.
//
// Supported syntax:
//   First line       → SemiBold 24sp  (title — always)
//   **text**         → Bold           (markers hidden)
//   __text__         → Underline      (markers hidden — processed before _)
//   _text_           → Italic         (markers hidden)
//   # / ## / ### H   → Heading style  (prefix hidden)

private class MarkdownVisualTransformation : VisualTransformation {

    // Heading: match only the prefix (# + space) — no need to match the full line
    private val headingPrefixRegex = Regex("""^(#{1,3}) """, RegexOption.MULTILINE)
    private val boldRegex          = Regex("""\*\*(.*?)\*\*""")
    private val italicRegex        = Regex("""(?<!_)_(?!_)(.*?)(?<!_)_(?!_)""")
    private val underlineRegex     = Regex("""__(.*?)__""")

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val firstNewline = raw.indexOf('\n')
        val titleEnd     = if (firstNewline == -1) raw.length else firstNewline
        val bodyStart    = if (firstNewline == -1) raw.length else firstNewline + 1

        // raw content ranges with their styles (inclusive IntRange)
        val styleRanges   = mutableListOf<Pair<IntRange, SpanStyle>>()
        // raw ranges of characters to hide
        val hiddenRanges  = mutableListOf<IntRange>()
        // full raw ranges already owned by an inline span (overlap guard)
        val claimedRanges = mutableListOf<IntRange>()

        // Try to register an inline span (bold / italic / underline).
        // markerLen = number of marker chars on each side (**=2, _=1, __=2).
        fun tryInline(matchRange: IntRange, markerLen: Int, style: SpanStyle) {
            val cs = matchRange.first + markerLen
            val ce = matchRange.last  - markerLen
            if (cs > ce) return // empty content
            if (claimedRanges.any { it.first <= matchRange.last && it.last >= matchRange.first }) return
            claimedRanges += matchRange
            hiddenRanges  += matchRange.first until cs       // opening marker
            hiddenRanges  += (ce + 1)..matchRange.last       // closing marker
            styleRanges   += (cs..ce) to style
        }

        if (bodyStart < raw.length) {
            // Underline __ BEFORE italic _ (same order as MarkdownRenderer)
            underlineRegex.findAll(raw, bodyStart).forEach { m ->
                tryInline(m.range, 2, SpanStyle(textDecoration = TextDecoration.Underline))
            }
            boldRegex.findAll(raw, bodyStart).forEach { m ->
                tryInline(m.range, 2, SpanStyle(fontWeight = FontWeight.Bold))
            }
            italicRegex.findAll(raw, bodyStart).forEach { m ->
                tryInline(m.range, 1, SpanStyle(fontStyle = FontStyle.Italic))
            }

            // Headings — processed independently (line-level, coexists with inline spans)
            headingPrefixRegex.findAll(raw, bodyStart).forEach { m ->
                val prefixEnd = m.range.last + 1   // first char of heading content
                val hashLen   = m.groupValues[1].length
                val nextNl    = raw.indexOf('\n', prefixEnd)
                val lineEnd   = if (nextNl == -1) raw.length - 1 else nextNl - 1
                if (prefixEnd > lineEnd) return@forEach // no content after prefix
                hiddenRanges += m.range.first until prefixEnd
                val style = when (hashLen) {
                    1    -> SpanStyle(fontWeight = FontWeight.Bold,     fontSize = TextUnit(22f, TextUnitType.Sp))
                    2    -> SpanStyle(fontWeight = FontWeight.Bold,     fontSize = TextUnit(20f, TextUnitType.Sp))
                    else -> SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = TextUnit(18f, TextUnitType.Sp))
                }
                styleRanges += (prefixEnd..lineEnd) to style
            }
        }

        // Build boolean hidden array for O(1) lookup
        val hidden = BooleanArray(raw.length)
        hiddenRanges.forEach { range ->
            for (i in range) if (i in hidden.indices) hidden[i] = true
        }

        // Build offset mapping arrays in a single O(n) pass
        val o2t = IntArray(raw.length + 1)        // originalToTransformed
        val t2o = ArrayList<Int>(raw.length + 1)  // transformedToOriginal

        var di = 0
        for (ri in raw.indices) {
            o2t[ri] = di
            if (!hidden[ri]) {
                t2o += ri
                di++
            }
        }
        o2t[raw.length] = di
        t2o += raw.length  // sentinel so offset == displayLength maps to raw.length

        // Build display string
        val display = buildString(di) {
            for (ri in raw.indices) if (!hidden[ri]) append(raw[ri])
        }

        // Build annotated display string
        val titleDisplayEnd = o2t[titleEnd]
        val annotated = buildAnnotatedString {
            append(display)
            // First line always rendered as title
            if (titleDisplayEnd > 0) {
                addStyle(
                    SpanStyle(fontWeight = FontWeight.SemiBold, fontSize = TextUnit(24f, TextUnitType.Sp)),
                    0,
                    titleDisplayEnd,
                )
            }
            // Markdown spans — convert raw ranges to display ranges via o2t
            styleRanges.forEach { (rawRange, style) ->
                val ds = o2t[rawRange.first.coerceIn(0, raw.length)]
                val de = o2t[(rawRange.last + 1).coerceIn(0, raw.length)]
                if (ds < de) addStyle(style, ds, de)
            }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                o2t[offset.coerceIn(0, raw.length)]
            override fun transformedToOriginal(offset: Int): Int =
                t2o.getOrElse(offset.coerceIn(0, t2o.size - 1)) { raw.length }
        }

        return TransformedText(annotated, mapping)
    }
}

// ── Formatting Toolbar — Liquid Glass ────────────────────────────────────────

@Composable
private fun FormattingToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onUnderline: () -> Unit,
    onHeading: (Int) -> Unit,
    onBullet: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
) {
    var headingMenuExpanded by remember { mutableStateOf(false) }

    // Liquid glass: semi-transparent surface + top shimmer + gold accent border
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.88f))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.07f),
                        Color.Transparent,
                    ),
                )
            ),
    ) {
        Column {
            // Thin gold accent line — the "glass edge"
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
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

                // Heading button — tap opens H1 / H2 / H3 menu
                Box {
                    ToolbarButton(
                        icon    = Lucide.Heading2,
                        label   = "Başlık",
                        onClick = { headingMenuExpanded = true },
                    )
                    DropdownMenu(
                        expanded = headingMenuExpanded,
                        onDismissRequest = { headingMenuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "H1",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                )
                            },
                            onClick = { onHeading(1); headingMenuExpanded = false },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "H2",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                )
                            },
                            onClick = { onHeading(2); headingMenuExpanded = false },
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "H3",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                )
                            },
                            onClick = { onHeading(3); headingMenuExpanded = false },
                        )
                    }
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