package com.pipe_network.app.infrastructure.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pipe_network.app.infrastructure.models.Friend

@Dao
interface FriendDao {
    @Query("SELECT * FROM friend WHERE id = :id")
    suspend fun getById(id: Int): Friend?

    @Query("SELECT * FROM friend WHERE public_key= :publicKey")
    suspend fun getByPublicKey(publicKey: String): Friend?

    @Query("SELECT * FROM friend")
    fun allLive(): LiveData<List<Friend>>

    @Query("SELECT * FROM friend")
    suspend fun all(): List<Friend>

    @Insert
    suspend fun create(friend: Friend): Long

    @Update
    suspend fun update(friend: Friend)

    @Delete
    suspend fun delete(vararg friend: Friend)
}