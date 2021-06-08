package com.pipe_network.app.application.services

import com.pipe_network.app.application.repositories.ForeignFeedRepository
import com.pipe_network.app.domain.entities.FeedHead
import java.util.*
import javax.inject.Inject

interface SyncForeignFeedsService {
    suspend fun getToRetrieveFeedHeads(feedHead: List<FeedHead>): List<FeedHead>
}

class SyncForeignFeedsServiceImpl @Inject constructor(
    private val foreignFeedRepository: ForeignFeedRepository,
) : SyncForeignFeedsService {
    override suspend fun getToRetrieveFeedHeads(feedHeads: List<FeedHead>): List<FeedHead> {
        val retrieveFeedHeads = arrayListOf<FeedHead>()
        for (feedHead in feedHeads) {
            val uuid = UUID.fromString(feedHead.uuid)
            val foreignFeed = foreignFeedRepository.get(uuid)

            if (foreignFeed == null) {
                retrieveFeedHeads.add(feedHead)
            }
        }
        return retrieveFeedHeads
    }
}