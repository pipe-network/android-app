package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.repositories.PipeConnectionFriendRepository
import com.pipe_network.app.application.repositories.PipeConnectionFriendRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class PipeConnectionFriendRepositoryModule {

    @Binds
    abstract fun bindPipeConnectionFriendRepository(
        pipeConnectionFriendRepository: PipeConnectionFriendRepositoryImpl
    ): PipeConnectionFriendRepository

}