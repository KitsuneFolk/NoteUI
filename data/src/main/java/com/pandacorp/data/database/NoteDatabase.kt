package com.pandacorp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pandacorp.domain.models.ListItem

@Database(entities = [ListItem::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    
    
}