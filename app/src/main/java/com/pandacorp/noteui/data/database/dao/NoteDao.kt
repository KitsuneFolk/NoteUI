package com.pandacorp.noteui.data.database.dao

import androidx.room.*
import com.pandacorp.noteui.data.model.NoteDataItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table ORDER BY id DESC")
    fun getAll(): Flow<MutableList<NoteDataItem>>

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(noteItem: NoteDataItem)
    
    @Insert
    fun insert(noteItem: NoteDataItem): Long

    @Insert
    fun insert(list: List<NoteDataItem>)
    
    @Delete
    fun remove(noteItem: NoteDataItem)

    @Delete
    fun remove(list: List<NoteDataItem>)
}