package de.tum.hack.jb.interhyp.challenge.ui.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun parseAnimatedGif(gifBytes: ByteArray): AnimatedGifFrames? {
    return try {
        // For iOS, we'll use a simpler approach: decode the GIF and extract frames
        // Note: Full GIF frame extraction on iOS requires more complex native code
        // For now, we'll decode it as a single animated image and let the system handle it
        // or extract frames using Skia if possible
        
        // Try to decode as animated GIF using Skia
        // Skia can decode GIFs but may not extract all frames easily
        // For a working solution, we'll create a simple frame extraction
        val skiaImage = Image.makeFromEncoded(gifBytes)
        skiaImage?.let {
            // For now, return single frame with animation delay
            // Full multi-frame support would require native iOS GIF parsing
            AnimatedGifFrames(
                frames = listOf(it.toComposeImageBitmap()),
                frameDelays = listOf(100) // Default delay
            )
        }
    } catch (e: Exception) {
        null
    }
}

