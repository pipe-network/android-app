package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.services.PictureStoringService
import com.pipe_network.app.application.services.PictureStoringServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PictureStoringServiceModule {

    @Binds
    abstract fun bindPictureStoringService(
        pictureStoringService: PictureStoringServiceImpl
    ): PictureStoringService

}