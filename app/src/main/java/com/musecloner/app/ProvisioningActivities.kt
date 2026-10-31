package com.musecloner.app

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle

/** Android 12+: system poochta hai kaunsa mode chahiye -> managed profile. */
@SuppressLint("NewApi")
class ProvisioningModeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val data = Intent().putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_MODE,
            DevicePolicyManager.PROVISIONING_MODE_MANAGED_PROFILE
        )
        setResult(RESULT_OK, data)
        finish()
    }
}

/** Android 12+: provisioning ke aakhri step me admin app "ok" bolta hai. */
class PolicyComplianceActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dpm = getSystemService(DevicePolicyManager::class.java)
        if (dpm.isProfileOwnerApp(packageName)) {
            val admin = ComponentName(this, CloneAdminReceiver::class.java)
            runCatching { dpm.setProfileName(admin, "Clone Space") }
            runCatching { dpm.setProfileEnabled(admin) }
        }
        setResult(RESULT_OK)
        finish()
    }
}
