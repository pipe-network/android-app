package com.pipe_network.app.domain.entities.pipe_messages

class RequestTypeMessage(type: String) : PipeBaseClass(type) {
    companion object {
        const val REQUEST_FEED_HEADS = "request_feed_heads"
        const val REQUEST_PROFILE = "request_profile"

        fun createAsRequestFeedHeads(): RequestTypeMessage {
            return RequestTypeMessage(REQUEST_FEED_HEADS)
        }

        fun createAsRequestProfile(): RequestTypeMessage {
            return RequestTypeMessage(REQUEST_PROFILE)
        }
    }
}