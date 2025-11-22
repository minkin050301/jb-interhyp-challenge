package de.tum.hack.jb.interhyp.challenge.data.network

/**
 * iOS implementation of Base64Encoder using a small pure Kotlin algorithm.
 * This avoids relying on Foundation symbol name differences across Kotlin/Native versions.
 */
actual object PlatformBase64Encoder : Base64Encoder {
    private const val BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    private val BASE64_INV = IntArray(256) { -1 }.apply {
        for (i in BASE64_CHARS.indices) {
            this[BASE64_CHARS[i].code] = i
        }
        this['='.code] = 0
    }

    override fun encode(data: ByteArray): String {
        if (data.isEmpty()) return ""
        val output = StringBuilder(((data.size + 2) / 3) * 4)
        var i = 0
        while (i < data.size) {
            val b0 = data[i].toInt() and 0xFF
            val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else -1
            val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else -1

            val c0 = b0 ushr 2
            val c1 = ((b0 and 0x03) shl 4) or (if (b1 >= 0) (b1 ushr 4) else 0)
            val c2 = if (b1 >= 0) (((b1 and 0x0F) shl 2) or (if (b2 >= 0) (b2 ushr 6) else 0)) else 64
            val c3 = if (b2 >= 0) (b2 and 0x3F) else 64

            output.append(BASE64_CHARS[c0])
            output.append(BASE64_CHARS[c1])
            output.append(if (c2 == 64) '=' else BASE64_CHARS[c2])
            output.append(if (c3 == 64) '=' else BASE64_CHARS[c3])

            i += 3
        }
        return output.toString()
    }

    override fun decode(base64String: String): ByteArray {
        if (base64String.isEmpty()) return ByteArray(0)

        // Remove whitespace
        val s = base64String.filterNot { it == '\n' || it == '\r' || it == '\t' || it == ' ' }
        if (s.length % 4 != 0) throw IllegalArgumentException("Invalid Base64 string length")

        val outLen = s.count { it != '=' } * 3 / 4
        val out = ByteArray(outLen)
        var outIndex = 0

        var i = 0
        while (i < s.length) {
            val c0 = BASE64_INV[s[i].code]
            val c1 = BASE64_INV[s[i + 1].code]
            val c2 = BASE64_INV[s[i + 2].code]
            val c3 = BASE64_INV[s[i + 3].code]
            if (c0 < 0 || c1 < 0 || c2 < 0 && s[i + 2] != '=' || c3 < 0 && s[i + 3] != '=') {
                throw IllegalArgumentException("Invalid Base64 character")
            }

            val b0 = (c0 shl 2) or (c1 ushr 4)
            out[outIndex++] = b0.toByte()

            if (s[i + 2] != '=') {
                val b1 = ((c1 and 0x0F) shl 4) or (c2 ushr 2)
                if (outIndex < out.size) out[outIndex++] = b1.toByte()
            }
            if (s[i + 3] != '=') {
                val b2 = ((c2 and 0x03) shl 6) or c3
                if (outIndex < out.size) out[outIndex++] = b2.toByte()
            }

            i += 4
        }
        return out
    }
}

