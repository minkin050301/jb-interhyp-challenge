package de.tum.hack.jb.interhyp.challenge.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable

/**
 * Theme preference options for the app
 */
enum class ThemePreference {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Converts ThemePreference to a boolean for MaterialTheme
 * When SYSTEM is selected, uses the system preference
 */
@Composable
fun ThemePreference.toDarkTheme(): Boolean {
    return when (this) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
}

