package com.pipe_network.app.domain.entities.pipe_messages

import com.pipe_network.app.domain.entities.OutgoingFeed

data class RespondFeed(val feed: OutgoingFeed): PipeBaseClass(TYPE) {
    companion object {
        const val TYPE = "respond_feed"
    }
}
