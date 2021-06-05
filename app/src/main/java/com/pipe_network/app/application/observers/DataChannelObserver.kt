package com.pipe_network.app.application.observers

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.pipe_network.app.application.webrtc.UnboundedFlowControlledDataChannel
import com.pipe_network.app.application.handlers.PeerConnectionHandler
import org.saltyrtc.client.signaling.CloseCode
import org.saltyrtc.tasks.webrtc.WebRTCTask
import org.saltyrtc.tasks.webrtc.exceptions.UntiedException
import org.saltyrtc.tasks.webrtc.transport.SignalingTransportHandler
import org.webrtc.DataChannel

class DataChannelObserver(
    private val dataChannel: DataChannel,
    private val unboundedFlowControlledDataChannel: UnboundedFlowControlledDataChannel,
    private val task: WebRTCTask,
    private val handler: SignalingTransportHandler,
) : DataChannel.Observer {
    private var dcOpened = false

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBufferedAmountChange(bufferedAmount: Long) {
        unboundedFlowControlledDataChannel.bufferedAmountChange()
    }

    override fun onStateChange() {
        when (dataChannel.state()) {
            DataChannel.State.CONNECTING -> Log.d(
                PeerConnectionHandler.TAG,
                "Data channel " + dataChannel.label() + " connecting"
            )
            DataChannel.State.OPEN -> if (dcOpened) {
                Log.e(
                    PeerConnectionHandler.TAG,
                    "Data channel " + dataChannel.label() + " re-opened"
                )
            } else {
                dcOpened = true
                Log.i(PeerConnectionHandler.TAG, "Data channel " + dataChannel.label() + " open")
                task.handover(handler)
            }
            DataChannel.State.CLOSING -> if (!dcOpened) {
                Log.e(PeerConnectionHandler.TAG, "Data channel " + dataChannel.label() + " closing")
            } else {
                Log.d(PeerConnectionHandler.TAG, "Data channel " + dataChannel.label() + " closing")
                try {
                    task.transportLink.closing()
                } catch (error: UntiedException) {
                    Log.w(PeerConnectionHandler.TAG, "Could not move into closing state", error)
                }
            }
            DataChannel.State.CLOSED -> {
                if (!dcOpened) {
                    Log.e(
                        PeerConnectionHandler.TAG,
                        "Data channel " + dataChannel.label() + " closed"
                    )
                } else {
                    Log.i(
                        PeerConnectionHandler.TAG,
                        "Data channel " + dataChannel.label() + " closed"
                    )
                    try {
                        task.transportLink.closed()
                    } catch (error: UntiedException) {
                        // Note: We can safely ignore this because, in
                        //       our case, the signalling instance may
                        //       be closed before the channel has been
                        //       through the closing sequence.
                    }
                }
                dataChannel.dispose()
            }
            null -> {
                Log.e(PeerConnectionHandler.TAG, "Data channel state is null!")
            }
        }
    }

    override fun onMessage(buffer: DataChannel.Buffer) {
        if (!buffer.binary) {
            task.close(CloseCode.PROTOCOL_ERROR)
        } else {
            try {
                task.transportLink.receive(buffer.data)
            } catch (error: UntiedException) {
                Log.w(
                    PeerConnectionHandler.TAG,
                    "Could not feed incoming data to the transport link",
                    error
                )
            }
        }
    }
}