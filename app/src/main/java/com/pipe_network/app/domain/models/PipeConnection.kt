package com.pipe_network.app.domain.models

import java.util.concurrent.CompletableFuture

interface PipeConnection {
    fun connect()
    fun disconnect()
    fun sendMessage(byteArray: ByteArray)
    fun sendMessageAsync(byteArray: ByteArray): CompletableFuture<*>
    fun isConnected(): Boolean
}