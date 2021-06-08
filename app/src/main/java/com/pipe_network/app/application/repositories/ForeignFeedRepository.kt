package com.pipe_network.app.application.repositories

import com.pipe_network.app.infrastructure.models.ForeignFeed
import java.util.*

interface ForeignFeedRepository {
    suspend fun all(): List<ForeignFeed>
    suspend fun get(id: UUID): ForeignFeed?
    suspend fun create(foreignFeed: ForeignFeed)
    suspend fun delete(vararg foreignFeed: ForeignFeed)
}