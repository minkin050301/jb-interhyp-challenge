package de.tum.hack.jb.interhyp.challenge.data.network

/**
 * WasmJS implementation of Base64Encoder
 * Uses JavaScript interop for Base64 encoding/decoding
 */

// External declarations for JavaScript's btoa and atob functions
@JsName("btoa")
private external fun btoa(input: String): String

@JsName("atob")
private external fun atob(input: String): String

actual object PlatformBase64Encoder : Base64Encoder {
    override fun encode(data: ByteArray): String {
        // Convert ByteArray to binary string
        val binaryString = data.joinToString("") { (it.toInt() and 0xFF).toChar().toString() }
        return btoa(binaryString)
    }
    
    override fun decode(base64String: String): ByteArray {
        val binaryString = atob(base64String)
        return ByteArray(binaryString.length) { i ->
            binaryString[i].code.toByte()
        }
    }
}

