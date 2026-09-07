package com.pulse.dialer.data.repo

import com.pulse.dialer.data.db.RecordingDao
import com.pulse.dialer.data.db.RecordingEntity
import com.pulse.dialer.data.storage.RecordingStorage
import kotlinx.coroutines.flow.Flow

class RecordingRepository(
    private val dao: RecordingDao,
    private val storage: RecordingStorage,
) {
    fun observe(): Flow<List<RecordingEntity>> = dao.observeAll()

    suspend fun insert(entity: RecordingEntity): Long = dao.insert(entity)

    suspend fun delete(id: Long) {
        val row = dao.byId(id) ?: return
        storage.delete(row.filePath)
        dao.delete(id)
    }

    suspend fun deleteAll() {
        storage.deleteAll()
        dao.deleteAll()
    }

    suspend fun totalBytes(): Long = dao.totalBytes()
}
