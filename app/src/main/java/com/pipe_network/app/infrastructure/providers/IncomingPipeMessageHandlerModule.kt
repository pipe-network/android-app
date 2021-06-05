package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.handlers.IncomingPipeMessageHandler
import com.pipe_network.app.application.handlers.IncomingPipeMessageHandlerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class IncomingPipeMessageHandlerModule {
    @Binds
    abstract fun bindIncomingPipeMessageHandler(
        incomingPipeMessageHandler: IncomingPipeMessageHandlerImpl
    ): IncomingPipeMessageHandler
}