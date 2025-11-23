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
import platform.Foundation.NSTimer
import platform.Foundation.NSURL
import platform.Foundation.dataWithBytes
import platform.Foundation.writeToFile
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView

import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(modifier: Modifier, videoBytes: ByteArray) {
    val url = remember(videoBytes) {
        val tmpDir = NSTemporaryDirectory()
        // Use unique filename to avoid conflicts with existing players or file locks
        val uuid = NSProcessInfo.processInfo.globallyUniqueString
        val fileName = "video_${uuid}.mp4"
        // removing trailing slash if present to be safe
        val cleanTmpDir = if (tmpDir.endsWith("/")) tmpDir.dropLast(1) else tmpDir
        val filePath = "$cleanTmpDir/$fileName"
        
        if (videoBytes.isEmpty()) {
            println("VideoPlayer: Error - videoBytes is empty!")
        }

        val data = videoBytes.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), videoBytes.size.toULong())
        }
        
        val success = data.writeToFile(filePath, true)
        println("VideoPlayer: Writing ${videoBytes.size} bytes to $filePath. Success: $success")
        
        if (NSFileManager.defaultManager.fileExistsAtPath(filePath)) {
             println("VideoPlayer: File confirmed at $filePath")
        } else {
             println("VideoPlayer: File NOT found at $filePath after writing!")
        }
        
        NSURL.fileURLWithPath(filePath)
    }

    val player = remember(url) { 
        println("VideoPlayer: Creating player for URL: $url")
        
        // Configure Audio Session
        try {
            val session = AVAudioSession.sharedInstance()
            session.setCategory(AVAudioSessionCategoryPlayback, error = null)
        } catch (e: Exception) {
            println("VideoPlayer: Error setting up audio session: ${e.message}")
        }

        val asset = AVURLAsset.URLAssetWithURL(url, null)
        println("VideoPlayer: Asset playable: ${asset.playable}")
        println("VideoPlayer: Asset tracks: ${asset.tracks}")
        
        val playerItem = AVPlayerItem.playerItemWithAsset(asset)
        AVPlayer(playerItem = playerItem) 
    }

    DisposableEffect(player) {
        val notificationCenter = NSNotificationCenter.defaultCenter
        
        val endObserver = notificationCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = player.currentItem,
            queue = null
        ) { _ ->
            player.seekToTime(CMTimeMake(0, 1))
            player.play()
        }
        
        val failObserver = notificationCenter.addObserverForName(
            name = AVPlayerItemFailedToPlayToEndTimeNotification,
            `object` = player.currentItem,
            queue = null
        ) { notification ->
            val error = notification?.userInfo?.get("AVPlayerItemFailedToPlayToEndTimeErrorKey")
            println("VideoPlayer: Failed to play to end: $error")
        }
        
        val newErrorObserver = notificationCenter.addObserverForName(
            name = AVPlayerItemNewErrorLogEntryNotification,
            `object` = player.currentItem,
            queue = null
        ) { _ ->
             val errorLog = player.currentItem?.errorLog()
             println("VideoPlayer: New error log entry")
             errorLog?.events?.forEach { event ->
                 println("VideoPlayer: Error event: $event")
             }
        }
        
        // Add observer for when the player item becomes ready or fails
        val statusObserver = notificationCenter.addObserverForName(
            name = "AVPlayerItemStatusDidChangeNotification",
            `object` = player.currentItem,
            queue = null
        ) { _ ->
            when (player.currentItem?.status) {
                AVPlayerItemStatusReadyToPlay -> {
                    println("VideoPlayer: Status = Ready to Play")
                    println("VideoPlayer: Duration: ${player.currentItem?.duration}")
                }
                AVPlayerItemStatusFailed -> {
                    val error = player.currentItem?.error
                    println("VideoPlayer: Status = Failed")
                    println("VideoPlayer: Error code: ${error?.code}")
                    println("VideoPlayer: Error domain: ${error?.domain}")
                    println("VideoPlayer: Error description: ${error?.localizedDescription}")
                    println("VideoPlayer: Error failure reason: ${error?.localizedFailureReason}")
                }
                AVPlayerItemStatusUnknown -> {
                    println("VideoPlayer: Status = Unknown")
                }
            }
        }
        
        // Check status and errors initially and periodically
        if (player.currentItem?.error != null) {
            println("VideoPlayer: Initial error: ${player.currentItem?.error?.localizedDescription}")
        }
        
        println("VideoPlayer: Initial status: ${player.currentItem?.status}")
        
        // Check status after a short delay to catch initialization issues
        NSTimer.scheduledTimerWithTimeInterval(
            interval = 0.5,
            repeats = false
        ) { _ ->
            when (player.currentItem?.status) {
                AVPlayerItemStatusReadyToPlay -> {
                    println("VideoPlayer: Status after delay = Ready to Play")
                }
                AVPlayerItemStatusFailed -> {
                    val error = player.currentItem?.error
                    println("VideoPlayer: Status after delay = Failed")
                    println("VideoPlayer: Error code: ${error?.code}")
                    println("VideoPlayer: Error domain: ${error?.domain}")
                    println("VideoPlayer: Error description: ${error?.localizedDescription}")
                    println("VideoPlayer: Error failure reason: ${error?.localizedFailureReason}")
                    println("VideoPlayer: Error recovery suggestion: ${error?.localizedRecoverySuggestion}")
                }
                AVPlayerItemStatusUnknown -> {
                    println("VideoPlayer: Status after delay = Unknown")
                }
            }
        }

        player.play()

        onDispose {
            if (player.error != null) {
                println("VideoPlayer: Dispose error: ${player.error?.localizedDescription}")
            }
            notificationCenter.removeObserver(statusObserver)
            notificationCenter.removeObserver(endObserver)
            notificationCenter.removeObserver(failObserver)
            notificationCenter.removeObserver(newErrorObserver)
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
