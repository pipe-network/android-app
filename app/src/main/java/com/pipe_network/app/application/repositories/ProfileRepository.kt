package com.pipe_network.app.application.repositories

import androidx.lifecycle.LiveData
import com.pipe_network.app.infrastructure.models.Profile

interface ProfileRepository {
    suspend fun create(profile: Profile): Long
    suspend fun get(): Profile?
    suspend fun update(profile: Profile)
    suspend fun delete()
    fun getLive(): LiveData<Profile>
}