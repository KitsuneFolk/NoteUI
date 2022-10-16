package com.pandacorp.data.repositories

import com.pandacorp.data.database.NoteDao
import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.repositories.DataRepositoryInterface

class DataRepository(private val noteDao: NoteDao) : DataRepositoryInterface {
    override fun getDatabaseItems(): MutableList<ListItem> {
        return noteDao.getAll()
    }
    
    override fun add(listItem: ListItem) {
        noteDao.insert(listItem)
    }
    
    override fun remove(listItem: ListItem) {
        noteDao.remove(listItem)
    }
    
}
