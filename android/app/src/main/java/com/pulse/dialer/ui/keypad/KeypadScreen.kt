package com.pulse.dialer.ui.keypad

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulse.dialer.PulseApplication
import com.pulse.dialer.permission.AppPermissions
import com.pulse.dialer.telecom.DefaultDialer
import com.pulse.dialer.ui.theme.Call
import com.pulse.dialer.ui.theme.CallFg
import com.pulse.dialer.ui.theme.Elevated
import com.pulse.dialer.ui.theme.Fg
import com.pulse.dialer.ui.theme.Hangup
import com.pulse.dialer.ui.theme.Muted
import com.pulse.dialer.util.PhoneNumbers

private val keys = listOf(
    "1" to "", "2" to "ABC", "3" to "DEF",
    "4" to "GHI", "5" to "JKL", "6" to "MNO",
    "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
    "*" to "", "0" to "+", "#" to "",
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeypadScreen(dial: String, onDialChange: (String) -> Unit) {
    val context = LocalContext.current
    val auto by PulseApplication.instance.settings.autoRecordFlow.collectAsState(initial = false)
    val callLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { granted ->
        if (granted[Manifest.permission.CALL_PHONE] == true && dial.isNotBlank()) {
            DefaultDialer.placeCall(context, PhoneNumbers.digitsAndSymbols(dial))
        }
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (auto) {
            Text("Auto-record is on", color = Hangup, fontSize = 12.sp)
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = PhoneNumbers.display(dial).ifBlank { "Enter number" },
                color = if (dial.isBlank()) Muted else Fg,
                fontSize = if (dial.length > 12) 24.sp else 32.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        keys.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                row.forEach { (digit, letters) ->
                    KeyCap(digit, letters, onClick = { onDialChange((dial + digit).take(20)) }, onLong = {
                        if (digit == "0") onDialChange((dial + "+").take(20))
                    })
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(48.dp))
            Box(
                Modifier.size(64.dp).clip(CircleShape).background(Call).combinedClickable(
                    onClick = {
                        if (dial.isBlank()) return@combinedClickable
                        callLauncher.launch(AppPermissions.phone)
                    },
                ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Call, contentDescription = "Call", tint = CallFg)
            }
            IconButton(onClick = { if (dial.isNotEmpty()) onDialChange(dial.dropLast(1)) }, enabled = dial.isNotEmpty()) {
                Icon(Icons.Outlined.Backspace, contentDescription = "Delete", tint = Muted)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun KeyCap(digit: String, letters: String, onClick: () -> Unit, onLong: () -> Unit) {
    Column(
        Modifier.size(72.dp).clip(CircleShape).background(Elevated).combinedClickable(onClick = onClick, onLongClick = onLong),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(digit, color = Fg, fontSize = 26.sp, fontFamily = FontFamily.Monospace)
        Text(letters.ifBlank { " " }, color = Muted, fontSize = 10.sp)
    }
}
