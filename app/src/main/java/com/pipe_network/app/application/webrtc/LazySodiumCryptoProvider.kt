package com.pipe_network.app.application.webrtc

import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import org.saltyrtc.client.crypto.CryptoException
import org.saltyrtc.client.crypto.CryptoInstance
import org.saltyrtc.client.crypto.CryptoProvider
import javax.inject.Inject

class LazySodiumCryptoProvider @Inject constructor() : CryptoProvider {
    private val lazySodium = LazySodiumAndroid(SodiumAndroid())

    override fun generateKeypair(publickey: ByteArray, privatekey: ByteArray) {
        // Verify key lengths
        if (publickey.size != CryptoProvider.PUBLICKEYBYTES) {
            throw CryptoException("Invalid public key buffer length")
        }
        if (privatekey.size != CryptoProvider.PRIVATEKEYBYTES) {
            throw CryptoException("Invalid private key buffer length")
        }

        // Generate keypair
        val success = lazySodium.cryptoBoxKeypair(publickey, privatekey)
        if (!success) {
            throw CryptoException("Could not generate keypair")
        }
    }

    override fun derivePublicKey(privateKey: ByteArray): ByteArray {
        // Verify key lengths
        if (privateKey.size != CryptoProvider.PRIVATEKEYBYTES) {
            throw CryptoException("Invalid private key length")
        }

        // Derive public key from private key
        val publicKey = ByteArray(CryptoProvider.PUBLICKEYBYTES)
        val success = lazySodium.cryptoScalarMultBase(publicKey, privateKey)
        if (!success) {
            throw CryptoException("Could not derive public key")
        }
        return publicKey
    }

    override fun symmetricEncrypt(
        data: ByteArray, key: ByteArray, nonce: ByteArray
    ): ByteArray {
        // Verify key lengths
        if (key.size != CryptoProvider.SYMMKEYBYTES) {
            throw CryptoException("Invalid key length")
        }
        if (nonce.size != CryptoProvider.NONCEBYTES) {
            throw CryptoException("Invalid nonce length")
        }

        // Encrypt
        val output = ByteArray(data.size + CryptoProvider.BOXOVERHEAD)
        val success = lazySodium.cryptoSecretBoxEasy(
            output, data, data.size.toLong(), nonce, key
        )
        if (!success) {
            throw CryptoException("Could not encrypt data")
        }
        return output
    }

    override fun symmetricDecrypt(
        data: ByteArray, key: ByteArray, nonce: ByteArray
    ): ByteArray {
        // Verify key lengths
        if (key.size != CryptoProvider.SYMMKEYBYTES) {
            throw CryptoException("Invalid key length")
        }
        if (nonce.size != CryptoProvider.NONCEBYTES) {
            throw CryptoException("Invalid nonce length")
        }

        // Decrypt
        val decrypted = ByteArray(data.size - CryptoProvider.BOXOVERHEAD)
        val success = lazySodium.cryptoSecretBoxOpenEasy(
            decrypted, data, data.size.toLong(), nonce, key
        )
        if (!success) {
            throw CryptoException("Could not decrypt data")
        }
        return decrypted
    }

    override fun getInstance(ownPrivateKey: ByteArray, otherPublicKey: ByteArray): CryptoInstance {
        return LazySodiumCryptoInstance(lazySodium, ownPrivateKey, otherPublicKey)
    }
}