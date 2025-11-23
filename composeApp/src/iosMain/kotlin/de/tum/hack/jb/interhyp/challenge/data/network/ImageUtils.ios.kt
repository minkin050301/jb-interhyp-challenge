package de.tum.hack.jb.interhyp.challenge.data.network

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image as SkiaImage

/**
 * iOS implementation for decoding image bytes to ImageBitmap using Skia
 */
actual fun decodeImageBitmap(imageBytes: ByteArray): ImageBitmap {
    return SkiaImage.makeFromEncoded(imageBytes).toComposeImageBitmap()
}
