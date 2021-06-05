package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.services.ProfilePictureService
import com.pipe_network.app.application.services.ProfilePictureServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfilePictureServiceModule {

    @Binds
    abstract fun bindProfilePictureService(
        profilePictureService: ProfilePictureServiceImpl
    ): ProfilePictureService

}