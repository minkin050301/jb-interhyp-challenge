package de.tum.hack.jb.interhyp.challenge.data.network

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataWithBytes
import platform.posix.memcpy

/**
 * iOS implementation of Base64Encoder using Foundation's NSData
 */
@OptIn(ExperimentalForeignApi::class)
actual object PlatformBase64Encoder : Base64Encoder {
    override fun encode(data: ByteArray): String {
        val nsData = data.usePinned { pinned ->
            NSData.dataWithBytes(pinned.addressOf(0), data.size.toULong())
        }
        return nsData.base64EncodedStringWithOptions(0u)
    }
    
    override fun decode(base64String: String): ByteArray {
        val nsData = NSData.create(base64Encoding = base64String)
            ?: throw IllegalArgumentException("Invalid Base64 string")
        
        val byteArray = ByteArray(nsData.length.toInt())
        byteArray.usePinned { pinned ->
            memcpy(pinned.addressOf(0), nsData.bytes, nsData.length)
        }
        return byteArray
    }
}

