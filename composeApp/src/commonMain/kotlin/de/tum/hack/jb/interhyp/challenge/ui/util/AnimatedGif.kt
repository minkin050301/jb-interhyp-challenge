package de.tum.hack.jb.interhyp.challenge.ui.util

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.delay

/**
 * Expect function to parse animated GIF and return frames with delays
 */
expect fun parseAnimatedGif(gifBytes: ByteArray): AnimatedGifFrames?

data class AnimatedGifFrames(
    val frames: List<ImageBitmap>,
    val frameDelays: List<Int> // in milliseconds
)

/**
 * Composable to display an animated GIF
 */
@Composable
fun AnimatedGif(
    gifBytes: ByteArray?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    val gifFrames = remember(gifBytes) {
        gifBytes?.let { parseAnimatedGif(it) }
    }
    
    LaunchedEffect(gifFrames) {
        if (gifFrames != null && gifFrames.frames.isNotEmpty()) {
            while (true) {
                // 30 FPS = ~33ms per frame
                val frameDelay = 33L
                delay(frameDelay)
                currentFrameIndex = (currentFrameIndex + 1) % gifFrames.frames.size
            }
        }
    }
    
    if (gifFrames != null && gifFrames.frames.isNotEmpty()) {
        Image(
            bitmap = gifFrames.frames[currentFrameIndex],
            contentDescription = contentDescription,
            modifier = modifier
        )
    }
}

