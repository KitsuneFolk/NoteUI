package com.pandacorp.noteui.data.repository

import com.pandacorp.noteui.data.database.dao.ColorDao
import com.pandacorp.noteui.data.mapper.ColorMapper
import com.pandacorp.noteui.domain.model.ColorItem
import com.pandacorp.noteui.domain.repository.ColorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ColorRepositoryImpl(private val dao: ColorDao, private val mapper: ColorMapper) : ColorRepository {
    override fun getAll(): Flow<List<ColorItem>> = dao.getAll().map { flow ->
        flow.map { item ->
            mapper.toColorItem(item)
        }
    }

    override fun update(item: ColorItem) {
        dao.update(mapper.toColorDataItem(item))
    }

    override fun insert(item: ColorItem) = dao.insert(mapper.toColorDataItem(item))

    override fun insert(list: List<ColorItem>) {
        val newList = list.map { mapper.toColorDataItem(it) }
        dao.insert(newList)
    }

    override fun remove(item: ColorItem) {
        dao.remove(mapper.toColorDataItem(item))
    }

    override fun removeAll() {
        dao.removeAll()
    }
}