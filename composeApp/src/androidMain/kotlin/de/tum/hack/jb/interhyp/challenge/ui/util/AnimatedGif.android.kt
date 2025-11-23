package de.tum.hack.jb.interhyp.challenge.ui.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Movie
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayInputStream

actual fun parseAnimatedGif(gifBytes: ByteArray): AnimatedGifFrames? {
    return try {
        val movie = Movie.decodeByteArray(gifBytes, 0, gifBytes.size)
        if (movie.duration() == 0) {
            // Not an animated GIF, return single frame
            val bitmap = BitmapFactory.decodeByteArray(gifBytes, 0, gifBytes.size)
            bitmap?.let {
                AnimatedGifFrames(
                    frames = listOf(it.asImageBitmap()),
                    frameDelays = listOf(100) // Default delay
                )
            }
        } else {
            val frames = mutableListOf<ImageBitmap>()
            val delays = mutableListOf<Int>()
            
            // Extract frames from the GIF
            val width = movie.width()
            val height = movie.height()
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Extract frames more accurately for 30 FPS animation
            val frameCount = 30 // Extract 30 frames for smooth 30 FPS animation
            val timeStep = if (movie.duration() > 0) movie.duration() / frameCount else 100
            
            for (i in 0 until frameCount) {
                val time = i * timeStep
                movie.setTime(time)
                canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
                movie.draw(canvas, 0f, 0f)
                
                val frameBitmap = Bitmap.createBitmap(bitmap)
                frames.add(frameBitmap.asImageBitmap())
                
                // 30 FPS = 33ms per frame
                delays.add(33)
            }
            
            if (frames.isEmpty()) {
                // Fallback: create single frame
                movie.setTime(0)
                canvas.drawColor(android.graphics.Color.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
                movie.draw(canvas, 0f, 0f)
                val frameBitmap = Bitmap.createBitmap(bitmap)
                frames.add(frameBitmap.asImageBitmap())
                delays.add(100)
            }
            
            AnimatedGifFrames(frames, delays)
        }
    } catch (e: Exception) {
        null
    }
}

