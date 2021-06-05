package com.pipe_network.app.application.handlers

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.pipe_network.app.application.observers.DataChannelObserver
import com.pipe_network.app.application.observers.SecureDataChannelObserver
import com.pipe_network.app.config.Config
import com.pipe_network.app.application.webrtc.ChunkMode
import com.pipe_network.app.application.webrtc.CryptoMode
import com.pipe_network.app.application.webrtc.DataChannelContext
import com.pipe_network.app.application.webrtc.SignalingTransportHandler
import com.pipe_network.app.application.webrtc.TaskMessageHandler
import com.pipe_network.app.application.webrtc.UnboundedFlowControlledDataChannel
import org.saltyrtc.chunkedDc.Unchunker
import org.saltyrtc.tasks.webrtc.WebRTCTask
import org.webrtc.*
import org.webrtc.PeerConnection


open class PeerConnectionHandler(
    private val peerConnection: PeerConnection,
    private val task: WebRTCTask,
) {
    fun setupTaskDataChannel() {
        // Bind task events
        task.setMessageHandler(TaskMessageHandler(peerConnection, task))
        val dataChannel = createDataChannel()
        val unboundedFlowControlledDataChannel = UnboundedFlowControlledDataChannel(dataChannel)

        // Bind events
        dataChannel.registerObserver(
            DataChannelObserver(
                dataChannel,
                unboundedFlowControlledDataChannel,
                task,
                SignalingTransportHandler(dataChannel, unboundedFlowControlledDataChannel),
            ),
        )
    }

    private fun createDataChannel(): DataChannel {
        val link = task.transportLink

        val parameters = DataChannel.Init()
        parameters.id = link.id
        parameters.negotiated = true
        parameters.ordered = true
        parameters.protocol = link.protocol
        return peerConnection.createDataChannel(link.label, parameters)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun createSecureDataChannelContext(
        dataChannel: DataChannel,
        messageListener: Unchunker.MessageListener,
        dataChannelObserver: DataChannel.Observer,
    ): DataChannelContext {
        Log.d(TAG, "Initiating secure data channel ${Config.DC_LABEL}")
        val dataChannelContext = DataChannelContext(
            CryptoMode.ENCRYPT_THEN_CHUNK,
            ChunkMode.UNRELIABLE_UNORDERED,
            dataChannel,
            task,
            messageListener,
        )
        val secureDataChannelObserver = SecureDataChannelObserver(dataChannel, dataChannelContext)
        dataChannel.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(bufferedAmount: Long) {
                dataChannelObserver.onBufferedAmountChange(bufferedAmount)
                secureDataChannelObserver.onBufferedAmountChange(bufferedAmount)
            }

            override fun onStateChange() {
                dataChannelObserver.onStateChange()
                secureDataChannelObserver.onStateChange()
            }

            override fun onMessage(buffer: DataChannel.Buffer) {
                dataChannelObserver.onMessage(buffer)
                secureDataChannelObserver.onMessage(buffer)
            }

        })
        return dataChannelContext
    }

    companion object {
        const val TAG = "PeerConnection"
    }
}