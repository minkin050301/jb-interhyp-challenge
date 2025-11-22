package de.tum.hack.jb.interhyp.challenge.data.network

/**
 * Platform-specific Base64 encoder interface.
 * Implementations should be provided for each platform.
 */
interface Base64Encoder {
    /**
     * Encode a ByteArray to a Base64 string
     */
    fun encode(data: ByteArray): String
    
    /**
     * Decode a Base64 string to a ByteArray
     */
    fun decode(base64String: String): ByteArray
}

/**
 * Expect declaration for platform-specific Base64Encoder
 */
expect object PlatformBase64Encoder : Base64Encoder

