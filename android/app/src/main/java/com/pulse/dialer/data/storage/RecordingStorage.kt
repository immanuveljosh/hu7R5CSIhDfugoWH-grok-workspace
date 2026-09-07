package com.pulse.dialer.data.storage

import android.content.Context
import com.pulse.dialer.util.TimeFormat
import java.io.File
import java.io.IOException
import java.time.Instant

class RecordingStorage(context: Context) {
    private val dir: File = File(context.filesDir, "recordings").apply { mkdirs() }

    fun directory(): File = dir

    fun createFile(startedAt: Instant): File {
        if (!dir.exists() && !dir.mkdirs()) {
            throw IOException("Could not create recordings directory")
        }
        val usable = dir.usableSpace
        if (usable in 1 until MIN_FREE_BYTES) {
            throw IOException("Storage full")
        }
        return File(dir, TimeFormat.recordingFileName(startedAt))
    }

    fun delete(path: String): Boolean {
        val file = File(path)
        if (!file.canonicalPath.startsWith(dir.canonicalPath)) return false
        return !file.exists() || file.delete()
    }

    fun deleteAll(): Int {
        val files = dir.listFiles() ?: return 0
        var n = 0
        files.forEach { if (it.delete()) n++ }
        return n
    }

    fun bytesUsed(): Long = dir.walkTopDown().filter { it.isFile }.sumOf { it.length() }

    companion object {
        const val MIN_FREE_BYTES = 2L * 1024L * 1024L
    }
}
