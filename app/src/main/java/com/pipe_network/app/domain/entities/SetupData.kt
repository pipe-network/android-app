package com.pipe_network.app.domain.entities

import android.net.Uri
import com.pipe_network.app.infrastructure.models.Profile

data class SetupData(
    var firstName: String,
    var lastName: String,
    var description: String,
    var profilePictureFileUri: Uri?,
) {
    fun toProfile(
        profilePicturePath: String,
        publicKey: String,
        privateKey: String,
    ): Profile {
        return Profile(
            uid = 0,
            firstName = firstName,
            lastName = lastName,
            description = description,
            profilePicturePath = profilePicturePath,
            privateKey = privateKey,
            publicKey = publicKey,
        )
    }
}