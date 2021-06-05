package com.pipe_network.app.domain.interfaces

interface MsgUnpacker<C> {
    fun msgunpack(byteArray: ByteArray): C
}