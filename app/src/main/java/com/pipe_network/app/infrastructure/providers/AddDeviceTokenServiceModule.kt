package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.services.AddDeviceTokenService
import com.pipe_network.app.application.services.AddDeviceTokenServiceImpl
import com.pipe_network.app.application.services.PurgeService
import com.pipe_network.app.application.services.PurgeServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AddDeviceTokenServiceModule {

    @Binds
    abstract fun bindAddDeviceTokenService(
        addDeviceTokenService: AddDeviceTokenServiceImpl
    ): AddDeviceTokenService

}