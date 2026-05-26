package com.still.app.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Converts a markdown-flavoured plain string into an [AnnotatedString] with
 * inline spans applied. Supported syntax:
 *
 *   **bold**        → FontWeight.Bold
 *   _italic_        → FontStyle.Italic
 *   __underline__   → TextDecoration.Underline
 *   ## Heading      → FontWeight.Bold + larger fontSize (via caller's titleStyle)
 *   - item          → bullet character prepended (• item)
 *
 * The raw text stored in Room stays as-is; this function is display-only.
 */
object MarkdownRenderer {

    private val BOLD_REGEX = Regex("""\*\*(.*?)\*\*""")
    private val ITALIC_REGEX = Regex("""(?<!\*|_)_(?!_)(.*?)(?<!\*|_)_(?!_)""")
    private val UNDERLINE_REGEX = Regex("""__(.*?)__""")
    private val HEADING_REGEX = Regex("""^#{1,3} (.+)""")
    private val BULLET_REGEX = Regex("""^- (.+)""")

    fun render(
        raw: String,
        headingFontSize: androidx.compose.ui.unit.TextUnit,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
    ): AnnotatedString = buildAnnotatedString {

        val lines = raw.lines()

        lines.forEachIndexed { lineIndex, rawLine ->
            // ── Line-level transforms ─────────────────────────────────────────
            val (line, lineStyle) = when {
                HEADING_REGEX.matches(rawLine) -> {
                    val content = HEADING_REGEX.find(rawLine)!!.groupValues[1]
                    content to SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = headingFontSize,
                    )
                }
                BULLET_REGEX.matches(rawLine) -> {
                    val content = BULLET_REGEX.find(rawLine)!!.groupValues[1]
                    "• $content" to null
                }
                else -> rawLine to null
            }

            val lineStart = length

            // ── Inline spans ──────────────────────────────────────────────────
            appendInlineStyled(line, bodyFontSize)

            val lineEnd = length

            // Apply line-level style over the whole line
            if (lineStyle != null) {
                addStyle(lineStyle, lineStart, lineEnd)
            }

            if (lineIndex < lines.lastIndex) append('\n')
        }
    }

    /**
     * Appends [text] to the builder applying bold / italic / underline spans.
     * Order matters: underline (__) is matched before italic (_).
     */
    private fun AnnotatedString.Builder.appendInlineStyled(
        text: String,
        bodyFontSize: androidx.compose.ui.unit.TextUnit,
    ) {
        // Collect all span regions so we can walk through the string once
        data class Span(val start: Int, val end: Int, val style: SpanStyle, val innerText: String)

        val spans = mutableListOf<Span>()

        // Bold — must come before italic to avoid partial matches
        BOLD_REGEX.findAll(text).forEach { m ->
            spans += Span(m.range.first, m.range.last + 1, SpanStyle(fontWeight = FontWeight.Bold), m.groupValues[1])
        }
        // Underline — must come before single-underscore italic
        UNDERLINE_REGEX.findAll(text).forEach { m ->
            // Skip if already covered by bold
            if (spans.none { it.start == m.range.first }) {
                spans += Span(m.range.first, m.range.last + 1, SpanStyle(textDecoration = TextDecoration.Underline), m.groupValues[1])
            }
        }
        // Italic
        ITALIC_REGEX.findAll(text).forEach { m ->
            if (spans.none { it.start == m.range.first }) {
                spans += Span(m.range.first, m.range.last + 1, SpanStyle(fontStyle = FontStyle.Italic), m.groupValues[1])
            }
        }

        spans.sortBy { it.start }

        var cursor = 0
        spans.forEach { span ->
            if (span.start > cursor) append(text.substring(cursor, span.start))
            val innerStart = length
            append(span.innerText)
            addStyle(span.style, innerStart, length)
            cursor = span.end
        }
        if (cursor < text.length) append(text.substring(cursor))
    }
}