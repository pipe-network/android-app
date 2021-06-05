package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.services.SetupService
import com.pipe_network.app.application.services.SetupServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SetupServiceModule {

    @Binds
    abstract fun bindSetupService(
        setupService: SetupServiceImpl
    ): SetupService

}