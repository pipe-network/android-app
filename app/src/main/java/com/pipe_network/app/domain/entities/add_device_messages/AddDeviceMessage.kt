package com.pipe_network.app.domain.entities.add_device_messages

import com.goterl.lazysodium.interfaces.SecretBox

data class  AddDeviceMessage(
    val publicKey: ByteArray,
    val nonce: ByteArray,
    val message: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AddDeviceMessage

        if (!publicKey.contentEquals(other.publicKey)) return false
        if (!nonce.contentEquals(other.nonce)) return false
        if (!message.contentEquals(other.message)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + nonce.contentHashCode()
        result = 31 * result + message.contentHashCode()
        return result
    }

    fun pack(): ByteArray {
        val packedMessage = ByteArray(SecretBox.KEYBYTES + SecretBox.NONCEBYTES + message.size)
        var destinationOffset = 0

        publicKey.copyInto(packedMessage, destinationOffset)
        destinationOffset += SecretBox.KEYBYTES

        nonce.copyInto(packedMessage, destinationOffset)
        destinationOffset += SecretBox.NONCEBYTES

        message.copyInto(packedMessage, destinationOffset)
        return packedMessage
    }

    companion object {
        fun unpack(packedMessage: ByteArray): AddDeviceMessage {
            val publicKey = ByteArray(SecretBox.KEYBYTES)
            val nonce = ByteArray(SecretBox.NONCEBYTES)
            val message = ByteArray(packedMessage.size - (SecretBox.KEYBYTES + SecretBox.NONCEBYTES))
            var startingIndex = 0

            packedMessage.copyInto(
                destination = publicKey,
                startIndex = startingIndex,
                endIndex = startingIndex + SecretBox.KEYBYTES,
            )
            startingIndex += SecretBox.KEYBYTES

            packedMessage.copyInto(
                destination = nonce,
                startIndex = startingIndex,
                endIndex = startingIndex + SecretBox.NONCEBYTES,
            )
            startingIndex += SecretBox.NONCEBYTES

            packedMessage.copyInto(
                destination = message,
                startIndex = startingIndex,
            )
            return AddDeviceMessage(publicKey, nonce, message)
        }
    }
}