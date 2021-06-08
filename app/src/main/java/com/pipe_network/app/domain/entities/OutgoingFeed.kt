package com.pipe_network.app.domain.entities

import java.math.BigInteger

data class OutgoingFeed(
    val uuid: String,
    val text: String,
    val timestamp: BigInteger,
    val picture: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OutgoingFeed

        if (uuid != other.uuid) return false
        if (text != other.text) return false
        if (timestamp != other.timestamp) return false
        if (picture != null) {
            if (other.picture == null) return false
            if (!picture.contentEquals(other.picture)) return false
        } else if (other.picture != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + (picture?.contentHashCode() ?: 0)
        return result
    }
}
