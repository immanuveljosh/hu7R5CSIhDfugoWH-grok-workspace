package com.pulse.dialer.ui.recordings

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pulse.dialer.PulseApplication
import com.pulse.dialer.data.db.RecordingEntity
import com.pulse.dialer.ui.theme.Fg
import com.pulse.dialer.ui.theme.Hangup
import com.pulse.dialer.ui.theme.Muted
import com.pulse.dialer.util.PhoneNumbers
import com.pulse.dialer.util.TimeFormat
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun RecordingsScreen() {
    val context = LocalContext.current
    val rows by PulseApplication.instance.recordings.observe().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var confirmAll by remember { mutableStateOf(false) }
    var playingId by remember { mutableStateOf<Long?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    val player = remember {
        ExoPlayer.Builder(context).build()
    }
    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isPlaying && player.playbackState == Player.STATE_ENDED) {
                    playingId = null
                    progress = 0f
                }
            }
        }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("On this device", color = Fg, fontSize = 22.sp, modifier = Modifier.weight(1f))
            if (rows.isNotEmpty()) {
                TextButton(onClick = { confirmAll = true }) { Text("Delete all", color = Hangup) }
            }
        }
        Text("Stored in app-private files. Never uploaded.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        if (rows.isEmpty()) {
            Text("No recordings yet. Enable auto-record in Settings, then place a call.", color = Muted, modifier = Modifier.padding(top = 24.dp))
        }
        LazyColumn(Modifier.padding(top = 8.dp)) {
            items(rows, key = { it.id }) { row ->
                RecordingRow(
                    row = row,
                    playing = playingId == row.id && player.isPlaying,
                    progress = if (playingId == row.id) progress else 0f,
                    onPlay = {
                        if (playingId == row.id) {
                            if (player.isPlaying) player.pause() else player.play()
                        } else {
                            playingId = row.id
                            player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(row.filePath))))
                            player.prepare()
                            player.play()
                        }
                    },
                    onSeek = { p ->
                        val dur = player.duration.coerceAtLeast(1)
                        player.seekTo((p * dur).toLong())
                        progress = p
                    },
                    onDelete = { scope.launch { PulseApplication.instance.recordings.delete(row.id) } },
                )
            }
        }
    }
    if (confirmAll) {
        AlertDialog(
            onDismissRequest = { confirmAll = false },
            title = { Text("Delete all recordings?") },
            text = { Text("This removes every local file and its metadata. It cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmAll = false
                    scope.launch { PulseApplication.instance.recordings.deleteAll() }
                    player.stop()
                    playingId = null
                }) { Text("Delete all", color = Hangup) }
            },
            dismissButton = { TextButton(onClick = { confirmAll = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun RecordingRow(
    row: RecordingEntity,
    playing: Boolean,
    progress: Float,
    onPlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onDelete: () -> Unit,
) {
    val whenText = DateTimeFormatter.ofPattern("MMM d · h:mm a").withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(row.startedAtEpochMs))
    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPlay) {
                Icon(if (playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = if (playing) "Pause" else "Play")
            }
            Column(Modifier.weight(1f)) {
                Text(row.contactName ?: PhoneNumbers.display(row.phoneNumber), color = Fg)
                Text(
                    "${row.direction.replaceFirstChar { it.uppercase() }} · $whenText · ${TimeFormat.duration(row.durationMs)}",
                    color = Muted,
                    fontSize = 12.sp,
                )
                Text("${row.filename} · ${TimeFormat.bytes(row.fileSize)}", color = Muted, fontSize = 11.sp)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Delete", tint = Hangup)
            }
        }
        if (playing || progress > 0f) {
            Slider(value = progress, onValueChange = onSeek)
        }
    }
}
