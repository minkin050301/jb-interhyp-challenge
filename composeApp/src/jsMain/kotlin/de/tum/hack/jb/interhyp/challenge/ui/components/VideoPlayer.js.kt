package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import kotlinx.browser.document
import org.khronos.webgl.Int8Array
import org.w3c.dom.HTMLVideoElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

@Composable
actual fun VideoPlayer(modifier: Modifier, videoBytes: ByteArray) {
    var videoElement by remember { mutableStateOf<HTMLVideoElement?>(null) }

    DisposableEffect(videoBytes) {
        val video = document.createElement("video") as HTMLVideoElement
        video.style.apply {
            position = "fixed"
            top = "0px"
            left = "0px"
            width = "0px"
            height = "0px"
            objectFit = "contain"
            zIndex = "1000"
        }
        video.controls = true
        video.autoplay = true
        video.loop = true
        video.muted = true
        video.playsInline = true
        
        // Convert ByteArray to JS Int8Array
        val array = videoBytes.unsafeCast<Int8Array>()
        
        val blob = Blob(arrayOf(array), BlobPropertyBag(type = "video/mp4"))
        val url = URL.createObjectURL(blob)
        video.src = url
        
        document.body?.appendChild(video)
        videoElement = video

        onDispose {
            video.pause()
            video.src = ""
            video.remove()
            URL.revokeObjectURL(url)
        }
    }

    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val position = coordinates.positionInWindow()
            val size = coordinates.size
            
            videoElement?.let { video ->
                video.style.left = "${position.x}px"
                video.style.top = "${position.y}px"
                video.style.width = "${size.width}px"
                video.style.height = "${size.height}px"
            }
        }
    )
}

