package com.pipe_network.app.application.factories

import com.pipe_network.app.config.Config
import org.saltyrtc.client.SaltyRTC
import org.saltyrtc.client.SaltyRTCBuilder
import org.saltyrtc.client.crypto.CryptoProvider
import org.saltyrtc.client.keystore.KeyStore
import org.saltyrtc.tasks.webrtc.WebRTCTaskBuilder
import org.saltyrtc.tasks.webrtc.WebRTCTaskVersion
import java.lang.Exception
import javax.net.ssl.SSLContext

class SaltyRTCFactory {
    companion object {
        private fun createSaltyRTCBase(
            sslContext: SSLContext,
            cryptoProvider: CryptoProvider,
            keyStore: KeyStore,
            peerTrustedKeyHex: String? = null,
        ): SaltyRTCBuilder {
            return SaltyRTCBuilder(cryptoProvider)
                .connectTo(Config.SERVER_ADDRESS_HOST, Config.SERVER_ADDRESS_PORT, sslContext)
                .withKeyStore(keyStore)
                .withTrustedPeerKey(peerTrustedKeyHex)
                .withPingInterval(Config.WEBSOCKET_PING_INTERVAL)
                .withWebsocketConnectTimeout(Config.WEBSOCKET_CONNECTION_TIMEOUT)
                .withServerKey(Config.SERVER_PUBLIC_KEY)
                .withWebSocketConnectAttemptsMax(Config.WEBSOCKET_ATTEMPTS_MAX)
                .usingTasks(
                    arrayOf(
                        WebRTCTaskBuilder()
                            .withVersion(WebRTCTaskVersion.V1)
                            .build()
                    )
                )
        }

        fun createAsInitiator(
            sslContext: SSLContext,
            cryptoProvider: CryptoProvider,
            keyStore: KeyStore,
            responderPublicKey: String,
        ): SaltyRTC {
            return createSaltyRTCBase(
                sslContext,
                cryptoProvider,
                keyStore,
                responderPublicKey,
            ).asInitiator() ?: throw Exception("Could not create a SaltyRTC Responder")
        }

        fun createAsResponder(
            sslContext: SSLContext,
            cryptoProvider: CryptoProvider,
            keyStore: KeyStore,
            initiatorPublicKey: String,
        ): SaltyRTC {
            return createSaltyRTCBase(
                sslContext,
                cryptoProvider,
                keyStore,
                initiatorPublicKey,
            ).asResponder() ?: throw Exception("Could not create a SaltyRTC Responder")
        }
    }
}
