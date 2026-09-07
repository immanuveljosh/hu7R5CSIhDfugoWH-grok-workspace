package com.pulse.dialer.recording

import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.pulse.dialer.data.db.RecordingEntity
import com.pulse.dialer.data.prefs.SettingsRepository
import com.pulse.dialer.data.repo.RecordingRepository
import com.pulse.dialer.data.storage.RecordingStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.time.Instant

class CallRecordingManager(
    private val context: Context,
    private val settings: SettingsRepository,
    private val recordings: RecordingRepository,
    private val storage: RecordingStorage,
    private val probe: AudioSourceProbe = AudioSourceProbe(),
) {
    data class Status(
        val active: Boolean = false,
        val notice: String? = null,
        val error: String? = null,
        val phoneNumber: String = "",
        val contactName: String? = null,
        val direction: String = "outgoing",
        val startedAt: Instant? = null,
    )

    private val _status = MutableStateFlow(Status())
    val status: StateFlow<Status> = _status

    private var recorder: MediaRecorder? = null
    private var file: File? = null
    private var sourceName: String = SettingsRepository.SOURCE_MIC
    private var quality: String = SettingsRepository.QUALITY_STANDARD

    suspend fun refreshCapability(): RecordingCapability {
        val cap = probe.probe(context)
        settings.setProbe(cap.sourceName, cap.supported)
        return cap
    }

    suspend fun onCallActive(number: String, contactName: String?, direction: String) {
        if (!settings.isAutoRecordEnabled()) {
            _status.value = Status()
            return
        }
        quality = settings.quality()
        val stored = settings.probedSource()
        val constant = probe.sourceConstant(stored)
        val cap = if (stored != null && constant != null) {
            RecordingCapability(
                supported = true,
                sourceConstant = constant,
                sourceName = stored,
                nearEndOnly = stored != SettingsRepository.SOURCE_VOICE_CALL,
                message = stored,
            )
        } else {
            refreshCapability()
        }
        if (!cap.supported || cap.sourceConstant == null || cap.sourceName == null) {
            _status.value = Status(error = SettingsRepository.UNSUPPORTED_MESSAGE)
            return
        }
        startLocked(number, contactName, direction, cap)
    }

    private fun startLocked(
        number: String,
        contactName: String?,
        direction: String,
        cap: RecordingCapability,
    ) {
        if (_status.value.active) return
        val started = Instant.now()
        val out = try {
            storage.createFile(started)
        } catch (io: Exception) {
            _status.value = Status(error = io.message ?: "Could not create recording file")
            return
        }
        val rec = try {
            if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
        } catch (t: Throwable) {
            out.delete()
            _status.value = Status(error = t.message ?: "Microphone unavailable")
            return
        }
        val high = quality == SettingsRepository.QUALITY_HIGH
        try {
            rec.setAudioSource(cap.sourceConstant!!)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setAudioSamplingRate(if (high) 48_000 else 16_000)
            rec.setAudioEncodingBitRate(if (high) 128_000 else 32_000)
            rec.setOutputFile(out.absolutePath)
            rec.prepare()
            rec.start()
        } catch (t: Throwable) {
            try {
                rec.release()
            } catch (_: Throwable) {
            }
            out.delete()
            if (cap.sourceName != SettingsRepository.SOURCE_MIC) {
                startLocked(
                    number,
                    contactName,
                    direction,
                    RecordingCapability(
                        true,
                        MediaRecorder.AudioSource.MIC,
                        SettingsRepository.SOURCE_MIC,
                        true,
                        "Microphone (near-end only)",
                    ),
                )
                return
            }
            _status.value = Status(error = t.message ?: SettingsRepository.UNSUPPORTED_MESSAGE)
            return
        }
        recorder = rec
        file = out
        sourceName = cap.sourceName!!
        val notice = if (cap.nearEndOnly) "Recording near-end audio" else "Recording call"
        _status.value = Status(
            active = true,
            notice = notice,
            phoneNumber = number,
            contactName = contactName,
            direction = direction,
            startedAt = started,
        )
        runCatching {
            val i = Intent(context, RecordingService::class.java).putExtra(RecordingService.EXTRA_NOTICE, notice)
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i) else context.startService(i)
        }
    }

    suspend fun onCallEnded() {
        val snap = _status.value
        val rec = recorder
        val out = file
        recorder = null
        file = null
        _status.value = Status()
        runCatching { context.stopService(Intent(context, RecordingService::class.java)) }
        if (rec == null || out == null || !snap.active || snap.startedAt == null) {
            rec?.runCatching { release() }
            return
        }
        try {
            rec.stop()
        } catch (t: Throwable) {
            Log.w(TAG, "stop failed", t)
        } finally {
            rec.reset()
            rec.release()
        }
        val duration = (System.currentTimeMillis() - snap.startedAt.toEpochMilli()).coerceAtLeast(0)
        if (!out.exists() || out.length() == 0L) {
            out.delete()
            return
        }
        recordings.insert(
            RecordingEntity(
                filename = out.name,
                phoneNumber = snap.phoneNumber,
                contactName = snap.contactName,
                direction = snap.direction,
                startedAtEpochMs = snap.startedAt.toEpochMilli(),
                durationMs = duration,
                filePath = out.absolutePath,
                fileSize = out.length(),
                audioSource = sourceName,
                quality = quality,
            ),
        )
    }

    companion object {
        private const val TAG = "PulseRecord"
    }
}
