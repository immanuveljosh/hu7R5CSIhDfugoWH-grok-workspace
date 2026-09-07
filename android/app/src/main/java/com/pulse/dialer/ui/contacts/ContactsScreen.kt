package com.pulse.dialer.ui.contacts

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import com.pulse.dialer.data.repo.ContactEntry
import com.pulse.dialer.data.repo.ContactsRepository
import com.pulse.dialer.permission.AppPermissions
import com.pulse.dialer.telecom.DefaultDialer
import com.pulse.dialer.ui.theme.Call
import com.pulse.dialer.ui.theme.Fg
import com.pulse.dialer.ui.theme.Muted
import com.pulse.dialer.util.PhoneNumbers

@Composable
fun ContactsScreen(onPlace: (String) -> Unit) {
    val context = LocalContext.current
    val repo = remember { ContactsRepository(context) }
    var query by remember { mutableStateOf("") }
    var rows by remember { mutableStateOf<List<ContactEntry>>(emptyList()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    LaunchedEffect(Unit) { launcher.launch(AppPermissions.contacts) }
    LaunchedEffect(query) { rows = repo.search(query) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text("People", color = Fg, fontSize = 22.sp)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            placeholder = { Text("Search name or number") },
            singleLine = true,
        )
        LazyColumn(Modifier.padding(top = 8.dp)) {
            items(rows, key = { it.id + it.number }) { c ->
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(c.name.ifBlank { PhoneNumbers.display(c.number) }, color = Fg)
                        Text(PhoneNumbers.display(c.number), color = Muted, fontSize = 12.sp)
                    }
                    IconButton(onClick = {
                        onPlace(c.number)
                        DefaultDialer.placeCall(context, c.number)
                    }) {
                        Icon(Icons.Outlined.Call, contentDescription = "Call", tint = Call)
                    }
                }
            }
        }
    }
}
