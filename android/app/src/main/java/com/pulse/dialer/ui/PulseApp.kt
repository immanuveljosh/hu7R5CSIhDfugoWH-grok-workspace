package com.pulse.dialer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.pulse.dialer.ui.keypad.KeypadScreen
import com.pulse.dialer.ui.recordings.RecordingsScreen
import com.pulse.dialer.ui.settings.SettingsScreen
import com.pulse.dialer.ui.theme.Bg
import com.pulse.dialer.ui.theme.Call
import com.pulse.dialer.ui.theme.Muted

@Composable
fun PulseApp(initialNumber: String = "") {
    var tab by remember { mutableIntStateOf(0) }
    var dial by remember { mutableStateOf(initialNumber) }
    LaunchedEffect(initialNumber) {
        if (initialNumber.isNotBlank()) {
            dial = initialNumber
            tab = 0
        }
    }
    val items = listOf(
        "Keypad" to Icons.Outlined.Apps,
        "Recents" to Icons.Outlined.AccessTime,
        "Contacts" to Icons.Outlined.Person,
        "Files" to Icons.Outlined.GraphicEq,
        "Settings" to Icons.Outlined.Settings,
    )
    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Bg, contentColor = Muted) {
                items.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Call,
                            selectedTextColor = Call,
                            indicatorColor = Color(0xFF1C1F25),
                            unselectedIconColor = Muted,
                            unselectedTextColor = Muted,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding).background(Bg)) {
            when (tab) {
                0 -> KeypadScreen(dial, onDialChange = { dial = it })
                1 -> com.pulse.dialer.ui.recents.RecentsScreen(onPlace = { dial = it; tab = 0 })
                2 -> com.pulse.dialer.ui.contacts.ContactsScreen(onPlace = { dial = it; tab = 0 })
                3 -> RecordingsScreen()
                else -> SettingsScreen()
            }
        }
    }
}
