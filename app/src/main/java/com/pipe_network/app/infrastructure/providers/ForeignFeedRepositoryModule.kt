package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.repositories.ForeignFeedRepository
import com.pipe_network.app.infrastructure.repositories.ForeignFeedDatabaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ForeignFeedRepositoryModule {

    @Binds
    abstract fun bindForeignFeedRepository(
        foreignFeedRepository: ForeignFeedDatabaseRepository
    ): ForeignFeedRepository

}