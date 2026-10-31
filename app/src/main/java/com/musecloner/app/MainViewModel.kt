package com.musecloner.app

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

enum class AppFilter(val label: String) {
    ALL("Sab"),
    CLONED("Cloned"),
    AVAILABLE("Baki"),
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    val manager = CloneManager(app)

    /** true = Clone Space ke andar, false = personal side. */
    val isCloneSpace: Boolean = manager.isCloneSpace

    // Clone Space state
    var apps by mutableStateOf<List<AppItem>>(emptyList())
        private set
    var loading by mutableStateOf(false)
        private set
    var busy by mutableStateOf<Set<String>>(emptySet())
        private set
    var query by mutableStateOf("")
    var filter by mutableStateOf(AppFilter.ALL)
    var showSystem by mutableStateOf(false)

    // Personal side state
    var hasSpace by mutableStateOf(false)
        private set
    var canCreate by mutableStateOf(true)
        private set
    var virtualInstalled by mutableStateOf(false)
        private set

    val messages = MutableSharedFlow<String>(extraBufferCapacity = 8)

    init {
        if (isCloneSpace) {
            viewModelScope.launch(Dispatchers.IO) { manager.applyOpenPolicies() }
        }
    }

    fun say(msg: String) {
        messages.tryEmit(msg)
    }

    fun refresh() {
        if (isCloneSpace) {
            viewModelScope.launch {
                loading = apps.isEmpty()
                apps = manager.loadApps()
                loading = false
            }
        } else {
            hasSpace = manager.hasCloneSpace()
            canCreate = manager.canCreateSpace()
            virtualInstalled = manager.isVirtualInstalled()
        }
    }

    fun clone(item: AppItem) {
        if (item.packageName in busy) return
        busy = busy + item.packageName
        viewModelScope.launch {
            val ok = manager.clone(item.packageName)
            busy = busy - item.packageName
            if (ok) {
                apps = apps.map { if (it.packageName == item.packageName) it.copy(installedHere = true) else it }
                say("${item.label} clone ho gaya")
            } else {
                say("${item.label} clone nahi ho paya")
            }
        }
    }

    fun enableGoogleServices() {
        viewModelScope.launch {
            val n = manager.clonePackages(GOOGLE_PACKAGES)
            say(
                if (n > 0) "Google services on: $n/${GOOGLE_PACKAGES.size}"
                else "Google services nahi mili. Phone me Google apps hone chahiye."
            )
            refresh()
        }
    }

    fun onProvisionResult(ok: Boolean) {
        say(
            if (ok) "Clone Space ban gaya. Ab \"Space kholo\" dabao."
            else "Clone Space nahi bana (cancel ya error)."
        )
        refresh()
    }

    fun openVirtual() {
        if (!manager.openVirtualSpace()) {
            say("Virtual Space install nahi hai ya khul nahi paya.")
        }
    }

    fun openSpace() {
        if (!manager.openCloneSpace()) {
            say("Space nahi khula. Apps ki list me Work tab se Muse Cloner kholo.")
        }
    }
}
