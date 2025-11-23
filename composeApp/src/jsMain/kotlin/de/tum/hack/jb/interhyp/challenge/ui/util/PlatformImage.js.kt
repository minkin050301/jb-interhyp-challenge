package de.tum.hack.jb.interhyp.challenge.ui.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image

external fun encodeURIComponent(str: String): String

actual fun byteArrayToImageBitmap(bytes: ByteArray): ImageBitmap? {
    return try {
        Image.makeFromEncoded(bytes).toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}

actual fun formatUrlForCrossDomain(url: String): String {
    // Use a CORS proxy for web requests to external images
    // Using corsproxy.io as it's reliable and simple
    val result = if (url.startsWith("http") && !url.contains("corsproxy.io")) {
        val encoded = encodeURIComponent(url)
        "https://corsproxy.io/?$encoded"
    } else {
        url
    }
    println("formatUrlForCrossDomain: $url -> $result")
    return result
}
