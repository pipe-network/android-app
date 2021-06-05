package com.pipe_network.app.application.webrtc

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.saltyrtc.tasks.webrtc.transport.SignalingTransportHandler
import org.webrtc.DataChannel
import java.nio.ByteBuffer

class SignalingTransportHandler(
    private val dc: DataChannel,
    private val ufcdc: UnboundedFlowControlledDataChannel,
) : SignalingTransportHandler {
    override fun getMaxMessageSize(): Long {
        // Sigh... still not supported by webrtc.org, so fallback to a
        // well-known (and, frankly, terribly small) value.
        return 64 * 1024
    }

    @SuppressLint("LongLogTag")
    override fun close() {
        Log.d(TAG, "Data channel " + dc.label() + " close request")
        dc.close()
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun send(message: ByteBuffer) {
        Log.d(
            TAG,
            "Data channel " + dc.label() + " outgoing signaling message of length " +
                    message.remaining()
        )

        ufcdc.write(DataChannel.Buffer(message, true))
    }

    companion object {
        const val TAG = "SignalingTransportHandler"
    }
}
