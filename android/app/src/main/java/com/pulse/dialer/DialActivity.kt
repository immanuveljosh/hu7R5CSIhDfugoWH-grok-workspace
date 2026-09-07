package com.pulse.dialer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pulse.dialer.telecom.DefaultDialer
import com.pulse.dialer.ui.PulseApp
import com.pulse.dialer.ui.theme.PulseTheme
import com.pulse.dialer.util.PhoneNumbers

class DialActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val seed = intent.data?.schemeSpecificPart.orEmpty().let(PhoneNumbers::digitsAndSymbols)
        if (intent.action == Intent.ACTION_CALL && seed.isNotBlank()) {
            runCatching { DefaultDialer.placeCall(this, seed) }
        }
        setContent {
            PulseTheme {
                PulseApp(initialNumber = seed)
            }
        }
    }
}
