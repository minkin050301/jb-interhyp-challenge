package de.tum.hack.jb.interhyp.challenge.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * App-wide theme that supports Light, Dark, and System preference modes.
 * 
 * @param themePreference The user's theme preference (LIGHT, DARK, or SYSTEM)
 * @param content The composable content to apply the theme to
 */
@Composable
fun AppTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = themePreference.toDarkTheme()
    val colors = if (darkTheme) darkColorScheme() else lightColorScheme()
    
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
