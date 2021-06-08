package com.pipe_network.app.infrastructure.repositories

import androidx.lifecycle.LiveData
import com.pipe_network.app.application.repositories.FeedRepository
import com.pipe_network.app.infrastructure.databases.ApplicationDatabase
import com.pipe_network.app.infrastructure.models.Feed
import java.util.*
import javax.inject.Inject

class FeedDatabaseRepository @Inject constructor(
    private val database: ApplicationDatabase,
) : FeedRepository {
    override fun allLive(): LiveData<List<Feed>> {
        return database.feedDao().allLive()
    }

    override suspend fun all(): List<Feed> {
        return database.feedDao().all()
    }

    override suspend fun get(id: UUID): Feed? {
        return database.feedDao().get(id)
    }

    override suspend fun create(feed: Feed): Long {
        return database.feedDao().create(feed)
    }

    override suspend fun delete(vararg feed: Feed) {
        return database.feedDao().delete(*feed)
    }
}