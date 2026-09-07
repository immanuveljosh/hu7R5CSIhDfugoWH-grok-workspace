package com.pulse.dialer.ui.recents

import android.provider.CallLog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.dialer.data.repo.CallLogRepository
import com.pulse.dialer.data.repo.RecentCall
import com.pulse.dialer.permission.AppPermissions
import com.pulse.dialer.telecom.DefaultDialer
import com.pulse.dialer.ui.theme.Call
import com.pulse.dialer.ui.theme.Fg
import com.pulse.dialer.ui.theme.Hangup
import com.pulse.dialer.ui.theme.Muted
import com.pulse.dialer.util.PhoneNumbers
import com.pulse.dialer.util.TimeFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RecentsScreen(onPlace: (String) -> Unit) {
    val context = LocalContext.current
    var rows by remember { mutableStateOf<List<RecentCall>>(emptyList()) }
    val repo = remember { CallLogRepository(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        // refresh after grant
    }
    LaunchedEffect(Unit) {
        launcher.launch(AppPermissions.phone)
        rows = repo.recents()
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("Calls", color = Fg, fontSize = 22.sp)
        if (rows.isEmpty()) {
            Text("No recents yet. Grant call log access to see them.", color = Muted, modifier = Modifier.padding(top = 16.dp))
        }
        LazyColumn(Modifier.padding(top = 12.dp)) {
            items(rows, key = { it.id }) { row ->
                val missed = row.type == CallLog.Calls.MISSED_TYPE
                val dir = when (row.type) {
                    CallLog.Calls.INCOMING_TYPE -> "Incoming"
                    CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                    CallLog.Calls.MISSED_TYPE -> "Missed"
                    else -> "Call"
                }
                Row(
                    Modifier.fillMaxSize().clickable { onPlace(row.number) }.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(row.name ?: PhoneNumbers.display(row.number), color = if (missed) Hangup else Fg)
                        Text(
                            "$dir · ${TimeFormat.duration(row.durationSec * 1000)} · ${
                                DateTimeFormatter.ofPattern("MMM d · h:mm a").withZone(ZoneId.systemDefault())
                                    .format(Instant.ofEpochMilli(row.date))
                            }",
                            color = Muted,
                            fontSize = 12.sp,
                        )
                    }
                    IconButton(onClick = { DefaultDialer.placeCall(context, row.number) }) {
                        Icon(Icons.Outlined.Call, contentDescription = "Call", tint = Call)
                    }
                }
            }
        }
    }
}
