package com.pipe_network.app.infrastructure.models

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import de.datlag.mimemagic.MimeData
import com.pipe_network.app.domain.entities.Profile as ProfileEntity
import java.io.File


@Entity
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val uid: Int,

    @ColumnInfo(name = "first_name")
    var firstName: String,

    @ColumnInfo(name = "last_name")
    var lastName: String,

    @ColumnInfo(name = "description")
    var description: String,

    @ColumnInfo(name = "profile_picture_path")
    var profilePicturePath: String,

    @ColumnInfo(name = "private_key")
    val privateKey: String,

    @ColumnInfo(name = "public_key")
    val publicKey: String
) {
    fun getProfilePictureFile(): File {
        return File(profilePicturePath)
    }

    fun getProfilePictureUri(): Uri {
        return getProfilePictureFile().toUri()
    }

    fun getProfilePictureMimeType(): String? {
        val mimeData = MimeData.fromFile(getProfilePictureFile())
        return mimeData.suffix
    }

    fun getProfileEntity(): ProfileEntity {
        return ProfileEntity(
            firstName = firstName,
            lastName = lastName,
            description = description,
            profilePicture = getProfilePictureFile().readBytes(),
            profilePictureMimeType = getProfilePictureMimeType() ?: "",
            publicKey = publicKey,
        )
    }
}