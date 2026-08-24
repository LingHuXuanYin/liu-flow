package com.liuflow.app.util

import java.util.Locale

object TimeFormat {

    /** Format seconds as MM:SS, supports >= 60 minutes. */
    fun mmss(totalSeconds: Int): String {
        val s = totalSeconds.coerceAtLeast(0)
        val m = s / 60
        val r = s % 60
        return String.format(Locale.US, "%02d:%02d", m, r)
    }

    /** Format a number of minutes as "1h 30m" / "45m" / "1.5h". */
    fun friendlyMinutes(minutes: Int): String = when {
        minutes < 60 -> "${minutes}m"
        minutes % 60 == 0 -> "${minutes / 60}h"
        else -> String.format(Locale.US, "%.1fh", minutes / 60.0)
    }

    fun decimalHours(minutes: Int): Double = minutes / 60.0
}
