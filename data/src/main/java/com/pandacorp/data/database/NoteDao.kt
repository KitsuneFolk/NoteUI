package com.pandacorp.data.database

import androidx.room.*
import com.pandacorp.domain.models.NoteItem

@Dao
interface NoteDao {
    @Query("SELECT * FROM noteItem")
    fun getAll(): MutableList<NoteItem>
    
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(noteItem: NoteItem)
    
    @Insert
    fun insert(noteItem: NoteItem)
    
    @Delete
    fun remove(noteItem: NoteItem)
}