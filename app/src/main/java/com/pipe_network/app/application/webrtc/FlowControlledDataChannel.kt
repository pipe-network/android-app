package com.pipe_network.app.application.webrtc

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.saltyrtc.tasks.webrtc.exceptions.IllegalStateError
import org.webrtc.DataChannel
import java.util.concurrent.CompletableFuture


/**
 * Create a flow-controlled (sender side) data channel.
 *
 * @param dc The data channel to be flow-controlled
 * @param lowWaterMark The low water mark unpauses the data channel once
 * the buffered amount of bytes becomes less or equal to it.
 * @param highWaterMark The high water mark pauses the data channel once
 * the buffered amount of bytes becomes greater or equal to it.
 */
open class FlowControlledDataChannel @JvmOverloads internal constructor(
    private val dc: DataChannel,
    private val lowWaterMark: Long = (256 * 1024).toLong(),
    private val highWaterMark: Long = (1024 * 1024).toLong()
) {
    @RequiresApi(Build.VERSION_CODES.N)
    private var readyFuture: CompletableFuture<*> = CompletableFuture.completedFuture(null)

    /**
     * A future whether the data channel is ready to be written on.
     */

    @RequiresApi(Build.VERSION_CODES.N)
    fun ready(): CompletableFuture<*> {
        return readyFuture
    }

    /**
     * Write a message to the data channel's internal buffer for delivery to
     * the remote side.
     *
     * Important: Before calling this, the `ready` Promise must be awaited.
     *
     * @param message The message to be sent.
     * @throws IllegalStateError in case the data channel is currently paused.
     */
    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.N)
    open fun write(message: DataChannel.Buffer?) {
        // Note: Locked since the "onBufferedAmountChange" event may run in parallel to the send
        //       calls.
        synchronized(this) {

            // Throw if paused
            if (!readyFuture.isDone) {
                throw IllegalStateError("Unable to write, data channel is paused!")
            }

            // Try sending
            // Note: Technically we should be able to catch an Exception in case the
            //       underlying buffer is full. However, webrtc.org is utterly
            //       outdated and just closes when its buffer would overflow. Thus,
            //       we use a well-tested high water mark instead and try to never
            //       fill the buffer completely.
            if (!dc.send(message)) {
                // This should never happen...
                throw IllegalStateError("Unable to send... because... webrtc.org stuff")
            }

            // Pause once high water mark has been reached
            val bufferedAmount = dc.bufferedAmount()
            if (bufferedAmount >= highWaterMark) {
                readyFuture = CompletableFuture<Void>()
                Log.d(TAG, dc.label() + " paused (buffered=" + bufferedAmount + ")")
            }
        }
    }

    /**
     * Must be called when the data channel's buffered amount changed.
     */
    @SuppressLint("LongLogTag")
    @RequiresApi(Build.VERSION_CODES.N)
    fun bufferedAmountChange() {
        // Webrtc.org fires the bufferedAmountChange event from a different
        // thread (B) while locking the native send call on the current
        // thread (A). This leads to a deadlock if we try to lock this
        // instance from (B). So, this... pleasant workaround prevents
        // deadlocking the send call.
        CompletableFuture.runAsync {
            synchronized(this) {
                val bufferedAmount = dc.bufferedAmount()
                // Unpause once low water mark has been reached
                if (bufferedAmount <= lowWaterMark && !readyFuture.isDone()) {
                    Log.d(TAG, dc.label() + " resumed (buffered=" + bufferedAmount + ")")
                    readyFuture.complete(null)
                }
            }
        }
    }

    companion object {
        const val TAG = "FlowControlledDataChannel"
    }
}