package com.pipe_network.app.infrastructure.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Feed(
    @ColumnInfo(name = "created")
    val created: Date,

    @ColumnInfo(name = "text")
    var text: String,

    @PrimaryKey
    val id: UUID = UUID.randomUUID(),

    @ColumnInfo(name = "picture_path")
    var picturePath: String? = null,
) {
    fun hasPicture(): Boolean {
        return this.picturePath != null
    }
}