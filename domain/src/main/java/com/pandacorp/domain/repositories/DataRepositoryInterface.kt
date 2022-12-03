package com.pandacorp.domain.repositories

import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.models.NoteItem

interface DataRepositoryInterface {
    fun getNotes(): MutableList<NoteItem>
    fun updateNote(noteItem: NoteItem)
    fun addNote(noteItem: NoteItem)
    fun removeNote(noteItem: NoteItem)
    
    fun getColors(): MutableList<ColorItem>
    fun updateColor(colorItem: ColorItem)
    fun addColor(colorItem: ColorItem)
    fun removeColor(colorItem: ColorItem)
    fun removeAllColors()
}