package com.pulse.dialer.data.repo

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactEntry(
    val id: String,
    val name: String,
    val number: String,
)

class ContactsRepository(private val context: Context) {
    suspend fun search(query: String): List<ContactEntry> = withContext(Dispatchers.IO) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext emptyList()
        }
        val out = LinkedHashMap<String, ContactEntry>()
        val uri: Uri = if (query.isBlank()) {
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        } else {
            Uri.withAppendedPath(
                ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI,
                Uri.encode(query),
            )
        }
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )
        context.contentResolver.query(
            uri,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE LOCALIZED ASC",
        )?.use { c ->
            val iId = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val iName = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val iNum = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (c.moveToNext() && out.size < 200) {
                val id = c.getString(iId) ?: continue
                val number = c.getString(iNum)?.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }.orEmpty()
                if (number.isEmpty()) continue
                out.putIfAbsent(
                    id + number,
                    ContactEntry(id, c.getString(iName).orEmpty(), number),
                )
            }
        }
        out.values.toList()
    }

    suspend fun nameFor(number: String): String? = withContext(Dispatchers.IO) {
        if (number.isBlank()) return@withContext null
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return@withContext null
        }
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(number),
        )
        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    }
}
