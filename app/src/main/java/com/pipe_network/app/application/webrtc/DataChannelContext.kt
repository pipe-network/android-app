package com.pipe_network.app.application.webrtc

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import org.saltyrtc.chunkedDc.Chunker
import org.saltyrtc.chunkedDc.Unchunker
import org.saltyrtc.chunkedDc.Unchunker.MessageListener
import org.saltyrtc.client.crypto.CryptoException
import org.saltyrtc.client.exceptions.OverflowException
import org.saltyrtc.client.exceptions.ProtocolException
import org.saltyrtc.client.exceptions.ValidationError
import org.saltyrtc.client.keystore.Box
import org.saltyrtc.tasks.webrtc.WebRTCTask
import org.saltyrtc.tasks.webrtc.crypto.DataChannelCryptoContext
import org.webrtc.DataChannel
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException


@RequiresApi(Build.VERSION_CODES.N)
class DataChannelContext(
    private var cryptoMode: CryptoMode,
    chunkMode: ChunkMode,
    private var dataChannel: DataChannel,
    task: WebRTCTask,
    private val messageListener: MessageListener,
) {

    val fcdc: FlowControlledDataChannel = FlowControlledDataChannel(dataChannel)
    private var crypto: DataChannelCryptoContext? = null

    private val unchunker: Unchunker

    private var queue: CompletableFuture<*>
    private val chunkLength: Int
    private var messageId: Long = 0

    init {
        // Wrap as flow-controlled data channel
        crypto = when (cryptoMode) {
            CryptoMode.CHUNK_THEN_ENCRYPT, CryptoMode.ENCRYPT_THEN_CHUNK -> task.createCryptoContext(
                dataChannel.id()
            )
            else -> null
        }

        // Create unchunker
        // TODO: Add support for reliable/ordered
        if (chunkMode !== ChunkMode.UNRELIABLE_UNORDERED) {
            throw RuntimeException("Unsupported mode: $chunkMode")
        }
        unchunker = Unchunker()
        unchunker.onMessage { buffer: ByteBuffer ->
            val decryptedBuffer: ByteBuffer
            // Decrypt message (if needed)
            if (this.cryptoMode === CryptoMode.ENCRYPT_THEN_CHUNK) {
                val box = Box(buffer, DataChannelCryptoContext.NONCE_LENGTH)
                try {
                    decryptedBuffer = ByteBuffer.wrap(crypto!!.decrypt(box))
                    // Hand out message
                    Log.d(
                        TAG,
                        ("Data channel " + dataChannel.label() + " incoming message of length "
                                + decryptedBuffer.remaining())
                    )
                    messageListener.onMessage(decryptedBuffer)
                } catch (error: ValidationError) {
                    Log.e(TAG, "Invalid packet received", error)
                    return@onMessage
                } catch (error: ProtocolException) {
                    Log.e(TAG, "Invalid packet received", error)
                    return@onMessage
                } catch (error: CryptoException) {
                    Log.e(TAG, "Unable to encrypt", error)
                    return@onMessage
                }
            }
        }

        // Determine chunk length
        // Note: Hard-coded because webrtc.org...
        // Important: We need to do this here because the "open" state may not
        //            be fired in case we're receiving a data channel.
        chunkLength = 64 * 1024

        // Initialise queue
        queue = fcdc.ready()
    }

    /**
     * Enqueue an operation to be run in order on this channel's write queue.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    fun enqueue(operation: Runnable?): CompletableFuture<*> {
        queue = queue.thenRunAsync(operation)
        queue.exceptionally { error ->
            Log.e(TAG, "Exception in write queue", error)
            null
        }
        return queue
    }

    /**
     * Send a message asynchronously via this channel's write queue. The
     * message will be fragmented into chunks.
     */

    @RequiresApi(Build.VERSION_CODES.N)
    fun sendAsync(buffer: ByteBuffer): CompletableFuture<*> {
        return enqueue {
            try {
                send(buffer)
            } catch (error: OverflowException) {
                Log.e(TAG, "CSN overflow", error)
            } catch (error: CryptoException) {
                Log.e(TAG, "Unable to encrypt", error)
            }
        }
    }

    /**
     * Send a message synchronously, fragmented into chunks.
     */
    @Throws(OverflowException::class, CryptoException::class)
    fun send(buffer: ByteBuffer) {
        var buffer = buffer
        Log.d(
            TAG,
            "Data channel " + dataChannel.label() + " outgoing message of length " +
                    buffer.remaining()
        )

        // Encrypt message (if needed)
        if (cryptoMode === CryptoMode.ENCRYPT_THEN_CHUNK) {
            val box: org.saltyrtc.client.keystore.Box? = crypto!!.encrypt(bufferToBytes(buffer))
            buffer = ByteBuffer.wrap(box!!.toBytes())
        }

        // Write chunks
        // TODO: Add support for reliable/ordered
        val chunker = Chunker(messageId++, buffer, chunkLength)
        while (chunker.hasNext()) {
            // Wait until we can send
            // Note: This will block!
            try {
                fcdc.ready().get()
            } catch (error: InterruptedException) {
                error.printStackTrace()
                return
            } catch (error: ExecutionException) {
                error.printStackTrace()
                return
            }
            buffer = chunker.next()

            // Encrypt chunk (if needed)
            if (cryptoMode === CryptoMode.CHUNK_THEN_ENCRYPT) {
                val box: org.saltyrtc.client.keystore.Box? = crypto!!.encrypt(bufferToBytes(buffer))
                buffer = ByteBuffer.wrap(box!!.toBytes())
            }

            // Write chunk
            val chunk = DataChannel.Buffer(buffer, true)
            Log.d(
                TAG,
                ("Data channel " + dataChannel.label() + " outgoing chunk of length " +
                        chunk.data.remaining())
            )
            fcdc.write(chunk)
        }
    }

    /**
     * Hand in a chunk for reassembly.
     *
     * @param buffer The chunk to be added to the reassembly buffer.
     */
    fun receive(buffer: ByteBuffer) {
        var buffer = buffer
        Log.d(
            TAG,
            "Data channel " + dataChannel.label() + " incoming chunk of length " +
                    buffer.remaining()
        )

        // Decrypt chunk (if needed)
        if (cryptoMode === CryptoMode.CHUNK_THEN_ENCRYPT) {
            val box =
                Box(buffer, DataChannelCryptoContext.NONCE_LENGTH)
            try {
                buffer = ByteBuffer.wrap(crypto!!.decrypt(box))
            } catch (error: ValidationError) {
                Log.e(TAG, "Invalid packet received", error)
                return
            } catch (error: ProtocolException) {
                Log.e(TAG, "Invalid packet received", error)
                return
            } catch (error: CryptoException) {
                Log.e(TAG, "Unable to encrypt", error)
                return
            }
        }

        // Reassemble
        unchunker.add(buffer)
    }

    /**
     * Close the underlying data channel.
     */
    fun close() {
        dataChannel.close()
    }

    companion object {
        const val TAG = "DataChannelContext"

        /**
         * Convert a ByteBuffer to a byte array.
         */
        private fun bufferToBytes(buffer: ByteBuffer): ByteArray {
            // Strip the buffer's array from unnecessary bytes
            // TODO: Fix the crypto API to use ByteBuffer - this is terrible.
            var bytes: ByteArray = buffer.array()
            if (buffer.position() != 0 || buffer.remaining() != bytes.size) {
                bytes = Arrays.copyOf(buffer.array(), buffer.remaining())
            }
            return bytes
        }
    }
}