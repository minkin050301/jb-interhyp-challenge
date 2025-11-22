package de.tum.hack.jb.interhyp.challenge

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// A minimal, modern dark color scheme for a clean look across platforms
private val DarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF69A1FF),       // modern blue accent
    onPrimary = Color(0xFF0A1018),
    secondary = Color(0xFF63E6BE),     // mint accent
    onSecondary = Color(0xFF0A1018),
    background = Color(0xFF0F1115),    // near-black with a hint of blue
    onBackground = Color(0xFFE6E9EF),  // soft white
    surface = Color(0xFF151922),       // deep slate for cards
    onSurface = Color(0xFFE6E9EF),
    surfaceVariant = Color(0xFF1D2330),
    onSurfaceVariant = Color(0xFFB2B9C6),
    outline = Color(0xFF2B3444),
    error = Color(0xFFFF6B6B),
    onError = Color(0xFF0A1018)
)

@Composable
fun DarkAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
