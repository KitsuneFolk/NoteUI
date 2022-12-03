package com.pandacorp.data.database

import androidx.room.*
import com.pandacorp.domain.models.ColorItem

@Dao
interface ColorDao {
    @Query("SELECT * FROM colorItem")
    fun getAll(): MutableList<ColorItem>
    
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(colorItem: ColorItem)
    
    @Insert
    fun insert(colorItem: ColorItem)
    
    @Delete
    fun remove(colorItem: ColorItem)
    
    @Query("DELETE FROM colorItem")
    fun removeAll()
}