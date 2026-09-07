package com.pulse.dialer.data.repo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class RecentCall(
    val id: Long,
    val number: String,
    val name: String?,
    val type: Int,
    val date: Long,
    val durationSec: Long,
)

class CallLogRepository(private val context: Context) {
    suspend fun recents(limit: Int = 50): List<RecentCall> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext emptyList()
        }
        val out = mutableListOf<RecentCall>()
        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
        )
        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC",
        )?.use { c ->
            val iId = c.getColumnIndex(CallLog.Calls._ID)
            val iNum = c.getColumnIndex(CallLog.Calls.NUMBER)
            val iName = c.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val iType = c.getColumnIndex(CallLog.Calls.TYPE)
            val iDate = c.getColumnIndex(CallLog.Calls.DATE)
            val iDur = c.getColumnIndex(CallLog.Calls.DURATION)
            while (c.moveToNext() && out.size < limit) {
                out += RecentCall(
                    id = c.getLong(iId),
                    number = c.getString(iNum).orEmpty(),
                    name = c.getString(iName),
                    type = c.getInt(iType),
                    date = c.getLong(iDate),
                    durationSec = c.getLong(iDur),
                )
            }
        }
        out
    }
}
