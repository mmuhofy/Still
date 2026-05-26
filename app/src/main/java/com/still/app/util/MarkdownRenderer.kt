package com.still.app.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit

/**
 * Converts markdown plain text → [AnnotatedString] for display only.
 * Storage stays as raw markdown — Room schema unchanged.
 *
 * Supported syntax:
 *   **bold**       → Bold
 *   __underline__  → Underline   (double underscore — checked BEFORE single)
 *   _italic_       → Italic      (single underscore)
 *   ## Heading     → Bold + headingFontSize
 *   - item         → • item
 */
object MarkdownRenderer {

    // Order matters: underline (__) must be matched before italic (_)
    private val UNDERLINE_REGEX = Regex("""__(.*?)__""")
    private val BOLD_REGEX = Regex("""\*\*(.*?)\*\*""")
    private val ITALIC_REGEX = Regex("""(?<!_)_(?!_)(.*?)(?<!_)_(?!_)""")
    private val HEADING_REGEX = Regex("""^#{1,3} (.+)""")
    private val BULLET_REGEX = Regex("""^- (.+)""")

    fun render(
        raw: String,
        headingFontSize: TextUnit,
        bodyFontSize: TextUnit,
    ): AnnotatedString = buildAnnotatedString {
        val lines = raw.lines()
        lines.forEachIndexed { idx, rawLine ->
            val lineStart = length

            when {
                HEADING_REGEX.matches(rawLine) -> {
                    val content = HEADING_REGEX.find(rawLine)!!.groupValues[1]
                    appendInlineStyled(content)
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontSize = headingFontSize),
                        lineStart, length,
                    )
                }
                BULLET_REGEX.matches(rawLine) -> {
                    val content = BULLET_REGEX.find(rawLine)!!.groupValues[1]
                    appendInlineStyled("• $content")
                }
                else -> appendInlineStyled(rawLine)
            }

            if (idx < lines.lastIndex) append('\n')
        }
    }

    private fun AnnotatedString.Builder.appendInlineStyled(text: String) {
        data class Span(val start: Int, val end: Int, val innerText: String, val style: SpanStyle)

        val spans = mutableListOf<Span>()

        // Underline first — so __ is consumed before _ can match
        UNDERLINE_REGEX.findAll(text).forEach { m ->
            spans += Span(m.range.first, m.range.last + 1, m.groupValues[1],
                SpanStyle(textDecoration = TextDecoration.Underline))
        }
        BOLD_REGEX.findAll(text).forEach { m ->
            if (spans.none { s -> m.range.first in s.start until s.end }) {
                spans += Span(m.range.first, m.range.last + 1, m.groupValues[1],
                    SpanStyle(fontWeight = FontWeight.Bold))
            }
        }
        ITALIC_REGEX.findAll(text).forEach { m ->
            if (spans.none { s -> m.range.first in s.start until s.end }) {
                spans += Span(m.range.first, m.range.last + 1, m.groupValues[1],
                    SpanStyle(fontStyle = FontStyle.Italic))
            }
        }

        spans.sortBy { it.start }

        var cursor = 0
        for (span in spans) {
            if (span.start > cursor) append(text.substring(cursor, span.start))
            val spanStart = length
            append(span.innerText)
            addStyle(span.style, spanStart, length)
            cursor = span.end
        }
        if (cursor < text.length) append(text.substring(cursor))
    }
}