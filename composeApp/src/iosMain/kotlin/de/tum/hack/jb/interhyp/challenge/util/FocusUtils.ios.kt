package de.tum.hack.jb.interhyp.challenge.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalFocusManager

@Composable
actual fun getFocusManager(): FocusManager? {
    val focusManager = LocalFocusManager.current
    return object : FocusManager {
        override fun clearFocus() {
            focusManager.clearFocus()
        }
    }
}

