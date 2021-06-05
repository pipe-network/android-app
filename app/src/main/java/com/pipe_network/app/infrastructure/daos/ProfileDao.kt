package com.pipe_network.app.infrastructure.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import com.pipe_network.app.infrastructure.models.Profile

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile LIMIT 1")
    suspend fun get(): Profile?

    @Query("SELECT * FROM profile LIMIT 1")
    fun getLive(): LiveData<Profile>

    @Insert
    suspend fun create(profile: Profile): Long

    @Update
    suspend fun update(profile: Profile)

    @Delete
    suspend fun delete(profile: Profile)
}