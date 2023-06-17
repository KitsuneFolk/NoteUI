package com.pandacorp.noteui.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "note_table")
data class NoteDataItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "title") val title: String="",
    @ColumnInfo(name = "content") val content: String="",
    @ColumnInfo(name = "background") val background: String="",
    @ColumnInfo(name = "isShowTransparentActionBar") val isShowTransparentActionBar: Boolean = false)
