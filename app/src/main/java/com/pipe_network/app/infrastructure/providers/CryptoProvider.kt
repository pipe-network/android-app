package com.pipe_network.app.infrastructure.providers

import com.pipe_network.app.application.webrtc.LazySodiumCryptoProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.saltyrtc.client.crypto.CryptoProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class CryptoProviderModule {

    @Binds
    abstract fun bindCryptoProvider(
        lazySodiumCryptoProvider: LazySodiumCryptoProvider
    ): CryptoProvider
}