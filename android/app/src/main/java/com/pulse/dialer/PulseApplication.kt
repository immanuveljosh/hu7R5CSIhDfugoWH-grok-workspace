package com.pulse.dialer

import android.app.Application
import com.pulse.dialer.data.db.AppDatabase
import com.pulse.dialer.data.prefs.SettingsRepository
import com.pulse.dialer.data.repo.RecordingRepository
import com.pulse.dialer.data.storage.RecordingStorage
import com.pulse.dialer.recording.CallRecordingManager

class PulseApplication : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var settings: SettingsRepository
        private set
    lateinit var recordings: RecordingRepository
        private set
    lateinit var recordingManager: CallRecordingManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = AppDatabase.build(this)
        settings = SettingsRepository(this)
        val storage = RecordingStorage(this)
        recordings = RecordingRepository(database.recordingDao(), storage)
        recordingManager = CallRecordingManager(this, settings, recordings, storage)
    }

    companion object {
        lateinit var instance: PulseApplication
            private set
    }
}
