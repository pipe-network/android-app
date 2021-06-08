package com.pipe_network.app.domain.entities.pipe_messages

import com.pipe_network.app.domain.entities.FeedHead

class RespondFeedHeads(val feedHeads: List<FeedHead>) : PipeBaseClass(TYPE) {
    companion object {
        const val TYPE = "respond_feed_heads"
    }
}