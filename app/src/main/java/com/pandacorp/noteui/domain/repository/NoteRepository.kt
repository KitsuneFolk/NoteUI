package com.pandacorp.noteui.domain.repository

import androidx.lifecycle.LiveData
import com.pandacorp.noteui.domain.model.NoteItem

interface NoteRepository {
    fun getAll(): LiveData<List<NoteItem>>

    fun update(item: NoteItem)

    fun insert(item: NoteItem): Long

    fun insert(list: List<NoteItem>)

    fun remove(item: NoteItem)

    fun remove(list: List<NoteItem>)
}