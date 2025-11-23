package de.tum.hack.jb.interhyp.challenge.ui.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

actual fun parseAnimatedGif(gifBytes: ByteArray): AnimatedGifFrames? {
    return try {
        // On Wasm/Skia, parsing animated GIFs frame-by-frame is complex without external libraries.
        // We fallback to showing just the first frame as a static image.
        val image = Image.makeFromEncoded(gifBytes)
        val bitmap = image.toComposeImageBitmap()
        AnimatedGifFrames(
            frames = listOf(bitmap),
            frameDelays = listOf(100)
        )
    } catch (e: Exception) {
        null
    }
}

