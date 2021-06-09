package com.pipe_network.app.application.services

import com.pipe_network.app.application.repositories.ForeignFeedRepository
import com.pipe_network.app.domain.entities.Feed
import com.pipe_network.app.domain.entities.FeedHead
import com.pipe_network.app.infrastructure.models.ForeignFeed
import java.util.*
import javax.inject.Inject

interface SyncForeignFeedsService {
    suspend fun getToRetrieveFeedHeads(feedHeads: List<FeedHead>): List<FeedHead>
    suspend fun storeForeignFeed(feed: Feed)
}

class SyncForeignFeedsServiceImpl @Inject constructor(
    private val foreignFeedRepository: ForeignFeedRepository,
    private val pictureStoringService: PictureStoringService,
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

    override suspend fun storeForeignFeed(feed: Feed) {
        var picturePath: String? = null
        feed.picture?.let {
            val picture = pictureStoringService.save(feed.uuid, feed.picture)
            picturePath = picture.absolutePath
        }
        foreignFeedRepository.create(
            ForeignFeed(
                id = UUID.fromString(feed.uuid),
                created = Date(feed.timestamp.toLong()),
                text = feed.text,
                picturePath = picturePath,
                friendId = feed.friend.id,
            )
        )
    }
}