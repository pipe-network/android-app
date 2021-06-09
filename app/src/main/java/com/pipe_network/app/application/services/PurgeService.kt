package com.pipe_network.app.application.services

import androidx.annotation.WorkerThread
import com.pipe_network.app.application.repositories.FeedRepository
import com.pipe_network.app.application.repositories.ForeignFeedRepository
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.application.repositories.ProfileRepository
import javax.inject.Inject

interface PurgeService {
    suspend fun purge()
}

class PurgeServiceImpl @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val friendRepository: FriendRepository,
    private val feedRepository: FeedRepository,
    private val foreignFeedRepository: ForeignFeedRepository,
) : PurgeService {

    @WorkerThread
    override suspend fun purge() {
        profileRepository.delete()
        val friends = friendRepository.all()
        friendRepository.delete(*friends.toTypedArray())

        val feeds = feedRepository.all()
        feedRepository.delete(*feeds.toTypedArray())

        val foreignFeeds = foreignFeedRepository.all()
        foreignFeedRepository.delete(*foreignFeeds.toTypedArray())
    }
}