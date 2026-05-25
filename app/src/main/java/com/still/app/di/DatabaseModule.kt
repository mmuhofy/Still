package com.still.app.di

import android.content.Context
import androidx.room.Room
import com.still.app.data.local.dao.NoteDao
import com.still.app.data.local.db.StillDatabase
import com.still.app.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): StillDatabase =
        Room.databaseBuilder(
            context,
            StillDatabase::class.java,
            Constants.DATABASE_NAME,
        ).build()

    @Provides
    @Singleton
    fun provideNoteDao(db: StillDatabase): NoteDao = db.noteDao()
}