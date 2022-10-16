package com.pandacorp.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ListItem(
    @PrimaryKey(autoGenerate = true) val id: Int=0,
    @ColumnInfo(name = "header") val header: String,
    @ColumnInfo(name = "content") val content: String)