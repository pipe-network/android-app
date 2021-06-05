package com.pipe_network.app.infrastructure.providers

import android.content.Context
import androidx.room.Room
import com.pipe_network.app.infrastructure.databases.ApplicationDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseProvider {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext applicationContext: Context): ApplicationDatabase {
        return Room.databaseBuilder(
            applicationContext,
            ApplicationDatabase::class.java, "pipe-database",
        ).addMigrations(
        ).build()
    }
}