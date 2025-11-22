package de.tum.hack.jb.interhyp.challenge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * App-wide theme that follows the system dark/light setting by default.
 *
 * Remove forced dark theme; this will pick darkColorScheme or lightColorScheme
 * based on the current platform setting. You can override by passing
 * a specific value to [darkTheme] later if needed.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
