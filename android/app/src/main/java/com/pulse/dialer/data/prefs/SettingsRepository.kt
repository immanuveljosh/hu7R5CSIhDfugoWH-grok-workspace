package com.pulse.dialer.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("pulse_settings")

class SettingsRepository(private val context: Context) {
    private val autoRecord = booleanPreferencesKey("auto_record")
    private val quality = stringPreferencesKey("quality")
    private val privacyAccepted = booleanPreferencesKey("privacy_accepted")
    private val probedSource = stringPreferencesKey("probed_source")
    private val recordingSupported = booleanPreferencesKey("recording_supported")

    val autoRecordFlow: Flow<Boolean> = context.dataStore.data.map { it[autoRecord] ?: false }
    val qualityFlow: Flow<String> = context.dataStore.data.map { it[quality] ?: QUALITY_STANDARD }
    val privacyAcceptedFlow: Flow<Boolean> = context.dataStore.data.map { it[privacyAccepted] ?: false }
    val probedSourceFlow: Flow<String?> = context.dataStore.data.map { it[probedSource] }
    val recordingSupportedFlow: Flow<Boolean> = context.dataStore.data.map { it[recordingSupported] ?: true }

    suspend fun isAutoRecordEnabled(): Boolean = autoRecordFlow.first()
    suspend fun quality(): String = qualityFlow.first()
    suspend fun probedSource(): String? = probedSourceFlow.first()

    suspend fun setAutoRecord(value: Boolean) {
        context.dataStore.edit { it[autoRecord] = value }
    }

    suspend fun setQuality(value: String) {
        context.dataStore.edit { it[quality] = value }
    }

    suspend fun acceptPrivacy() {
        context.dataStore.edit { it[privacyAccepted] = true }
    }

    suspend fun setProbe(source: String?, supported: Boolean) {
        context.dataStore.edit {
            if (source == null) it.remove(probedSource) else it[probedSource] = source
            it[recordingSupported] = supported
            if (!supported) it[autoRecord] = false
        }
    }

    companion object {
        const val QUALITY_STANDARD = "standard"
        const val QUALITY_HIGH = "high"
        const val SOURCE_VOICE_CALL = "VOICE_CALL"
        const val SOURCE_VOICE_COMMUNICATION = "VOICE_COMMUNICATION"
        const val SOURCE_MIC = "MIC"
        const val UNSUPPORTED_MESSAGE = "Call recording is not supported on this device."
    }
}
