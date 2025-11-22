package de.tum.hack.jb.interhyp.challenge.data.network

import kotlinx.browser.window
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

/**
 * JS implementation of Base64Encoder using browser's btoa/atob functions
 */
actual object PlatformBase64Encoder : Base64Encoder {
    override fun encode(data: ByteArray): String {
        // Convert ByteArray to binary string
        val binaryString = data.joinToString("") { (it.toInt() and 0xFF).toChar().toString() }
        return window.btoa(binaryString)
    }
    
    override fun decode(base64String: String): ByteArray {
        val binaryString = window.atob(base64String)
        return ByteArray(binaryString.length) { i ->
            binaryString[i].code.toByte()
        }
    }
}

