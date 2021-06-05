package com.pipe_network.app.infrastructure.models

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.pipe_network.app.application.factories.PeerConnectionFactory
import com.pipe_network.app.application.factories.SaltyRTCFactory
import com.pipe_network.app.application.observers.PipeConnectionObserver
import com.pipe_network.app.application.webrtc.DataChannelContext
import com.pipe_network.app.config.Config
import com.pipe_network.app.application.handlers.PeerConnectionHandler
import com.pipe_network.app.domain.models.PipeConnection
import org.saltyrtc.client.crypto.CryptoProvider
import org.saltyrtc.client.keystore.KeyStore
import org.saltyrtc.client.signaling.state.SignalingState
import org.saltyrtc.tasks.webrtc.WebRTCTask
import org.webrtc.DataChannel
import org.webrtc.PeerConnection
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import javax.net.ssl.SSLContext

@SuppressLint("LongLogTag")
@RequiresApi(Build.VERSION_CODES.N)
class InitiatorPipeConnection(
    context: Context,
    pipeConnectionObserver: PipeConnectionObserver,
    sslContext: SSLContext,
    cryptoProvider: CryptoProvider,
    keyStore: KeyStore,
    responderPublicKey: String,
) : PipeConnection {

    private val saltyRTC = SaltyRTCFactory.createAsInitiator(
        sslContext,
        cryptoProvider,
        keyStore,
        responderPublicKey,
    )

    private var isConnected: Boolean = false

    lateinit var task: WebRTCTask
    lateinit var peerConnection: PeerConnection
    lateinit var dataChannelContext: DataChannelContext

    init {
        saltyRTC.events.signalingStateChanged.register {
            pipeConnectionObserver.onSignalingStateChanged(it)

            if (it.state == SignalingState.TASK) {
                task = saltyRTC.task as WebRTCTask
                peerConnection = PeerConnectionFactory.createAsInitiator(
                    context,
                    task,
                    pipeConnectionObserver,
                )
                PeerConnectionHandler(peerConnection, task).setupTaskDataChannel()
                Log.d(TAG, "Set task and peerConnection")
            }
            return@register false
        }

        saltyRTC.events.handover.register {
            Log.d(TAG, "Handover")
            pipeConnectionObserver.onHandover()
            val parameters = DataChannel.Init()
            parameters.protocol = task.transportLink.protocol
            val dataChannel = peerConnection.createDataChannel(Config.DC_LABEL, parameters)
            dataChannelContext = PeerConnectionHandler(
                peerConnection,
                task,
            ).createSecureDataChannelContext(
                dataChannel,
                {
                    Log.d(TAG, "Received message")
                    pipeConnectionObserver.onDataChannelContextMessage(it.array())
                },
                object : DataChannel.Observer {
                    override fun onBufferedAmountChange(bufferedAmount: Long) {}
                    override fun onStateChange() {
                        pipeConnectionObserver.onDataChannelContextStateChange(
                            dataChannel.state()
                        )
                    }

                    override fun onMessage(buffer: DataChannel.Buffer?) {}
                }
            )
            return@register true
        }

    }

    override fun connect() {
        saltyRTC.connect()
        isConnected = true
    }

    override fun isConnected(): Boolean {
        return isConnected
    }

    override fun disconnect() {
        saltyRTC.disconnect()
        isConnected = false
    }

    override fun sendMessage(byteArray: ByteArray) {
        return dataChannelContext.send(ByteBuffer.wrap(byteArray))
    }

    override fun sendMessageAsync(byteArray: ByteArray): CompletableFuture<*> {
        return dataChannelContext.sendAsync(ByteBuffer.wrap(byteArray))
    }

    companion object {
        const val TAG = "InitiatorPipeConnectionScenario"
    }
}