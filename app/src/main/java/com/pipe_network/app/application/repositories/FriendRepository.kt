package com.pipe_network.app.application.repositories

import androidx.lifecycle.LiveData
import com.pipe_network.app.infrastructure.models.Friend

interface FriendRepository {
    suspend fun add(friend: Friend): Long
    fun allLive(): LiveData<List<Friend>>
    suspend fun all(): List<Friend>
    suspend fun getById(id: Int): Friend?
    suspend fun getByPublicKey(publicKey: String): Friend?
    suspend fun update(friend: Friend)
    suspend fun delete(vararg friend: Friend)
}