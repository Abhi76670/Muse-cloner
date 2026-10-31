package com.musecloner.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.musecloner.app.AppFilter
import com.musecloner.app.MainViewModel

/** Clone Space ke andar: saare apps ki list + clone / open / remove. */
@Composable
fun CloneSpaceScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    var showAbout by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }

    val userApps = remember(vm.apps) { vm.apps.filter { !it.isSystem } }
    val clonedCount = remember(userApps) { userApps.count { it.installedHere } }

    val visible = remember(vm.apps, vm.query, vm.filter, vm.showSystem) {
        vm.apps.filter { vm.showSystem || !it.isSystem }
            .filter {
                when (vm.filter) {
                    AppFilter.ALL -> true
                    AppFilter.CLONED -> it.installedHere
                    AppFilter.AVAILABLE -> !it.installedHere
                }
            }
            .filter {
                vm.query.isBlank() ||
                    it.label.contains(vm.query, ignoreCase = true) ||
                    it.packageName.contains(vm.query, ignoreCase = true)
            }
    }

    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        snackbarHost = { SnackbarHost(snackbar, Modifier.navigationBarsPadding()) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            GradientHeader {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoBadge()
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Clone Space", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${clonedCount} / ${userApps.size} apps clone hue",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp,
                        )
                    }
                    IconButton(onClick = { vm.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                    IconButton(onClick = { showAbout = true }) {
                        Icon(Icons.Filled.Info, contentDescription = "Info", tint = Color.White)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = vm.query,
                onValueChange = { vm.query = it },
                singleLine = true,
                placeholder = { Text("App search karo") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (vm.query.isNotEmpty()) {
                        IconButton(onClick = { vm.query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppFilter.entries.forEach { f ->
                    FilterChip(
                        selected = vm.filter == f,
                        onClick = { vm.filter = f },
                        label = { Text(f.label) },
                    )
                }
                FilterChip(
                    selected = vm.showSystem,
                    onClick = { vm.showSystem = !vm.showSystem },
                    label = { Text("System apps") },
                )
            }

            Spacer(Modifier.height(8.dp))

            if (vm.loading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = navBottom + 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    item(key = "google") {
                        GoogleCard(
                            onEnableServices = { vm.enableGoogleServices() },
                            onAddAccount = {
                                try {
                                    context.startActivity(vm.manager.addGoogleAccountIntent())
                                } catch (e: Exception) {
                                    vm.say("Settings nahi khuli. Settings > Accounts me jao.")
                                }
                            },
                        )
                    }

                    if (visible.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                "Koi app nahi mila",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    items(visible, key = { it.packageName }) { app ->
                        AppRow(
                            item = app,
                            busy = app.packageName in vm.busy,
                            onClone = { vm.clone(app) },
                            onOpen = {
                                if (!vm.manager.openApp(app.packageName)) vm.say("App khul nahi paya")
                            },
                            onRemove = {
                                try {
                                    context.startActivity(vm.manager.uninstallIntent(app.packageName))
                                } catch (e: Exception) {
                                    vm.say("Clone hata nahi paya")
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            confirmButton = { TextButton(onClick = { showAbout = false }) { Text("Theek hai") } },
            title = { Text("Kaise kaam karta hai") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Ye Android ke Work Profile me apps ki dusri copy chalata hai. Har clone ka data alag hota hai, bilkul fresh install jaisa.")
                    Text("Google login: Clone Space ke accounts alag hote hain. Pehle \"Account add\" se apna Gmail yahan add karo, phir apps me wahi accounts dikhenge.")
                    Text("Banking, Play Integrity wale apps aur kuch games clone me block ho sakte hain.")
                }
            },
        )
    }
}

@Composable
private fun GoogleCard(onEnableServices: () -> Unit, onAddAccount: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Google login setup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                "\"Continue with Google\" ke liye pehle Google services on karo, phir apna Gmail add karo. Ye sirf ek baar karna hai.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onEnableServices, modifier = Modifier.weight(1f)) {
                    Text("1. Services on")
                }
                Button(onClick = onAddAccount, modifier = Modifier.weight(1f)) {
                    Text("2. Account add")
                }
            }
        }
    }
}
