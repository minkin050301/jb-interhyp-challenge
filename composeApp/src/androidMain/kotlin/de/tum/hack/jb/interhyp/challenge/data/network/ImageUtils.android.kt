package de.tum.hack.jb.interhyp.challenge.data.network

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * Android implementation for decoding image bytes to ImageBitmap
 */
actual fun decodeImageBitmap(imageBytes: ByteArray): ImageBitmap {
    val androidBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    return androidBitmap.asImageBitmap()
}
