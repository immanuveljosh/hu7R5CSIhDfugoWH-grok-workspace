package com.pulse.dialer.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filename: String,
    val phoneNumber: String,
    val contactName: String?,
    val direction: String,
    val startedAtEpochMs: Long,
    val durationMs: Long,
    val filePath: String,
    val fileSize: Long,
    val audioSource: String,
    val quality: String,
)
