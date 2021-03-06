package com.pipe_network.app.application.repositories

import androidx.lifecycle.LiveData
import com.pipe_network.app.infrastructure.models.Feed
import java.util.*

interface FeedRepository {
    fun allLive(): LiveData<List<Feed>>
    suspend fun all(): List<Feed>
    suspend fun get(id: UUID): Feed?
    suspend fun create(feed: Feed): Long
    suspend fun delete(vararg feed: Feed)
}