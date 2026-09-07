package com.pulse.dialer.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max

object TimeFormat {
    private val fileStamp: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss", Locale.US)

    fun recordingFileName(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
        val stamp = instant.atZone(zone).format(fileStamp)
        return "Call_${stamp}.m4a"
    }

    fun isValidRecordingFileName(name: String): Boolean =
        Regex("""^Call_\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2}\.m4a$""").matches(name)

    fun duration(ms: Long): String {
        if (ms <= 0L || ms == Long.MIN_VALUE) return "0:00"
        val total = max(0L, ms / 1000L)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(Locale.US, h, m, s)
        else "%d:%02d".format(Locale.US, m, s)
    }

    fun bytes(value: Long): String = when {
        value < 1024 -> "$value B"
        value < 1024 * 1024 -> "%.1f KB".format(Locale.US, value / 1024.0)
        else -> "%.1f MB".format(Locale.US, value / (1024.0 * 1024.0))
    }
}
