package com.pipe_network.app.application.services

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import com.pipe_network.app.application.repositories.ProfileRepository
import com.pipe_network.app.domain.entities.SetupData
import org.saltyrtc.client.crypto.CryptoProvider
import org.saltyrtc.client.keystore.KeyStore
import java.io.File
import javax.inject.Inject

interface SetupService {
    suspend fun isSetupNeeded(): Boolean
    suspend fun setup(context: Context, setupData: SetupData)
}

class SetupServiceImpl @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val cryptoProvider: CryptoProvider,
    private val profilePictureService: ProfilePictureService,
) : SetupService {

    @WorkerThread
    override suspend fun isSetupNeeded(): Boolean {
        val profile = profileRepository.get()
        Log.d(TAG, "Profile: $profile")
        return profile == null
    }

    @WorkerThread
    override suspend fun setup(context: Context, setupData: SetupData) {
        val profilePictureFile: File = if (setupData.profilePictureFileUri == null) {
            profilePictureService.copyExamplePictureAsProfilePicture(context)
        } else {
            profilePictureService.copySetupDataPictureAsProfilePicture(
                context,
                setupData.profilePictureFileUri!!,
            )
        }

        val publicKey = ByteArray(CryptoProvider.PUBLICKEYBYTES)
        val privateKey = ByteArray(CryptoProvider.PRIVATEKEYBYTES)
        cryptoProvider.generateKeypair(publicKey, privateKey)
        val keystore = KeyStore(cryptoProvider, privateKey)
        profileRepository.create(
            setupData.toProfile(
                profilePicturePath = profilePictureFile.absolutePath,
                publicKey = keystore.publicKeyHex,
                privateKey = keystore.privateKeyHex,
            )
        )
    }

    companion object {
        const val TAG = "SetupService"
    }
}