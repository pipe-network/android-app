package com.pipe_network.app.domain.interfaces

interface MsgPacker {
    fun msgpack(): ByteArray
}