package de.tum.hack.jb.interhyp.challenge.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AudioToolbox.AudioServicesPlaySystemSound

class IosVibrator(private val scope: CoroutineScope) : Vibrator {
    override fun vibrate() {
        scope.launch {
            // Repeat the standard vibration to make it feel longer and stronger
            // kSystemSoundID_Vibrate = 4095
            val vibrationId = 4095u
            
            repeat(3) {
                AudioServicesPlaySystemSound(vibrationId)
                // Wait for the vibration to finish (approx 400-500ms) before triggering again
                delay(600)
            }
        }
    }
}

@Composable
actual fun rememberVibrator(): Vibrator {
    val scope = rememberCoroutineScope()
    return remember(scope) { IosVibrator(scope) }
}
