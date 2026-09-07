package com.pulse.dialer.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY startedAtEpochMs DESC")
    fun observeAll(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings ORDER BY startedAtEpochMs DESC")
    suspend fun all(): List<RecordingEntity>

    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun byId(id: Long): RecordingEntity?

    @Insert
    suspend fun insert(entity: RecordingEntity): Long

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM recordings")
    suspend fun deleteAll()

    @Query("SELECT COALESCE(SUM(fileSize), 0) FROM recordings")
    suspend fun totalBytes(): Long
}
