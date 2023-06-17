package com.pandacorp.noteui.domain.repository

import com.pandacorp.noteui.domain.model.NoteItem
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAll(): Flow<List<NoteItem>>
    fun update(item: NoteItem)
    fun insert(item: NoteItem): Long
    fun insert(list: List<NoteItem>)
    fun remove(item: NoteItem)
    fun remove(list: List<NoteItem>)
}