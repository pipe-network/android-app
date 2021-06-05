package com.pipe_network.app.infrastructure.providers

import android.annotation.SuppressLint
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.inject.Qualifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TrustAllCertsSSLContext

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultSSLContext


@Module
@InstallIn(SingletonComponent::class)
class SSLContextProvider {
    @TrustAllCertsSSLContext
    @Provides
    fun provideTrustAllCertsSSLContext(): SSLContext {
        val trustAllCerts: Array<TrustManager> = arrayOf(
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<out X509Certificate>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate> {
                    return arrayOf()
                }
            }
        )
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext
    }

    @DefaultSSLContext
    @Provides
    fun provideDefaultSSLContext(): SSLContext {
        return SSLContext.getDefault()
    }
}