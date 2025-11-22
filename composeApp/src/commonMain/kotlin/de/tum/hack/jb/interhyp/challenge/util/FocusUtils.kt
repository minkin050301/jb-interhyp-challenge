package de.tum.hack.jb.interhyp.challenge.util

import androidx.compose.runtime.Composable

@Composable
expect fun getFocusManager(): FocusManager?

interface FocusManager {
    fun clearFocus()
}

