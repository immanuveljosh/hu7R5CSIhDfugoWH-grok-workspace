package com.pulse.dialer

import android.media.MediaRecorder
import com.pulse.dialer.data.prefs.SettingsRepository
import com.pulse.dialer.recording.AudioSourceProbe
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AudioSourceProbeTest {
    private val probe = AudioSourceProbe()

    @Test
    fun mapsOfficialSourcesOnly() {
        assertEquals(MediaRecorder.AudioSource.VOICE_CALL, probe.sourceConstant(SettingsRepository.SOURCE_VOICE_CALL))
        assertEquals(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            probe.sourceConstant(SettingsRepository.SOURCE_VOICE_COMMUNICATION),
        )
        assertEquals(MediaRecorder.AudioSource.MIC, probe.sourceConstant(SettingsRepository.SOURCE_MIC))
        assertNull(probe.sourceConstant("VOICE_UPLINK"))
        assertNull(probe.sourceConstant(null))
    }

    @Test
    fun unsupportedCopyIsExact() {
        assertEquals(
            "Call recording is not supported on this device.",
            SettingsRepository.UNSUPPORTED_MESSAGE,
        )
    }
}
