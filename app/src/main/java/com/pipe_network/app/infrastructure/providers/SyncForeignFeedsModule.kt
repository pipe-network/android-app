package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.services.SyncForeignFeedsService
import com.pipe_network.app.application.services.SyncForeignFeedsServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncForeignFeedsModule {

    @Binds
    abstract fun bindSyncForeignFeedsService(
        syncForeignFeedsService: SyncForeignFeedsServiceImpl
    ): SyncForeignFeedsService

}