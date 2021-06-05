package com.pipe_network.app.application.observers

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.pipe_network.app.application.webrtc.DataChannelContext
import org.webrtc.DataChannel
import org.webrtc.DataChannel.*

class SecureDataChannelObserver(
    private val dataChannel: DataChannel,
    private val dataChannelContext: DataChannelContext,
) : Observer {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBufferedAmountChange(bufferedAmount: Long) {
        // Forward buffered amount to flow control
        dataChannelContext.fcdc.bufferedAmountChange()
    }

    @SuppressLint("LongLogTag")
    override fun onStateChange() {
        when (dataChannel.state()) {
            State.CONNECTING -> Log.d(
                TAG,
                "Data channel " + dataChannel.label() + " connecting"
            )
            State.OPEN -> Log.i(
                TAG,
                "Data channel " + dataChannel.label() + " open"
            )
            State.CLOSING -> Log.d(
                TAG,
                "Data channel " + dataChannel.label() + " closing"
            )
            State.CLOSED -> {
                Log.i(TAG, "Data channel " + dataChannel.label() + " closed")
                dataChannel.dispose()
            }
            else -> Log.e(TAG, "Data channel is null")
        }
    }

    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMessage(buffer: Buffer) {
        Log.d(TAG, "Received message")
        // Reassemble chunks to message
        dataChannelContext.receive(buffer.data)
    }
    
    companion object {
        const val TAG = "SecureDataChannelObserver"
    }
}