package com.pulse.dialer.ui.settings

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.dialer.PulseApplication
import com.pulse.dialer.data.prefs.SettingsRepository
import com.pulse.dialer.permission.AppPermissions
import com.pulse.dialer.telecom.DefaultDialer
import com.pulse.dialer.ui.theme.Call
import com.pulse.dialer.ui.theme.Fg
import com.pulse.dialer.ui.theme.Hangup
import com.pulse.dialer.ui.theme.Muted
import com.pulse.dialer.util.TimeFormat
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val settings = PulseApplication.instance.settings
    val auto by settings.autoRecordFlow.collectAsState(initial = false)
    val quality by settings.qualityFlow.collectAsState(initial = SettingsRepository.QUALITY_STANDARD)
    val supported by settings.recordingSupportedFlow.collectAsState(initial = true)
    val source by settings.probedSourceFlow.collectAsState(initial = null)
    val privacy by settings.privacyAcceptedFlow.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    var used by remember { mutableStateOf(0L) }
    var count by remember { mutableStateOf(0) }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted[android.Manifest.permission.RECORD_AUDIO] == true) {
            scope.launch {
                val cap = PulseApplication.instance.recordingManager.refreshCapability()
                if (cap.supported) settings.setAutoRecord(true)
            }
        }
    }
    LaunchedEffect(Unit) {
        used = PulseApplication.instance.recordings.totalBytes()
        PulseApplication.instance.recordings.observe().collect { count = it.size }
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Pulse", color = Fg, fontSize = 22.sp)
        Text("Private Android dialer", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp, bottom = 16.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f).padding(end = 12.dp)) {
                Text("Auto call recording", color = Fg)
                Text("Starts when a call becomes active and stops when it ends. Never records while this is off.", color = Muted, fontSize = 12.sp)
            }
            Switch(
                checked = auto,
                enabled = supported,
                onCheckedChange = { value ->
                    if (value) micLauncher.launch(AppPermissions.forRecording())
                    else scope.launch { settings.setAutoRecord(false) }
                },
            )
        }
        if (auto) {
            Text(
                "Recording is enabled. Tell the other party if your local law requires consent.",
                color = Hangup,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (!supported) {
            Text(SettingsRepository.UNSUPPORTED_MESSAGE, color = Fg, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
        } else if (source == SettingsRepository.SOURCE_MIC) {
            Text("Microphone fallback — near-end only. Two-way call audio is not available on this device.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Text("Quality", color = Fg, modifier = Modifier.padding(top = 20.dp))
        Row(Modifier.padding(top = 8.dp)) {
            FilterChip(
                selected = quality == SettingsRepository.QUALITY_STANDARD,
                onClick = { scope.launch { settings.setQuality(SettingsRepository.QUALITY_STANDARD) } },
                label = { Text("Standard AAC") },
                modifier = Modifier.padding(end = 8.dp),
            )
            FilterChip(
                selected = quality == SettingsRepository.QUALITY_HIGH,
                onClick = { scope.launch { settings.setQuality(SettingsRepository.QUALITY_HIGH) } },
                label = { Text("High AAC") },
            )
        }

        Text("Local storage", color = Fg, modifier = Modifier.padding(top = 20.dp))
        Text("$count files · ${TimeFormat.bytes(used)} in app-private storage. Pulse never uploads recordings.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))

        Text("Default phone app", color = Fg, modifier = Modifier.padding(top = 20.dp))
        Text(
            if (DefaultDialer.isDefault(context)) "Pulse is the default dialer."
            else "Android requires this role for in-call UI and eligible recording.",
            color = Muted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        Button(onClick = {
            val act = context as? Activity ?: return@Button
            DefaultDialer.requestRole(act, 42)
        }) { Text("Set as default dialer") }

        Text("Privacy", color = Fg, modifier = Modifier.padding(top = 20.dp))
        Text(
            "Recordings stay in this app’s private files. Pulse does not use accessibility services, hidden APIs, or root. Permissions are requested only when a feature needs them.",
            color = Muted,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (!privacy) {
            Button(onClick = { scope.launch { settings.acceptPrivacy() } }, modifier = Modifier.padding(top = 8.dp)) {
                Text("I understand")
            }
        }

        Button(
            onClick = { scope.launch { PulseApplication.instance.recordingManager.refreshCapability() } },
            modifier = Modifier.padding(top = 20.dp),
        ) { Text("Re-check recording support", color = Call) }
    }
}
