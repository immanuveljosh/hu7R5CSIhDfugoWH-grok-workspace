package com.pulse.dialer.telecom

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager

object DefaultDialer {
    fun isDefault(context: Context): Boolean {
        val telecom = context.getSystemService(TelecomManager::class.java)
        return telecom?.defaultDialerPackage == context.packageName
    }

    fun requestRole(activity: Activity, requestCode: Int) {
        if (Build.VERSION.SDK_INT >= 29) {
            val rm = activity.getSystemService(RoleManager::class.java)
            if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_DIALER) && !rm.isRoleHeld(RoleManager.ROLE_DIALER)) {
                activity.startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER), requestCode)
                return
            }
        }
        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.packageName)
        activity.startActivity(intent)
    }

    fun placeCall(context: Context, number: String) {
        val uri = android.net.Uri.fromParts("tel", number, null)
        if (isDefault(context)) {
            val extras = android.os.Bundle()
            context.getSystemService(TelecomManager::class.java)?.placeCall(uri, extras)
        } else {
            val intent = Intent(Intent.ACTION_CALL, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
