package com.pipe_network.app.application.factories

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.pipe_network.app.application.observers.PipeConnectionObserver
import com.pipe_network.app.infrastructure.models.InitiatorPipeConnection
import com.pipe_network.app.domain.models.PipeConnection
import com.pipe_network.app.infrastructure.models.ResponderPipeConnection
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.saltyrtc.client.crypto.CryptoProvider
import org.saltyrtc.client.keystore.KeyStore
import javax.inject.Inject
import javax.net.ssl.SSLContext

@Module
@InstallIn(SingletonComponent::class)
class PipeConnectionFactory @Inject constructor() {
    @com.pipe_network.app.infrastructure.providers.TrustAllCertsSSLContext
    @Inject
    lateinit var sslContext: SSLContext

    @Inject
    lateinit var cryptoProvider: CryptoProvider

    @RequiresApi(Build.VERSION_CODES.N)
    fun createInitiatorPipeConnection(
        context: Context,
        observer: PipeConnectionObserver,
        privateKey: String,
        responderPublicKey: String,
    ): PipeConnection {
        val keyStore = KeyStore(cryptoProvider, privateKey)
        Log.d(TAG, "Derived public key: ${keyStore.publicKeyHex}")
        return InitiatorPipeConnection(
            context,
            observer,
            sslContext,
            cryptoProvider,
            keyStore,
            responderPublicKey,
        )
    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun createResponderPipeConnection(
        context: Context,
        observer: PipeConnectionObserver,
        privateKey: String,
        initiatorPublicKey: String,
    ): PipeConnection {
        val keyStore = KeyStore(cryptoProvider, privateKey)
        Log.d(TAG, "Derived public key: ${keyStore.publicKeyHex}")
        return ResponderPipeConnection(
            context,
            observer,
            sslContext,
            cryptoProvider,
            keyStore,
            initiatorPublicKey
        )
    }

    companion object {
        const val TAG = "PipeConnectionFcty"
    }
}