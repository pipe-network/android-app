package com.pipe_network.app.infrastructure.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pipe_network.app.infrastructure.models.Feed
import java.util.*

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed")
    fun allLive(): LiveData<List<Feed>>

    @Query("SELECT * FROM feed")
    suspend fun all(): List<Feed>

    @Query("SELECT * FROM feed WHERE id = :id ")
    suspend fun get(id: UUID): Feed?

    @Insert
    suspend fun create(feed: Feed): Long

    @Delete
    suspend fun delete(vararg feed: Feed)
}