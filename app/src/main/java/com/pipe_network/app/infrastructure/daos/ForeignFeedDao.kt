package com.pipe_network.app.infrastructure.daos

import androidx.room.*
import com.pipe_network.app.infrastructure.models.ForeignFeed
import java.util.*

@Dao
interface ForeignFeedDao {
    @Query("SELECT * FROM ForeignFeed")
    suspend fun all(): List<ForeignFeed>

    @Query("SELECT * FROM ForeignFeed WHERE id = :id ")
    suspend fun get(id: UUID): ForeignFeed?

    @Insert
    suspend fun create(foreignFeed: ForeignFeed)

    @Delete
    suspend fun delete(vararg foreignFeed: ForeignFeed)
}