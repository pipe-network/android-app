package com.pipe_network.app.application.webrtc

import com.goterl.lazysodium.LazySodium
import com.goterl.lazysodium.interfaces.Box.BEFORENMBYTES
import com.goterl.lazysodium.interfaces.Box.MACBYTES
import org.saltyrtc.client.crypto.CryptoException
import org.saltyrtc.client.crypto.CryptoInstance
import org.saltyrtc.client.crypto.CryptoProvider


class LazySodiumCryptoInstance(
    private val lazySodium: LazySodium,
    ownPrivateKey: ByteArray,
    otherPublicKey: ByteArray
) : CryptoInstance {
    private var sharedKey: ByteArray

    init {
        // Verify key lengths
        if (otherPublicKey.size != CryptoProvider.PUBLICKEYBYTES) {
            throw CryptoException("Invalid public key length")
        }
        if (ownPrivateKey.size != CryptoProvider.PRIVATEKEYBYTES) {
            throw CryptoException("Invalid private key length")
        }

        // Precalculate shared key
        val k = ByteArray(BEFORENMBYTES)
        val success: Boolean = lazySodium.cryptoBoxBeforeNm(k, otherPublicKey, ownPrivateKey)
        if (!success) {
            throw CryptoException("Could not precalculate shared key")
        }
        sharedKey = k
    }

    override fun encrypt(data: ByteArray, nonce: ByteArray): ByteArray {
        val ciphertext = ByteArray(data.size + MACBYTES)
        val success: Boolean = lazySodium.cryptoBoxEasyAfterNm(
            ciphertext, data, data.size.toLong(), nonce, sharedKey
        )
        if (!success) {
            throw CryptoException("Could not encrypt data")
        }
        return ciphertext
    }

    override fun decrypt(data: ByteArray, nonce: ByteArray): ByteArray {
        val plaintext = ByteArray(data.size - MACBYTES)
        val success: Boolean = lazySodium.cryptoBoxOpenEasyAfterNm(
            plaintext, data, data.size.toLong(), nonce, sharedKey
        )
        if (!success) {
            throw CryptoException("Could not decrypt data")
        }
        return plaintext
    }
}