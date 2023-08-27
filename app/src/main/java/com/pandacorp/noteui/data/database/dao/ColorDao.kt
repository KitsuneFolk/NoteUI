package com.pandacorp.noteui.data.database.dao

import androidx.room.*
import com.pandacorp.noteui.data.model.ColorDataItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ColorDao {
    @Query("SELECT * FROM color_table ORDER BY id DESC")
    fun getAll(): Flow<MutableList<ColorDataItem>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(colorItem: ColorDataItem)

    @Insert
    fun insert(colorItem: ColorDataItem)

    @Insert
    fun insert(list: List<ColorDataItem>)

    @Delete
    fun remove(colorItem: ColorDataItem)

    @Query("DELETE FROM color_table")
    fun removeAll()
}