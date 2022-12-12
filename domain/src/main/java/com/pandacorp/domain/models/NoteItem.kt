package com.pandacorp.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class NoteItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "background") var background: String,
    @ColumnInfo(name = "header") var header: String,
    @ColumnInfo(name = "content") var content: String,
    @ColumnInfo(name = "isShowTransparentActionBar") var isShowTransparentActionBar: Boolean = false)