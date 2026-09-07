package com.pulse.dialer.ui.call

import android.os.Bundle
import android.telecom.Call
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.dialer.PulseApplication
import com.pulse.dialer.telecom.CallController
import com.pulse.dialer.telecom.InCallServiceLocator
import com.pulse.dialer.ui.theme.Bg
import com.pulse.dialer.ui.theme.Elevated
import com.pulse.dialer.ui.theme.Fg
import com.pulse.dialer.ui.theme.Hangup
import com.pulse.dialer.ui.theme.Muted
import com.pulse.dialer.ui.theme.PulseTheme
import com.pulse.dialer.util.PhoneNumbers
import com.pulse.dialer.util.TimeFormat
import kotlinx.coroutines.delay

class InCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseTheme { InCallPane(onHangup = { finish() }) }
        }
    }
}

@Composable
private fun InCallPane(onHangup: () -> Unit) {
    val snap by CallController.primaryCall.collectAsState()
    val rec by PulseApplication.instance.recordingManager.status.collectAsState()
    var elapsed by remember { mutableLongStateOf(0L) }
    LaunchedEffect(snap?.state) {
        val start = System.currentTimeMillis()
        while (true) {
            if (snap?.state != Call.STATE_ACTIVE) break
            elapsed = System.currentTimeMillis() - start
            delay(250)
        }
    }
    LaunchedEffect(snap) {
        if (snap == null) onHangup()
    }
    Column(
        Modifier.fillMaxSize().background(Bg).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            Modifier.padding(top = 72.dp).weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(PhoneNumbers.initials(snap?.name ?: ""), color = Fg, fontSize = 28.sp, modifier = Modifier.padding(bottom = 16.dp))
            Text(snap?.name ?: PhoneNumbers.display(snap?.number.orEmpty()), color = Fg, fontSize = 24.sp)
            if (!snap?.name.isNullOrBlank()) {
                Text(PhoneNumbers.display(snap?.number.orEmpty()), color = Muted, fontSize = 14.sp)
            }
            val status = when (snap?.state) {
                Call.STATE_DIALING, Call.STATE_CONNECTING -> "Calling…"
                Call.STATE_HOLDING -> "On hold"
                Call.STATE_ACTIVE -> TimeFormat.duration(elapsed)
                else -> "Call"
            }
            Text(status, color = Muted, modifier = Modifier.padding(top = 12.dp))
            if (rec.active) {
                Text(rec.notice ?: "Recording", color = Hangup, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
            } else if (rec.error != null) {
                Text(rec.error.orEmpty(), color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
            }
        }
        Row(Modifier.padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            RoundAction(if (CallController.muted) Icons.Outlined.MicOff else Icons.Outlined.Mic, "Mute") {
                InCallServiceLocator.service?.let { CallController.setMuted(!CallController.muted, it) }
            }
            RoundAction(Icons.Outlined.VolumeUp, "Speaker") {
                InCallServiceLocator.service?.let { CallController.setSpeaker(!CallController.speaker, it) }
            }
            RoundAction(Icons.Outlined.Pause, "Hold") {
                runCatching { CallController.hold() }
            }
        }
        IconButton(
            onClick = {
                runCatching { CallController.disconnect() }
                onHangup()
            },
            modifier = Modifier.size(72.dp).clip(CircleShape).background(Hangup),
        ) {
            Icon(Icons.Outlined.CallEnd, contentDescription = "Hang up", tint = Fg)
        }
    }
}

@Composable
private fun RoundAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.size(56.dp).clip(CircleShape).background(Elevated)) {
            Icon(icon, contentDescription = label, tint = Fg)
        }
        Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp))
    }
}
