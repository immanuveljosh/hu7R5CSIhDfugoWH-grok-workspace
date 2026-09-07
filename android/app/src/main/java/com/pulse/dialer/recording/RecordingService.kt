package com.pulse.dialer.recording

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pulse.dialer.R
import com.pulse.dialer.ui.call.InCallActivity

class RecordingService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notice = intent?.getStringExtra(EXTRA_NOTICE) ?: getString(R.string.recording_notice)
        ensureChannel()
        val pending = PendingIntent.getActivity(
            this,
            0,
            Intent(this, InCallActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_call)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(notice)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pending)
            .build()
        if (Build.VERSION.SDK_INT >= 30) {
            startForeground(ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(ID, notification)
        }
        return START_STICKY
    }

    private fun ensureChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.recording_channel), NotificationManager.IMPORTANCE_LOW),
        )
    }

    companion object {
        const val EXTRA_NOTICE = "notice"
        private const val CHANNEL = "pulse_recording"
        private const val ID = 71
    }
}
