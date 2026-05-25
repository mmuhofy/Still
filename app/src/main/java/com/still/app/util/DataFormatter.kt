package com.still.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formats epoch millis into a human-readable note date.
 *
 * - Today     → "14:32"
 * - This year → "12 Mar"
 * - Older     → "12.03.24"
 */
fun formatNoteDate(epochMillis: Long): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = epochMillis }

    return when {
        isSameDay(now, target) ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))

        now.get(Calendar.YEAR) == target.get(Calendar.YEAR) ->
            SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(epochMillis))

        else ->
            SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(Date(epochMillis))
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
    a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)