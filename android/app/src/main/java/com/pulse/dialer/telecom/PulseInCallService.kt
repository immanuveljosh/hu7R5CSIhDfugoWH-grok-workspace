package com.pulse.dialer.telecom

import android.content.Intent
import android.os.IBinder
import android.telecom.Call
import android.telecom.InCallService
import com.pulse.dialer.PulseApplication
import com.pulse.dialer.data.repo.ContactsRepository
import com.pulse.dialer.ui.call.InCallActivity
import com.pulse.dialer.ui.call.IncomingCallActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PulseInCallService : InCallService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val contacts by lazy { ContactsRepository(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        InCallServiceLocator.service = this
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)

    override fun onUnbind(intent: Intent?): Boolean {
        if (InCallServiceLocator.service === this) InCallServiceLocator.service = null
        return super.onUnbind(intent)
    }

    override fun onCallAdded(call: Call) {
        CallController.attach(call)
        when (call.details.state) {
            Call.STATE_RINGING, Call.STATE_NEW -> showIncoming()
            else -> showInCall()
        }
        call.registerCallback(
            object : Call.Callback() {
                override fun onStateChanged(call: Call, state: Int) {
                    if (state == Call.STATE_ACTIVE) {
                        showInCall()
                        startRecording(call)
                    }
                    if (state == Call.STATE_DISCONNECTED) stopRecording()
                    if (state == Call.STATE_RINGING) showIncoming()
                }
            },
        )
        if (call.details.state == Call.STATE_ACTIVE) startRecording(call)
    }

    override fun onCallRemoved(call: Call) {
        CallController.detach(call)
        if (CallController.all().isEmpty()) stopRecording()
    }

    override fun onDestroy() {
        if (InCallServiceLocator.service === this) InCallServiceLocator.service = null
        scope.cancel()
        super.onDestroy()
    }

    private fun startRecording(call: Call) {
        val number = call.details.handle?.schemeSpecificPart.orEmpty()
        val direction =
            if (call.details.callDirection == Call.Details.DIRECTION_INCOMING) "incoming" else "outgoing"
        scope.launch {
            val name = call.details.callerDisplayName ?: contacts.nameFor(number)
            runCatching {
                PulseApplication.instance.recordingManager.onCallActive(number, name, direction)
            }
        }
    }

    private fun stopRecording() {
        scope.launch {
            runCatching { PulseApplication.instance.recordingManager.onCallEnded() }
        }
    }

    private fun showIncoming() {
        startActivity(
            Intent(this, IncomingCallActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP,
            ),
        )
    }

    private fun showInCall() {
        startActivity(
            Intent(this, InCallActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP,
            ),
        )
    }
}
