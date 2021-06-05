package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.repositories.FriendRepository
import com.pipe_network.app.infrastructure.repositories.FriendDatabaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class FriendRepositoryModule {

    @Binds
    abstract fun bindFriendRepository(
        friendDatabaseRepository: FriendDatabaseRepository
    ): FriendRepository

}