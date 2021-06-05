package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.services.PurgeService
import com.pipe_network.app.application.services.PurgeServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PurgeServiceModule {

    @Binds
    abstract fun bindPurgeService(
        purgeService: PurgeServiceImpl
    ): PurgeService

}