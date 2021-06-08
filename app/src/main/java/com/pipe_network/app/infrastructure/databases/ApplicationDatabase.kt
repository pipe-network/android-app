package com.pipe_network.app.infrastructure.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pipe_network.app.infrastructure.daos.FriendDao
import com.pipe_network.app.infrastructure.daos.ProfileDao
import com.pipe_network.app.infrastructure.daos.FeedDao
import com.pipe_network.app.infrastructure.daos.ForeignFeedDao
import com.pipe_network.app.infrastructure.models.Friend
import com.pipe_network.app.infrastructure.models.Profile
import com.pipe_network.app.infrastructure.models.Feed
import com.pipe_network.app.infrastructure.models.ForeignFeed
import com.pipe_network.app.infrastructure.models.converters.DateConverter
import com.pipe_network.app.infrastructure.models.converters.UUIDConverter

@Database(
    entities = [Profile::class, Friend::class, Feed::class, ForeignFeed::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class, UUIDConverter::class)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun friendDao(): FriendDao
    abstract fun feedDao(): FeedDao
    abstract fun foreignFeedDao(): ForeignFeedDao
}
