package com.pipe_network.app.infrastructure.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pipe_network.app.infrastructure.models.Friend
import com.pipe_network.app.infrastructure.models.FriendWithForeignFeeds

@Dao
interface FriendDao {
    @Query("SELECT * FROM Friend WHERE id = :id")
    suspend fun getById(id: Int): Friend?

    @Query("SELECT * FROM Friend WHERE public_key= :publicKey")
    suspend fun getByPublicKey(publicKey: String): Friend?

    @Query("SELECT * FROM Friend")
    fun allLive(): LiveData<List<Friend>>

    @Transaction
    @Query("SELECT * FROM Friend where id = :id")
    fun getFriendWithForeignFeeds(id: Int): List<FriendWithForeignFeeds>

    @Query("SELECT * FROM Friend")
    suspend fun all(): List<Friend>

    @Insert
    suspend fun create(friend: Friend): Long

    @Update
    suspend fun update(friend: Friend)

    @Delete
    suspend fun delete(vararg friend: Friend)
}