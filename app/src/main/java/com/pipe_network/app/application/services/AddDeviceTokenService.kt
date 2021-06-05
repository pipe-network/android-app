package com.pipe_network.app.application.services

import android.annotation.SuppressLint
import android.util.Log
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.goterl.lazysodium.LazySodiumAndroid
import com.goterl.lazysodium.SodiumAndroid
import com.goterl.lazysodium.interfaces.SecretBox
import com.pipe_network.app.application.repositories.ProfileRepository
import com.pipe_network.app.config.Config
import com.pipe_network.app.domain.entities.add_device_messages.AddDeviceControlMessage
import com.pipe_network.app.domain.entities.add_device_messages.AddDeviceMessage
import com.pipe_network.app.domain.entities.add_device_messages.AddDeviceRequestMessage
import com.pipe_network.app.domain.entities.add_device_messages.AddDeviceSolvedMessage
import com.pipe_network.app.infrastructure.models.Profile
import com.pipe_network.app.infrastructure.providers.TrustAllCertsSSLContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.msgpack.jackson.dataformat.MessagePackFactory
import org.saltyrtc.client.crypto.CryptoException
import java.net.URI
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.net.ssl.SSLContext

interface AddDeviceTokenService {
    fun newDeviceToken(token: String)
}

class AddDeviceTokenServiceImpl @Inject constructor(
    val profileRepository: ProfileRepository,
    @TrustAllCertsSSLContext private val sslContext: SSLContext,
) : AddDeviceTokenService {
    override fun newDeviceToken(token: String) {
        Log.d(TAG, "Adding new token to the server: $token")
        CoroutineScope(Dispatchers.IO).launch {
            val profile = profileRepository.get()

            if (profile != null) {
                val addDeviceTokenWebSocketClient = AddDeviceTokenWebSocketClient(
                    URI("wss://${Config.SERVER_ADDRESS_HOST}:${Config.SERVER_ADDRESS_PORT}/add-device-token"),
                    token,
                    profile,
                )
                addDeviceTokenWebSocketClient.setSocketFactory(sslContext.socketFactory)
                addDeviceTokenWebSocketClient.connect()
            }
        }
    }

    private class AddDeviceTokenWebSocketClient(
        serverURI: URI,
        val deviceToken: String,
        val profile: Profile,
    ) : WebSocketClient(serverURI) {
        private val objectMapper = ObjectMapper(MessagePackFactory()).registerKotlinModule()
        private val lazySodium = LazySodiumAndroid(SodiumAndroid())

        fun String.decodeHex(): ByteArray = chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()

        override fun onOpen(handshakedata: ServerHandshake?) {
            val addDeviceRequestMessage = AddDeviceRequestMessage()
            val addDeviceMessage = AddDeviceMessage(
                publicKey = profile.publicKey.decodeHex(),
                nonce = lazySodium.nonce(SecretBox.NONCEBYTES),
                message = objectMapper.writeValueAsBytes(addDeviceRequestMessage),
            )
            this.send(addDeviceMessage.pack())
        }

        @SuppressLint("LongLogTag")
        override fun onMessage(message: String?) {
            Log.d(TAG, "on string message: $message")
        }

        @SuppressLint("LongLogTag")
        override fun onMessage(message: ByteBuffer) {
            Log.d(TAG, "received bytebuffer message")
            val lazySodium = LazySodiumAndroid(SodiumAndroid())
            val incomingAddDeviceMessage = AddDeviceMessage.unpack(message.array())
            val decryptedAddDeviceControlMessage = ByteArray(
                SecretBox.MACBYTES + incomingAddDeviceMessage.message.size
            )
            val decryptResult = lazySodium.cryptoBoxOpenEasy(
                decryptedAddDeviceControlMessage,
                incomingAddDeviceMessage.message,
                incomingAddDeviceMessage.message.size.toLong(),
                incomingAddDeviceMessage.nonce,
                incomingAddDeviceMessage.publicKey,
                profile.privateKey.decodeHex()
            )

            if (!decryptResult) {
                throw CryptoException("could not decrypt data")
            }

            val addDeviceControlMessage = objectMapper.readValue(
                decryptedAddDeviceControlMessage,
                AddDeviceControlMessage::class.java,
            )
            val addDeviceSolvedMessage = AddDeviceSolvedMessage(
                uuid = addDeviceControlMessage.uuid,
                deviceToken = deviceToken,
            )
            val addDeviceSolvedMessagePack = objectMapper.writeValueAsBytes(
                addDeviceSolvedMessage
            )
            val randomNonce = lazySodium.nonce(SecretBox.NONCEBYTES)
            val encryptedAddDeviceSolvedMessage = ByteArray(
                SecretBox.MACBYTES + addDeviceSolvedMessagePack.size
            )
            val encryptResult = lazySodium.cryptoBoxEasy(
                encryptedAddDeviceSolvedMessage,
                addDeviceSolvedMessagePack,
                addDeviceSolvedMessagePack.size.toLong(),
                randomNonce,
                incomingAddDeviceMessage.publicKey,
                profile.privateKey.decodeHex(),
            )
            if (!encryptResult) {
                throw CryptoException("could not encrypt data")
            }
            val outgoingAddDeviceMessage = AddDeviceMessage(
                publicKey = profile.publicKey.decodeHex(),
                nonce = randomNonce,
                message = encryptedAddDeviceSolvedMessage,
            )
            this.send(outgoingAddDeviceMessage.pack())
        }

        @SuppressLint("LongLogTag")
        override fun onClose(code: Int, reason: String?, remote: Boolean) {
            Log.d(TAG, "closed websocket connection with code: $code and reason: $reason")
        }

        @SuppressLint("LongLogTag")
        override fun onError(ex: Exception?) {
            Log.d(TAG, "error occured in websocket connection: ${ex?.message ?: "no exception"}")
        }

        companion object {
            const val TAG = "AddDeviceWebSocketClient"
        }
    }

    companion object {
        const val TAG = "AddDeviceTokenService"
    }
}