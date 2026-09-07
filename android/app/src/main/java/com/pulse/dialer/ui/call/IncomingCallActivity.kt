package com.pulse.dialer.ui.call

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.CallEnd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.dialer.PulseApplication
import com.pulse.dialer.telecom.CallController
import com.pulse.dialer.ui.theme.Bg
import com.pulse.dialer.ui.theme.Call
import com.pulse.dialer.ui.theme.CallFg
import com.pulse.dialer.ui.theme.Fg
import com.pulse.dialer.ui.theme.Hangup
import com.pulse.dialer.ui.theme.Muted
import com.pulse.dialer.ui.theme.PulseTheme
import com.pulse.dialer.util.PhoneNumbers

class IncomingCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PulseTheme {
                val snap by CallController.primaryCall.collectAsState()
                val auto by PulseApplication.instance.settings.autoRecordFlow.collectAsState(initial = false)
                Column(
                    Modifier.fillMaxSize().background(Bg).padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("INCOMING CALL", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 48.dp))
                    Text(
                        snap?.name ?: PhoneNumbers.display(snap?.number.orEmpty()),
                        color = Fg,
                        fontSize = 28.sp,
                        modifier = Modifier.padding(top = 32.dp),
                    )
                    if (!snap?.name.isNullOrBlank()) {
                        Text(PhoneNumbers.display(snap?.number.orEmpty()), color = Muted)
                    }
                    if (auto) {
                        Text(
                            "Auto-record is on. This call will be recorded on this device if audio capture is allowed.",
                            color = Muted,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp),
                        )
                    }
                    Row(
                        Modifier.padding(top = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(64.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = {
                                runCatching { CallController.reject() }
                                finish()
                            },
                            modifier = Modifier.size(72.dp).clip(CircleShape).background(Hangup),
                        ) { Icon(Icons.Outlined.CallEnd, contentDescription = "Decline", tint = Fg) }
                        IconButton(
                            onClick = {
                                runCatching { CallController.answer() }
                                startActivity(Intent(this@IncomingCallActivity, InCallActivity::class.java))
                                finish()
                            },
                            modifier = Modifier.size(72.dp).clip(CircleShape).background(Call),
                        ) { Icon(Icons.Outlined.Call, contentDescription = "Answer", tint = CallFg) }
                    }
                }
            }
        }
    }
}
