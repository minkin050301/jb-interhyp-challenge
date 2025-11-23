package de.tum.hack.jb.interhyp.challenge.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

class IosVibrator : Vibrator {
    override fun vibrate() {
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium)
        generator.prepare()
        generator.impactOccurred()
    }
}

@Composable
actual fun rememberVibrator(): Vibrator {
    return remember { IosVibrator() }
}

