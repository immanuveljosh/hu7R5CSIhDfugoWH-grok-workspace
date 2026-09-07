package com.pulse.dialer

import com.pulse.dialer.util.TimeFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class TimeFormatTest {
    @Test
    fun recordingFileNameMatchesContract() {
        val instant = Instant.parse("2026-09-07T06:00:45Z")
        val name = TimeFormat.recordingFileName(instant, ZoneId.of("UTC"))
        assertEquals("Call_2026-09-07_06-00-45.m4a", name)
        assertTrue(TimeFormat.isValidRecordingFileName(name))
    }

    @Test
    fun rejectsMalformedNames() {
        assertFalse(TimeFormat.isValidRecordingFileName("recording.m4a"))
        assertFalse(TimeFormat.isValidRecordingFileName("Call_2026-9-7_11-30-45.m4a"))
        assertFalse(TimeFormat.isValidRecordingFileName("Call_2026-09-07_11-30-45.mp3"))
    }

    @Test
    fun durationFormat() {
        assertEquals("0:00", TimeFormat.duration(0))
        assertEquals("0:05", TimeFormat.duration(5_000))
        assertEquals("1:05", TimeFormat.duration(65_000))
        assertEquals("1:01:01", TimeFormat.duration(3_661_000))
        assertEquals("0:00", TimeFormat.duration(-12))
    }
}
