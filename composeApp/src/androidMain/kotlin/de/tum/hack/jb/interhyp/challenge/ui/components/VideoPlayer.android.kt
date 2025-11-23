package de.tum.hack.jb.interhyp.challenge.ui.components

import android.widget.VideoView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

@Composable
actual fun VideoPlayer(modifier: Modifier, videoBytes: ByteArray) {
    val context = LocalContext.current
    val videoFile = remember(videoBytes) {
        try {
            // Create temp file in cache directory
            val file = File.createTempFile("video", ".mp4", context.cacheDir)
            file.writeBytes(videoBytes)
            file.deleteOnExit()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    if (videoFile != null) {
        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoPath(videoFile.absolutePath)
                    setOnPreparedListener { mp ->
                        mp.start()
                        mp.isLooping = true
                    }
                    start()
                }
            }
        )
    }
}

