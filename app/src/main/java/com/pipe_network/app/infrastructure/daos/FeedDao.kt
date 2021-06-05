package com.pipe_network.app.infrastructure.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pipe_network.app.infrastructure.models.Feed

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed")
    fun allLive(): LiveData<List<Feed>>

    @Query("SELECT * FROM feed")
    suspend fun all(): List<Feed>

    @Insert
    suspend fun create(feed: Feed): Long

    @Delete
    suspend fun delete(vararg feed: Feed)
}