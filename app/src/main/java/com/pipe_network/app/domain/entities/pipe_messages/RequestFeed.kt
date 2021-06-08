package com.pipe_network.app.domain.entities.pipe_messages

data class RequestFeed(val uuid: String): PipeBaseClass(TYPE) {
    companion object {
        const val TYPE = "request_feed"
    }
}