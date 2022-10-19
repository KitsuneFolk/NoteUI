package com.pandacorp.data.database

import androidx.room.*
import com.pandacorp.domain.models.ListItem

@Dao
interface NoteDao {
    @Query("SELECT * FROM listItem")
    fun getAll(): MutableList<ListItem>
    
    @Query("SELECT * FROM listItem WHERE id IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<ListItem>
    
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(listItem: ListItem)
    
    @Insert
    fun insert(listItem: ListItem)
    
    @Delete
    fun remove(listItem: ListItem)
}