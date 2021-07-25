package com.pipe_network.app.application.repositories

import androidx.lifecycle.LiveData
import com.pipe_network.app.infrastructure.models.ForeignFeed
import com.pipe_network.app.infrastructure.models.ForeignFeedWithUser
import com.pipe_network.app.infrastructure.models.Friend
import java.util.*

interface ForeignFeedRepository {
    suspend fun all(): List<ForeignFeed>
    fun allLive(): LiveData<List<ForeignFeed>>
    fun allLiveWithUsers(): LiveData<List<ForeignFeedWithUser>>
    suspend fun allByFriend(friend: Friend): List<ForeignFeed>
    suspend fun get(id: UUID): ForeignFeed?
    suspend fun create(foreignFeed: ForeignFeed)
    suspend fun delete(vararg foreignFeed: ForeignFeed)
}