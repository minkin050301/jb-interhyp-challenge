package de.tum.hack.jb.interhyp.challenge.data.network

/**
 * Utility functions for image processing
 */
object ImageUtils {
    /**
     * Encode image bytes to Base64 string
     */
    fun encodeImageToBase64(imageBytes: ByteArray): String {
        return PlatformBase64Encoder.encode(imageBytes)
    }
    
    /**
     * Decode Base64 string to image bytes
     */
    fun decodeBase64ToImage(base64String: String): ByteArray {
        return PlatformBase64Encoder.decode(base64String)
    }
    
    /**
     * Get MIME type from file extension
     */
    fun getMimeTypeFromExtension(extension: String): String {
        return when (extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "webp" -> "image/webp"
            "heic" -> "image/heic"
            "heif" -> "image/heif"
            else -> "image/jpeg" // Default
        }
    }
    
    /**
     * Strip Base64 prefix if present (e.g., "data:image/png;base64,")
     */
    fun stripBase64Prefix(base64String: String): String {
        val prefix = base64String.substringBefore(",")
        return if (prefix.contains("base64")) {
            base64String.substringAfter(",")
        } else {
            base64String
        }
    }
}

