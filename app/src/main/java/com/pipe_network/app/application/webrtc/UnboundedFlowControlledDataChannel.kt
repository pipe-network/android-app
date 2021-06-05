package com.pipe_network.app.application.webrtc

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.AnyThread
import androidx.annotation.RequiresApi
import org.webrtc.DataChannel
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

/**
 * A flow-controlled (sender side) data channel that allows to queue an
 * infinite amount of messages.
 *
 * While this cancels the effect of the flow control, it prevents the data
 * channel's underlying buffer from becoming saturated by queueing all messages
 * in application space.
 */
@AnyThread
class UnboundedFlowControlledDataChannel(dc: DataChannel) : FlowControlledDataChannel(dc) {
    @RequiresApi(Build.VERSION_CODES.N)
    private var queue: CompletableFuture<*> = this.ready()

    /**
     * Write a message to the data channel's internal or application buffer for
     * delivery to the remote side.
     *
     * Warning: This method is not thread-safe.
     *
     * @param message The message to be sent.
     */
    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun write(message: DataChannel.Buffer?) {
        // Note: This very simple technique allows for ordered message
        //       queueing by using future chaining.
        queue = queue.thenRunAsync {

            // Wait until ready
            try {
                this.ready().get()
            } catch (error: ExecutionException) {
                // Should not happen
                Log.e(TAG, "Woops!", error)
                return@thenRunAsync
            } catch (error: InterruptedException) {
                // Can happen when the channel has been closed abruptly
                Log.e(TAG, "Unable to send pending chunk! Channel closed abruptly?", error)
                return@thenRunAsync
            }

            // Write message
            super.write(message)
        }
        queue.exceptionally { error: Throwable? ->
            Log.e(TAG, "Exception in write queue", error)
            null
        }
    }

    companion object {
        const val TAG = "UnboundedFlowControlledDataChannel"
    }
}