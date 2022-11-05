package com.pandacorp.domain.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ColorItem(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    @ColumnInfo(name = "type") val type: Int = COLOR,
    @ColumnInfo(name = "color") var color: Int
){
    companion object Type{
        const val ADD = 0
        const val COLOR = 1
    }
}