package com.pipe_network.app.infrastructure.models

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.pipe_network.app.application.factories.PeerConnectionFactory
import com.pipe_network.app.application.factories.SaltyRTCFactory
import com.pipe_network.app.application.handlers.PeerConnectionHandler
import com.pipe_network.app.application.observers.PipeConnectionObserver
import com.pipe_network.app.application.webrtc.DataChannelContext
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

@RequiresApi(Build.VERSION_CODES.N)
@SuppressLint("LongLogTag")
class ResponderPipeConnection(
    context: Context,
    pipeConnectionObserver: PipeConnectionObserver,
    sslContext: SSLContext,
    cryptoProvider: CryptoProvider,
    keyStore: KeyStore,
    initiatorPublicKey: String,
) : PipeConnection {
    private val saltyRTC = SaltyRTCFactory.createAsResponder(
        sslContext,
        cryptoProvider,
        keyStore,
        initiatorPublicKey,
    )

    private var isConnected = false

    lateinit var task: WebRTCTask
    lateinit var peerConnection: PeerConnection
    lateinit var dataChannelContext: DataChannelContext

    init {
        saltyRTC.events.signalingStateChanged.register { signalingChangeEvent ->
            pipeConnectionObserver.onSignalingStateChanged(signalingChangeEvent)

            if (signalingChangeEvent.state == SignalingState.TASK) {
                task = saltyRTC.task as WebRTCTask
                peerConnection = PeerConnectionFactory.createAsResponder(
                    context,
                    task,
                    pipeConnectionObserver,
                ) { dataChannel ->
                    dataChannelContext = PeerConnectionHandler(
                        peerConnection,
                        task,
                    ).createSecureDataChannelContext(
                        dataChannel,
                        { message ->
                            pipeConnectionObserver.onDataChannelContextMessage(message.array())
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
                }
                PeerConnectionHandler(peerConnection, task).setupTaskDataChannel()
                Log.d(TAG, "Set task and peerConnection")
            }
            return@register false
        }

        saltyRTC.events.handover.register {
            Log.d(TAG, "Handover")
            pipeConnectionObserver.onHandover()
            return@register true
        }
    }

    override fun connect() {
        isConnected = true
        return saltyRTC.connect()
    }

    override fun isConnected(): Boolean {
        return isConnected
    }

    override fun disconnect() {
        isConnected = false
        return saltyRTC.disconnect()
    }

    override fun sendMessage(byteArray: ByteArray) {
        return dataChannelContext.send(ByteBuffer.wrap(byteArray))
    }

    override fun sendMessageAsync(byteArray: ByteArray): CompletableFuture<*> {
        return dataChannelContext.sendAsync(ByteBuffer.wrap(byteArray))
    }

    companion object {
        const val TAG = "ResponderPipeConnectionScenario"
    }
}