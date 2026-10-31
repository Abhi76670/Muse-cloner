package com.musecloner.app

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.CrossProfileApps
import android.content.pm.PackageManager
import android.net.Uri
import android.os.UserManager
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppItem(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap?,
    val installedHere: Boolean,
    val isSystem: Boolean,
)

/** build-virtual.yml se bane engine app ka package name. */
const val VIRTUAL_PACKAGE = "top.niunaijun.blackbox"

val GOOGLE_PACKAGES = listOf(
    "com.google.android.gms",
    "com.google.android.gsf",
    "com.android.vending",
)

/**
 * Saara system-level kaam yahan hai.
 *
 * Do jagah chalta hai (same APK):
 *  1. Personal side  -> Clone Space banata / kholta hai.
 *  2. Clone Space    -> (profile owner) apps ki copy install karta hai.
 */
class CloneManager(private val context: Context) {

    private val pm: PackageManager = context.packageManager
    private val dpm: DevicePolicyManager = context.getSystemService(DevicePolicyManager::class.java)
    private val crossProfile: CrossProfileApps = context.getSystemService(CrossProfileApps::class.java)
    private val admin = ComponentName(context, CloneAdminReceiver::class.java)

    /** true = ye instance Clone Space ke andar chal raha hai. */
    val isCloneSpace: Boolean
        get() = dpm.isProfileOwnerApp(context.packageName)

    // ---------- Personal side ----------

    fun canCreateSpace(): Boolean = try {
        dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
    } catch (e: Exception) {
        true
    }

    fun provisionIntent(): Intent =
        Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
            .putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, admin)

    fun hasCloneSpace(): Boolean = try {
        crossProfile.targetUserProfiles.isNotEmpty()
    } catch (e: Exception) {
        false
    }

    fun openCloneSpace(): Boolean {
        return try {
            val user = crossProfile.targetUserProfiles.firstOrNull() ?: return false
            crossProfile.startMainActivity(ComponentName(context, MainActivity::class.java), user)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ---------- Virtual Space (alag engine app, 20 clone tak) ----------

    fun isVirtualInstalled(): Boolean = pm.getLaunchIntentForPackage(VIRTUAL_PACKAGE) != null

    fun openVirtualSpace(): Boolean {
        val intent = pm.getLaunchIntentForPackage(VIRTUAL_PACKAGE) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ---------- Clone Space side ----------

    /** Account add karne / apps install karne par koi rok-tok na ho. */
    fun applyOpenPolicies() {
        if (!isCloneSpace) return
        listOf(
            UserManager.DISALLOW_MODIFY_ACCOUNTS,
            UserManager.DISALLOW_INSTALL_APPS,
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            UserManager.DISALLOW_UNINSTALL_APPS,
        ).forEach { runCatching { dpm.clearUserRestriction(admin, it) } }
        runCatching { dpm.setAccountManagementDisabled(admin, "com.google", false) }
    }

    /** Phone ke saare launchable apps (clone hue ya na hue). */
    @Suppress("DEPRECATION")
    suspend fun loadApps(): List<AppItem> = withContext(Dispatchers.Default) {
        val flags = PackageManager.MATCH_UNINSTALLED_PACKAGES
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val fromLauncher = pm.queryIntentActivities(launcher, flags)
            .distinctBy { it.activityInfo.packageName }
            .filter { it.activityInfo.packageName != context.packageName }
            .map { ri ->
                val f = ri.activityInfo.applicationInfo.flags
                AppItem(
                    packageName = ri.activityInfo.packageName,
                    label = ri.loadLabel(pm).toString(),
                    icon = runCatching { ri.loadIcon(pm).toBitmap(96, 96).asImageBitmap() }.getOrNull(),
                    installedHere = (f and ApplicationInfo.FLAG_INSTALLED) != 0,
                    isSystem = f.isPureSystem(),
                )
            }

        val result = fromLauncher.ifEmpty {
            // Fallback: kuch phones par launcher query khaali aati hai
            pm.getInstalledApplications(flags)
                .filter { it.packageName != context.packageName && !it.flags.isPureSystem() }
                .map { ai ->
                    AppItem(
                        packageName = ai.packageName,
                        label = pm.getApplicationLabel(ai).toString(),
                        icon = runCatching { pm.getApplicationIcon(ai).toBitmap(96, 96).asImageBitmap() }.getOrNull(),
                        installedHere = (ai.flags and ApplicationInfo.FLAG_INSTALLED) != 0,
                        isSystem = false,
                    )
                }
        }
        result.sortedBy { it.label.lowercase() }
    }

    private fun Int.isPureSystem(): Boolean =
        (this and ApplicationInfo.FLAG_SYSTEM) != 0 && (this and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

    /** App ki copy Clone Space me install karo. */
    suspend fun clone(pkg: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { dpm.installExistingPackage(admin, pkg) }.getOrDefault(false)
    }

    /** Kitne packages install hue, uski ginti. */
    suspend fun clonePackages(pkgs: List<String>): Int = withContext(Dispatchers.IO) {
        pkgs.count { runCatching { dpm.installExistingPackage(admin, it) }.getOrDefault(false) }
    }

    fun openApp(pkg: String): Boolean {
        val intent = pm.getLaunchIntentForPackage(pkg) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching { context.startActivity(intent) }.isSuccess
    }

    fun uninstallIntent(pkg: String): Intent =
        Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun addGoogleAccountIntent(): Intent =
        Intent(Settings.ACTION_ADD_ACCOUNT)
            .putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
