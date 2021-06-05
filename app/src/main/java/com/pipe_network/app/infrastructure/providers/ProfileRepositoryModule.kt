package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.repositories.ProfileRepository
import com.pipe_network.app.infrastructure.repositories.ProfileDatabaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileRepositoryModule {

    @Binds
    abstract fun bindProfileRepository(
        profileDatabaseRepository: ProfileDatabaseRepository
    ): ProfileRepository

}