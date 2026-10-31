package com.musecloner.app.ui

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.musecloner.app.MainViewModel

/** Personal side: Clone Space banane aur kholne ki screen. */
@Composable
fun SetupScreen(vm: MainViewModel) {
    val snackbar = remember { SnackbarHostState() }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        vm.onProvisionResult(result.resultCode == Activity.RESULT_OK)
    }

    LaunchedEffect(Unit) { vm.messages.collect { snackbar.showSnackbar(it) } }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.refresh() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        snackbarHost = { SnackbarHost(snackbar, Modifier.navigationBarsPadding()) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            GradientHeader {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LogoBadge()
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Muse Cloner", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Apps ki alag copy, alag data",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 14.sp,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                StatusPill(
                    text = if (vm.hasSpace) "Clone Space ready" else "Clone Space abhi nahi bana",
                    ok = vm.hasSpace,
                )
            }

            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StepCard(
                    number = 1,
                    title = "Clone Space banao",
                    body = "Phone me ek alag space banta hai jahan apps ki dusri copy fresh install jaisi chalti hai.",
                ) {
                    if (vm.hasSpace) {
                        Text(
                            "Ban chuka hai",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    } else {
                        Button(
                            enabled = vm.canCreate,
                            onClick = {
                                try {
                                    launcher.launch(vm.manager.provisionIntent())
                                } catch (e: Exception) {
                                    vm.say("Is phone par Clone Space support nahi hai.")
                                }
                            },
                        ) { Text("Space banao") }
                        if (!vm.canCreate) {
                            Text(
                                "Abhi is phone par space nahi ban sakta (pehle se koi work profile ho sakta hai).",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }

                StepCard(
                    number = 2,
                    title = "Space kholo",
                    body = "Space ke andar Muse Cloner khulega. Wahan se phone ke saare apps clone kar sakte ho.",
                ) {
                    Button(enabled = vm.hasSpace, onClick = { vm.openSpace() }) { Text("Space kholo") }
                }

                StepCard(
                    number = 3,
                    title = "Google login setup",
                    body = "Space ke andar \"Google login setup\" card se Google services on karo aur apna Gmail add karo. " +
                        "Uske baad \"Continue with Google\" me wahi accounts dikhenge.",
                )

                StepCard(
                    number = 4,
                    title = "Virtual Space (20 clone tak)",
                    body = "Ek hi app ke 20 clone chahiye to Virtual Space use karo. Har space alag hota hai aur uske andar " +
                        "Google services alag se install ho sakti hain. Ye alag APK hai, GitHub me " +
                        "\"Build Virtual Space APK\" workflow se banta hai.",
                ) {
                    if (vm.virtualInstalled) {
                        Button(onClick = { vm.openVirtual() }) { Text("Virtual Space kholo") }
                    } else {
                        Text(
                            "Abhi install nahi hai.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Dhyan rakho", fontWeight = FontWeight.SemiBold)
                        Text(
                            "Banking, Play Integrity aur kuch game apps clone me chalne se mana kar sakte hain. " +
                                "Kuch apps multi-account ko allow nahi karte, to apne risk par use karo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}
