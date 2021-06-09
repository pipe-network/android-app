package com.pipe_network.app.infrastructure.models

import androidx.room.*
import java.io.File
import java.util.*

@Entity
data class ForeignFeed(
    @PrimaryKey
    val id: UUID,

    @ColumnInfo(name = "created")
    val created: Date,

    @ColumnInfo(name = "text")
    var text: String,

    @ColumnInfo(name = "picture_path")
    var picturePath: String? = null,

    @ColumnInfo(name = "friendId")
    var friendId: Int,
) {
    fun hasPicture(): Boolean {
        return picturePath != null
    }

    fun getPictureFile(): File? {
        picturePath?.let {
            return File(it)
        }
        return null
    }
}

data class ForeignFeedWithUser(
    @Embedded val foreignFeed: ForeignFeed,
    @Relation(
        parentColumn = "friendId",
        entityColumn = "id"
    )
    val friend: Friend
)


