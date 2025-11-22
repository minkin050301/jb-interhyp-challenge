package de.tum.hack.jb.interhyp.challenge.data.network

import android.util.Base64

/**
 * Android implementation of Base64Encoder using Android's Base64 utility
 */
actual object PlatformBase64Encoder : Base64Encoder {
    override fun encode(data: ByteArray): String {
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }
    
    override fun decode(base64String: String): ByteArray {
        return Base64.decode(base64String, Base64.DEFAULT)
    }
}

