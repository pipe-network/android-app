package com.pipe_network.app.infrastructure.repositories

import androidx.lifecycle.LiveData
import com.pipe_network.app.application.repositories.ProfileRepository
import com.pipe_network.app.infrastructure.databases.ApplicationDatabase
import com.pipe_network.app.infrastructure.models.Profile
import javax.inject.Inject

class ProfileDatabaseRepository @Inject constructor(
    private val database: ApplicationDatabase
) : ProfileRepository {
    override suspend fun get(): Profile? {
        return database.profileDao().get()
    }

    override suspend fun create(profile: Profile): Long {
        return database.profileDao().create(profile)
    }

    override suspend fun update(profile: Profile) {
        return database.profileDao().update(profile)
    }

    override fun getLive(): LiveData<Profile> {
        return database.profileDao().getLive()
    }

    override suspend fun delete() {
        val profile = get()
        if (profile != null) {
            profile.getProfilePictureFile().delete()
            database.profileDao().delete(profile)
        }
    }
}