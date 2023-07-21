package com.pandacorp.noteui.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.pandacorp.noteui.data.database.dao.NoteDao
import com.pandacorp.noteui.data.mapper.NoteMapper
import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.domain.repository.NoteRepository

class NoteRepositoryImpl(private val dao: NoteDao, private val mapper: NoteMapper) : NoteRepository {
    override fun getAll(): LiveData<List<NoteItem>> = dao.getAll().map { list ->
        list.map {
            mapper.toNoteItem(it)
        }
    }

    override fun update(item: NoteItem) = dao.update(mapper.toNoteDataItem(item))

    override fun insert(item: NoteItem): Long = dao.insert(mapper.toNoteDataItem(item))

    override fun insert(list: List<NoteItem>) {
        val newList = list.map { mapper.toNoteDataItem(it) }
        dao.insert(newList)
    }

    override fun remove(item: NoteItem) = dao.remove(mapper.toNoteDataItem(item))

    override fun remove(list: List<NoteItem>) {
        val newList = list.map { mapper.toNoteDataItem(it) }
        dao.remove(newList)
    }
}
