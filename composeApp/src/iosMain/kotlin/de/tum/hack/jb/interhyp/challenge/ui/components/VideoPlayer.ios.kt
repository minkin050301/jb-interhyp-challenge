package de.tum.hack.jb.interhyp.challenge.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFoundation.*
import platform.CoreGraphics.CGRect
import platform.CoreGraphics.CGRectMake
import platform.CoreMedia.CMTimeMake
import platform.Foundation.NSData
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToFile
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(modifier: Modifier, videoBytes: ByteArray) {
    val url = remember(videoBytes) {
        val tmpDir = NSTemporaryDirectory()
        val fileName = "video_${videoBytes.contentHashCode()}.mp4"
        // removing trailing slash if present to be safe
        val cleanTmpDir = if (tmpDir.endsWith("/")) tmpDir.dropLast(1) else tmpDir
        val filePath = "$cleanTmpDir/$fileName"
        
        val data = videoBytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), videoBytes.size.toULong())
        }
        
        data.writeToFile(filePath, true)
        NSURL.fileURLWithPath(filePath)
    }

    val player = remember(url) { 
        val playerItem = AVPlayerItem(uRL = url)
        AVPlayer(playerItem = playerItem) 
    }

    DisposableEffect(player) {
        val observer = NSNotificationCenter.defaultCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = player.currentItem,
            queue = null
        ) { _ ->
            player.seekToTime(CMTimeMake(0, 1))
            player.play()
        }
        
        player.play()

        onDispose {
            NSNotificationCenter.defaultCenter.removeObserver(observer)
            player.pause()
            player.replaceCurrentItemWithPlayerItem(null)
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            PlayerUIView().apply {
                setPlayer(player)
            }
        },
        update = { view ->
            (view as? PlayerUIView)?.setPlayer(player)
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
private class PlayerUIView : UIView(frame = CGRectMake(0.0, 0.0, 0.0, 0.0)) {
    private val playerLayer = AVPlayerLayer()

    init {
        playerLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.addSublayer(playerLayer)
    }

    fun setPlayer(player: AVPlayer) {
        playerLayer.player = player
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        playerLayer.frame = bounds
        CATransaction.commit()
    }
}
