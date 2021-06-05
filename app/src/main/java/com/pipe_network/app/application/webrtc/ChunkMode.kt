package com.pipe_network.app.application.webrtc

enum class ChunkMode {
    RELIABLE_ORDERED, UNRELIABLE_UNORDERED;

    override fun toString(): String {
        return when (this) {
            RELIABLE_ORDERED -> "reliable-ordered"
            UNRELIABLE_UNORDERED -> "unreliable-unordered"
        }
    }

    companion object {
        fun fromString(string: String?): ChunkMode? {
            return when (string) {
                "reliable-ordered" -> RELIABLE_ORDERED
                "unreliable-unordered" -> UNRELIABLE_UNORDERED
                else -> null
            }
        }
    }
}