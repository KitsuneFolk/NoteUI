package com.pandacorp.data.repositories

import com.pandacorp.data.database.ColorDao
import com.pandacorp.data.database.NoteDao
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.repositories.DataRepositoryInterface

class DataRepository(
    private val noteDao: NoteDao,
    private val colorDao: ColorDao) :
    DataRepositoryInterface {
    
    override fun getNotes(): MutableList<NoteItem> {
        return noteDao.getAll()
    }
    
    override fun updateNote(noteItem: NoteItem) {
        noteDao.update(noteItem)
    }
    
    override fun addNote(noteItem: NoteItem) {
        noteDao.insert(noteItem)
    }
    
    override fun removeNote(noteItem: NoteItem) {
        noteDao.remove(noteItem)
    }
    
    override fun getColors(): MutableList<ColorItem> {
        return colorDao.getAll()
    }
    
    override fun updateColor(colorItem: ColorItem) {
        colorDao.update(colorItem)
    }
    
    override fun addColor(colorItem: ColorItem) {
        colorDao.insert(colorItem)
    }
    
    override fun removeColor(colorItem: ColorItem) {
        colorDao.remove(colorItem)
    }
    
    override fun removeAllColors() {
        colorDao.removeAll()
    }
    
    
}
