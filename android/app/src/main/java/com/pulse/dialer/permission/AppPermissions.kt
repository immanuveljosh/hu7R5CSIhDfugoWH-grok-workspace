package com.pulse.dialer.permission

import android.Manifest
import android.os.Build

object AppPermissions {
    val phone: Array<String> = buildList {
        add(Manifest.permission.READ_PHONE_STATE)
        add(Manifest.permission.CALL_PHONE)
        add(Manifest.permission.ANSWER_PHONE_CALLS)
        add(Manifest.permission.READ_CALL_LOG)
        add(Manifest.permission.WRITE_CALL_LOG)
        add(Manifest.permission.READ_PHONE_NUMBERS)
    }.toTypedArray()

    val contacts = arrayOf(Manifest.permission.READ_CONTACTS)
    val microphone = arrayOf(Manifest.permission.RECORD_AUDIO)
    val notifications: Array<String> =
        if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.POST_NOTIFICATIONS) else emptyArray()

    fun forLaunch(): Array<String> = phone + contacts + notifications
    fun forRecording(): Array<String> = microphone + notifications
}
