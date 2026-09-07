package com.pulse.dialer.recording

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.pulse.dialer.data.prefs.SettingsRepository
import java.io.File

data class RecordingCapability(
    val supported: Boolean,
    val sourceConstant: Int?,
    val sourceName: String?,
    val nearEndOnly: Boolean,
    val message: String,
)

/**
 * Official-API capability check. Tries VOICE_CALL, then VOICE_COMMUNICATION, then MIC.
 * Does not use hidden APIs, accessibility, or root.
 */
class AudioSourceProbe {
    fun probe(context: Context): RecordingCapability {
        val dir = File(context.cacheDir, "probe").apply { mkdirs() }
        val ranked = listOf(
            Triple(MediaRecorder.AudioSource.VOICE_CALL, SettingsRepository.SOURCE_VOICE_CALL, false),
            Triple(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SettingsRepository.SOURCE_VOICE_COMMUNICATION, true),
            Triple(MediaRecorder.AudioSource.MIC, SettingsRepository.SOURCE_MIC, true),
        )
        for ((source, name, nearEnd) in ranked) {
            if (canRecord(context, dir, source)) {
                val label = when (name) {
                    SettingsRepository.SOURCE_VOICE_CALL -> "Call audio"
                    SettingsRepository.SOURCE_VOICE_COMMUNICATION -> "Voice communication (near-end likely)"
                    else -> "Microphone (near-end only). Two-way call audio is not available on this device."
                }
                return RecordingCapability(true, source, name, nearEnd, label)
            }
        }
        return RecordingCapability(
            supported = false,
            sourceConstant = null,
            sourceName = null,
            nearEndOnly = true,
            message = SettingsRepository.UNSUPPORTED_MESSAGE,
        )
    }

    fun sourceConstant(name: String?): Int? = when (name) {
        SettingsRepository.SOURCE_VOICE_CALL -> MediaRecorder.AudioSource.VOICE_CALL
        SettingsRepository.SOURCE_VOICE_COMMUNICATION -> MediaRecorder.AudioSource.VOICE_COMMUNICATION
        SettingsRepository.SOURCE_MIC -> MediaRecorder.AudioSource.MIC
        else -> null
    }

    private fun canRecord(context: Context, dir: File, source: Int): Boolean {
        val file = File(dir, "probe-$source.m4a")
        var recorder: MediaRecorder? = null
        return try {
            recorder = createRecorder(context)
            recorder.setAudioSource(source)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioSamplingRate(16000)
            recorder.setAudioEncodingBitRate(32_000)
            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()
            Thread.sleep(180)
            recorder.stop()
            recorder.reset()
            file.length() > 0
        } catch (_: Throwable) {
            false
        } finally {
            try {
                recorder?.release()
            } catch (_: Throwable) {
            }
            file.delete()
        }
    }

    private fun createRecorder(context: Context): MediaRecorder =
        if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
}
