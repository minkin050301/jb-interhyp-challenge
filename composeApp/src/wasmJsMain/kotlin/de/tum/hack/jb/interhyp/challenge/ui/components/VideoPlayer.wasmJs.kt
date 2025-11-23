package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement
import org.khronos.webgl.Int8Array
import org.w3c.dom.url.URL

@Composable
actual fun VideoPlayer(modifier: Modifier, videoBytes: ByteArray) {
    var videoElement by remember { mutableStateOf<HTMLVideoElement?>(null) }

    DisposableEffect(videoBytes) {
        val video = document.createElement("video") as HTMLVideoElement
        val style = video.style
        style.position = "fixed"
        style.top = "0px"
        style.left = "0px"
        style.width = "0px"
        style.height = "0px"
        style.objectFit = "contain"
        style.zIndex = "1000"
        
        video.controls = true
        video.autoplay = true
        video.loop = true
        video.muted = true // Required for autoplay in many browsers
        
        // Create Blob URL from ByteArray
        val url = createBlobUrl(videoBytes)
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
                val style = video.style
                style.left = "${position.x}px"
                style.top = "${position.y}px"
                style.width = "${size.width}px"
                style.height = "${size.height}px"
            }
        }
    )
}

private fun createBlobUrl(byteArray: ByteArray): String {
    val jsArray = Int8Array(byteArray.size)
    for (i in byteArray.indices) {
        jsArray[i] = byteArray[i]
    }
    return createBlobUrlFromInt8Array(jsArray)
}

private fun createBlobUrlFromInt8Array(data: Int8Array): String = js("""
    var blob = new Blob([data], { type: 'video/mp4' });
    return URL.createObjectURL(blob);
""")
