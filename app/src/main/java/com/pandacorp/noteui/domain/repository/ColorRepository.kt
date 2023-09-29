package com.pandacorp.noteui.domain.repository

import com.pandacorp.noteui.domain.model.ColorItem
import kotlinx.coroutines.flow.Flow

interface ColorRepository {
    fun getAll(): Flow<List<ColorItem>>

    fun update(item: ColorItem)

    fun insert(item: ColorItem)

    fun insert(list: List<ColorItem>)

    fun remove(item: ColorItem)

    fun removeAll()
}