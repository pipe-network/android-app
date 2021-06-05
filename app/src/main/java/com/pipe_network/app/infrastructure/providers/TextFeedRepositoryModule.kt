package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.repositories.FeedRepository
import com.pipe_network.app.infrastructure.repositories.FeedDatabaseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TextFeedRepositoryModule {

    @Binds
    abstract fun bindTextFeedRepository(
        feedDatabaseRepository: FeedDatabaseRepository
    ): FeedRepository

}