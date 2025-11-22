package de.tum.hack.jb.interhyp.challenge.data.network

/**
 * WasmJS implementation of Base64Encoder
 * Uses JavaScript interop for Base64 encoding/decoding
 */
actual object PlatformBase64Encoder : Base64Encoder {
    override fun encode(data: ByteArray): String {
        // Convert ByteArray to binary string
        val binaryString = data.joinToString("") { (it.toInt() and 0xFF).toChar().toString() }
        return js("btoa")(binaryString) as String
    }
    
    override fun decode(base64String: String): ByteArray {
        val binaryString = js("atob")(base64String) as String
        return ByteArray(binaryString.length) { i ->
            binaryString[i].code.toByte()
        }
    }
}

