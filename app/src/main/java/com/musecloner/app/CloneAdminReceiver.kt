package com.musecloner.app

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * Clone Space (managed/work profile) ka admin.
 * Profile ban jaane ke baad system yahan callback deta hai.
 */
class CloneAdminReceiver : DeviceAdminReceiver() {

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        val dpm = context.getSystemService(DevicePolicyManager::class.java)
        val admin = ComponentName(context, CloneAdminReceiver::class.java)

        runCatching { dpm.setProfileName(admin, "Clone Space") }
        runCatching { dpm.setProfileEnabled(admin) }

        // Clone Space ke andar app ki screen khol do
        runCatching {
            context.startActivity(
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
