package com.pipe_network.app.application.webrtc

enum class CryptoMode {
    NONE, CHUNK_THEN_ENCRYPT, ENCRYPT_THEN_CHUNK;

    override fun toString(): String {
        return when (this) {
            NONE -> "none"
            CHUNK_THEN_ENCRYPT -> "chunk-then-encrypt"
            ENCRYPT_THEN_CHUNK -> "encrypt-then-chunk"
        }
    }

    companion object {
        fun fromString(string: String?): CryptoMode? {
            return when (string) {
                "none" -> NONE
                "chunk-then-encrypt" -> CHUNK_THEN_ENCRYPT
                "encrypt-then-chunk" -> ENCRYPT_THEN_CHUNK
                else -> null
            }
        }
    }
}