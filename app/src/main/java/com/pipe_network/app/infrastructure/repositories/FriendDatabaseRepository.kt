package com.pipe_network.app.infrastructure.repositories

import androidx.lifecycle.LiveData
import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.infrastructure.databases.ApplicationDatabase
import com.pipe_network.app.infrastructure.models.Friend
import javax.inject.Inject

class FriendDatabaseRepository @Inject constructor(
    private val database: ApplicationDatabase,
) : FriendRepository {
    override suspend fun add(friend: Friend): Long {
        return database.friendDao().create(friend)
    }

    override fun allLive(): LiveData<List<Friend>> {
        return database.friendDao().allLive()
    }

    override suspend fun all(): List<Friend> {
        return database.friendDao().all()
    }

    override suspend fun getById(id: Int): Friend? {
        return database.friendDao().getById(id)
    }

    override suspend fun getByPublicKey(publicKey: String): Friend? {
        return database.friendDao().getByPublicKey(publicKey)
    }

    override suspend fun update(friend: Friend) {
        return database.friendDao().update(friend)
    }

    override suspend fun delete(vararg friend: Friend) {
        return database.friendDao().delete(*friend)
    }
}