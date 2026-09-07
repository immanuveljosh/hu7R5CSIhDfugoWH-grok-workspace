package com.pulse.dialer.telecom

import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class CallSnapshot(
    val id: String,
    val number: String,
    val name: String?,
    val state: Int,
    val direction: String,
    val muted: Boolean,
    val speaker: Boolean,
    val onHold: Boolean,
)

object CallController {
    private val calls = LinkedHashMap<String, Call>()
    private val _snapshots = MutableStateFlow<List<CallSnapshot>>(emptyList())
    val snapshots: StateFlow<List<CallSnapshot>> = _snapshots
    private val _primary = MutableStateFlow<CallSnapshot?>(null)
    val primaryCall: StateFlow<CallSnapshot?> = _primary

    var muted: Boolean = false
        private set
    var speaker: Boolean = false
        private set

    fun attach(call: Call) {
        calls[idOf(call)] = call
        call.registerCallback(
            object : Call.Callback() {
                override fun onStateChanged(call: Call, state: Int) = publish()
                override fun onDetailsChanged(call: Call, details: Call.Details) = publish()
            },
        )
        publish()
    }

    fun detach(call: Call) {
        calls.remove(idOf(call))
        publish()
    }

    fun answer(call: Call = requirePrimary()) {
        call.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun reject(call: Call = requirePrimary()) {
        if (Build.VERSION.SDK_INT >= 30) {
            call.reject(Call.REJECT_REASON_DECLINED)
        } else {
            call.reject(false, "")
        }
    }

    fun disconnect(call: Call = requirePrimary()) {
        call.disconnect()
    }

    fun hold(call: Call = requirePrimary()) {
        if (call.details.state == Call.STATE_HOLDING) call.unhold() else call.hold()
    }

    fun playDtmf(digit: Char, call: Call = requirePrimary()) {
        call.playDtmfTone(digit)
        call.stopDtmfTone()
    }

    fun setMuted(value: Boolean, service: InCallService) {
        muted = value
        service.setMuted(value)
        publish()
    }

    fun setSpeaker(value: Boolean, service: InCallService) {
        speaker = value
        val route = if (value) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_WIRED_OR_EARPIECE
        service.setAudioRoute(route)
        publish()
    }

    fun find(id: String): Call? = calls[id]
    fun all(): Collection<Call> = calls.values.toList()
    fun requirePrimary(): Call = calls.values.lastOrNull() ?: error("No call")

    private fun idOf(call: Call): String = Integer.toHexString(System.identityHashCode(call))

    private fun directionOf(call: Call): String =
        if (call.details.callDirection == Call.Details.DIRECTION_INCOMING) "incoming" else "outgoing"

    private fun numberOf(call: Call): String = call.details.handle?.schemeSpecificPart.orEmpty()

    private fun publish() {
        val list = calls.map { (id, call) ->
            CallSnapshot(
                id = id,
                number = numberOf(call),
                name = call.details.callerDisplayName,
                state = call.details.state,
                direction = directionOf(call),
                muted = muted,
                speaker = speaker,
                onHold = call.details.state == Call.STATE_HOLDING,
            )
        }
        _snapshots.value = list
        _primary.value = list.lastOrNull()
    }
}
