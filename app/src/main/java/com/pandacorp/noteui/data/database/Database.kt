package com.pandacorp.noteui.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pandacorp.noteui.data.database.dao.ColorDao
import com.pandacorp.noteui.data.database.dao.NoteDao
import com.pandacorp.noteui.data.model.ColorDataItem
import com.pandacorp.noteui.data.model.NoteDataItem

@Database(entities = [NoteDataItem::class, ColorDataItem::class], version = 1, exportSchema = false)
abstract class Database : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun colorDao(): ColorDao
}