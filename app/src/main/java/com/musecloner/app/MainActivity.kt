package com.musecloner.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.musecloner.app.ui.CloneSpaceScreen
import com.musecloner.app.ui.MuseClonerTheme
import com.musecloner.app.ui.SetupScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Header gradient dark hai, isliye status bar ke icons hamesha light
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        setContent {
            MuseClonerTheme { AppRoot() }
        }
    }
}

@Composable
private fun AppRoot() {
    val vm: MainViewModel = viewModel()
    if (vm.isCloneSpace) CloneSpaceScreen(vm) else SetupScreen(vm)
}
