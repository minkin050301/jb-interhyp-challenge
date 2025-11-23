package de.tum.hack.jb.interhyp.challenge.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle

class IosVibrator : Vibrator {
    override fun vibrate() {
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
        generator.prepare()
        // Trigger multiple impacts for a longer/stronger effect since iOS doesn't allow setting duration
        generator.impactOccurred()
        
        // Small delays to create a "longer" vibration feel
        // Note: In a real app, using a timer/dispatch queue would be cleaner,
        // but for a quick effect in a synchronous call, we'll just trigger once heavily.
        // iOS Haptics are designed to be short and sharp. 
        // For "long" vibration on iOS, we'd typically need AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
        // but that is often rejected in App Store reviews for non-alert contexts.
        // Let's try the standard "Vibrate" system sound which is the strongest/longest available.
        
        // kSystemSoundID_Vibrate = 4095
        platform.AudioToolbox.AudioServicesPlaySystemSound(4095u)
    }
}

@Composable
actual fun rememberVibrator(): Vibrator {
    return remember { IosVibrator() }
}

