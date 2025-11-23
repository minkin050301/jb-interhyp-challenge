package de.tum.hack.jb.interhyp.challenge.util

import androidx.compose.runtime.Composable

interface Vibrator {
    fun vibrate()
}

@Composable
expect fun rememberVibrator(): Vibrator

