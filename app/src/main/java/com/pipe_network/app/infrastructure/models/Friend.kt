package com.pipe_network.app.infrastructure.models

import androidx.room.*
import java.io.File

@Entity
data class Friend(
    @PrimaryKey(autoGenerate = true)
    val id: Int,

    @ColumnInfo(name = "first_name")
    var firstName: String,

    @ColumnInfo(name = "last_name")
    var lastName: String,

    @ColumnInfo(name = "description")
    var description: String,

    @ColumnInfo(name = "profile_picture_path")
    var profilePicturePath: String,

    @ColumnInfo(name = "public_key")
    val publicKey: String,
) {
    fun getProfilePictureFile(): File {
        return File(profilePicturePath)
    }

    fun isInitialized(): Boolean {
        return firstName != "" || lastName != ""
    }

    fun fullName(): String {
        return "$firstName $lastName"
    }
}

data class FriendWithForeignFeeds(
    @Embedded val friend: Friend,
    @Relation(
        parentColumn = "id",
        entityColumn = "friendId"
    )
    val foreignFeeds: List<ForeignFeed>
)
