package com.pandacorp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.models.NoteItem

@Database(entities = [NoteItem::class, ColorItem::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun colorsCardDao(): ColorDao
    
}