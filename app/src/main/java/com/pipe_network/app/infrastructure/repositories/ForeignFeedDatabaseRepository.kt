package com.pipe_network.app.infrastructure.repositories

import androidx.lifecycle.LiveData
import com.pipe_network.app.application.repositories.ForeignFeedRepository
import com.pipe_network.app.infrastructure.databases.ApplicationDatabase
import com.pipe_network.app.infrastructure.models.ForeignFeed
import com.pipe_network.app.infrastructure.models.ForeignFeedWithUser
import java.util.*
import javax.inject.Inject

class ForeignFeedDatabaseRepository @Inject constructor(
    private val database: ApplicationDatabase,
) : ForeignFeedRepository {
    override suspend fun all(): List<ForeignFeed> {
        return database.foreignFeedDao().all()
    }

    override fun allLive(): LiveData<List<ForeignFeed>> {
        return database.foreignFeedDao().allLive()
    }

    override fun allLiveWithUsers(): LiveData<List<ForeignFeedWithUser>> {
        return database.foreignFeedDao().allLiveWithUsers()
    }

    override suspend fun get(id: UUID): ForeignFeed? {
        return database.foreignFeedDao().get(id)
    }

    override suspend fun create(foreignFeed: ForeignFeed) {
        return database.foreignFeedDao().create(foreignFeed)
    }

    override suspend fun delete(vararg foreignFeed: ForeignFeed) {
        return database.foreignFeedDao().delete(*foreignFeed)
    }
}