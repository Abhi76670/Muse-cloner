package com.musecloner.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F46E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = Color(0xFF1E1B4B),
    secondary = Color(0xFF0D9488),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCCFBF1),
    onSecondaryContainer = Color(0xFF042F2E),
    background = Color(0xFFF6F6FB),
    onBackground = Color(0xFF16162A),
    surface = Color.White,
    onSurface = Color(0xFF16162A),
    surfaceVariant = Color(0xFFEDEDF7),
    onSurfaceVariant = Color(0xFF55557A),
    error = Color(0xFFDC2626),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8B8CFF),
    onPrimary = Color(0xFF14134A),
    primaryContainer = Color(0xFF2E2C8A),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    secondaryContainer = Color(0xFF0F4F4A),
    onSecondaryContainer = Color(0xFFCCFBF1),
    background = Color(0xFF0E0E1A),
    onBackground = Color(0xFFEDEDF7),
    surface = Color(0xFF171726),
    onSurface = Color(0xFFEDEDF7),
    surfaceVariant = Color(0xFF232338),
    onSurfaceVariant = Color(0xFFB4B4D0),
    error = Color(0xFFF87171),
)

@Composable
fun MuseClonerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = Typography(),
        content = content,
    )
}
