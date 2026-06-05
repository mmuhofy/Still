package com.still.app.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Marker-visible markdown renderer.
 *
 * Markers stay in the string (rawText == annotatedString.text, offsets are 1:1).
 * Markers are styled as faint/small so they visually fade into the background.
 * Formatted content gets full styling applied.
 *
 * Supported syntax:
 *   **bold**       → markers faint, inner text bold
 *   __underline__  → markers faint, inner text underlined  (checked before _italic_)
 *   _italic_       → markers faint, inner text italic
 *   ## Heading     → ## faint + small, rest bold + headingFontSize
 *   - item         → - faint, rest normal
 */
object MarkdownRenderer {

    // Marker style — visible but faint so they don't distract
    private val MARKER_STYLE = SpanStyle(
        color    = Color.Unspecified.copy(alpha = 0f), // overridden at call site with theme color
        fontSize = 8.sp,
    )

    private val BOLD_REGEX      = Regex("""\*\*(.*?)\*\*""")
    private val UNDERLINE_REGEX = Regex("""__(.*?)__""")
    private val ITALIC_REGEX    = Regex("""(?<![_*])_(?!_)(.*?)(?<![_*])_(?![_*])""")
    private val HEADING_REGEX   = Regex("""^(#{1,3}) (.+)""")
    private val BULLET_REGEX    = Regex("""^(- )(.+)""")

    fun render(
        raw: String,
        headingFontSize: TextUnit,
        bodyFontSize: TextUnit,
        markerColor: Color,       // pass theme onBackground with low alpha
    ): AnnotatedString = buildAnnotatedString {
        val faintMarker = MARKER_STYLE.copy(color = markerColor)

        raw.lines().forEachIndexed { idx, line ->
            val lineStart = length

            val headingMatch = HEADING_REGEX.find(line)
            val bulletMatch  = BULLET_REGEX.find(line)

            when {
                headingMatch != null -> {
                    val hashes  = headingMatch.groupValues[1] // "##"
                    val content = headingMatch.groupValues[2]

                    // Faint hashes
                    val hashStart = length
                    append(hashes)
                    addStyle(faintMarker, hashStart, length)

                    // Space
                    append(" ")

                    // Bold heading content
                    val contentStart = length
                    append(content)
                    addStyle(
                        SpanStyle(fontWeight = FontWeight.Bold, fontSize = headingFontSize),
                        contentStart, length,
                    )
                }

                bulletMatch != null -> {
                    // Faint "- "
                    val dashStart = length
                    append(bulletMatch.groupValues[1])
                    addStyle(faintMarker, dashStart, length)

                    // Normal content with inline styles
                    appendInlineStyled(bulletMatch.groupValues[2], faintMarker)
                }

                else -> appendInlineStyled(line, faintMarker)
            }

            if (idx < raw.lines().lastIndex) append('\n')
        }
    }

    private fun AnnotatedString.Builder.appendInlineStyled(
        text: String,
        faintMarker: SpanStyle,
    ) {
        data class Span(
            val start: Int, val end: Int,
            val markerLen: Int,          // length of opening marker (same for closing)
            val contentStyle: SpanStyle,
        )

        val spans = mutableListOf<Span>()

        // Underline before italic — __ must not be consumed as two _
        UNDERLINE_REGEX.findAll(text).forEach { m ->
            spans += Span(
                m.range.first, m.range.last + 1,
                markerLen    = 2,
                contentStyle = SpanStyle(textDecoration = TextDecoration.Underline),
            )
        }
        BOLD_REGEX.findAll(text).forEach { m ->
            if (spans.none { m.range.first in it.start until it.end }) {
                spans += Span(
                    m.range.first, m.range.last + 1,
                    markerLen    = 2,
                    contentStyle = SpanStyle(fontWeight = FontWeight.Bold),
                )
            }
        }
        ITALIC_REGEX.findAll(text).forEach { m ->
            if (spans.none { m.range.first in it.start until it.end }) {
                spans += Span(
                    m.range.first, m.range.last + 1,
                    markerLen    = 1,
                    contentStyle = SpanStyle(fontStyle = FontStyle.Italic),
                )
            }
        }

        spans.sortBy { it.start }

        var cursor = 0
        for (span in spans) {
            // Text before this span
            if (span.start > cursor) append(text.substring(cursor, span.start))

            val fullMatch = text.substring(span.start, span.end)
            val mLen      = span.markerLen
            val inner     = fullMatch.substring(mLen, fullMatch.length - mLen)

            // Opening marker — faint
            val openStart = length
            append(fullMatch.substring(0, mLen))
            addStyle(faintMarker, openStart, length)

            // Inner content — styled
            val innerStart = length
            append(inner)
            addStyle(span.contentStyle, innerStart, length)

            // Closing marker — faint
            val closeStart = length
            append(fullMatch.substring(fullMatch.length - mLen))
            addStyle(faintMarker, closeStart, length)

            cursor = span.end
        }
        if (cursor < text.length) append(text.substring(cursor))
    }
}