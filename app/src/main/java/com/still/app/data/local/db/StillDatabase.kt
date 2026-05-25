package com.still.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.still.app.data.local.dao.NoteDao
import com.still.app.data.local.entity.NoteEntity

@Database(
    entities = [NoteEntity::class],
    version = 1,
    exportSchema = true,   // schema exported to /schemas — version history tracked
)
abstract class StillDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
}