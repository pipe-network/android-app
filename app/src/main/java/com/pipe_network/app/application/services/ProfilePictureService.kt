package com.pipe_network.app.application.services

import android.content.Context
import android.net.Uri
import de.datlag.mimemagic.MimeData
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

interface ProfilePictureService {
    suspend fun copyExamplePictureAsProfilePicture(context: Context): File
    suspend fun copySetupDataPictureAsProfilePicture(
        context: Context,
        profilePictureUri: Uri,
    ): File
}

class ProfilePictureServiceImpl @Inject constructor() : ProfilePictureService {
    private val exampleProfilePicture = "example_profile_picture.jpg"

    override suspend fun copyExamplePictureAsProfilePicture(context: Context): File {
        val examplePictureInputStream = context.assets.open(exampleProfilePicture)
        val examplePictureByteArray = examplePictureInputStream.readBytes()
        val mimeData = MimeData.fromByteArray(examplePictureByteArray)
        val profilePictureFile = File(
            context.filesDir,
            "$PROFILE_PICTURE_FILENAME.${mimeData.suffix}",
        )
        profilePictureFile.writeBytes(examplePictureByteArray)
        return Compressor.compress(context, profilePictureFile)
    }

    override suspend fun copySetupDataPictureAsProfilePicture(
        context: Context,
        profilePictureUri: Uri,
    ): File {
        val profilePictureInputStream = context.contentResolver.openInputStream(
            profilePictureUri,
        )
        val profilePictureByteArray = profilePictureInputStream!!.readBytes()
        val mimeData = MimeData.fromByteArray(profilePictureByteArray)
        val profilePictureFile = File(
            context.filesDir,
            "$PROFILE_PICTURE_FILENAME.${mimeData.suffix}",
        )
        profilePictureFile.writeBytes(profilePictureByteArray)
        return Compressor.compress(context, profilePictureFile)
    }

    companion object {
        const val PROFILE_PICTURE_FILENAME = "profile_picture"
    }
}