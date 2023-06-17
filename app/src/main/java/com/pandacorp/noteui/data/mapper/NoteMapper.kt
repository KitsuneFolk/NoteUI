package com.pandacorp.noteui.data.mapper

import com.pandacorp.noteui.data.model.NoteDataItem
import com.pandacorp.noteui.domain.model.NoteItem

class NoteMapper {
    fun toNoteItem(noteDataItem: NoteDataItem): NoteItem =
        NoteItem(
            noteDataItem.id,
            noteDataItem.title,
            noteDataItem.content,
            noteDataItem.background,
            noteDataItem.isShowTransparentActionBar
        )

    fun toNoteDataItem(noteItem: NoteItem): NoteDataItem =
        NoteDataItem(
            noteItem.id,
            noteItem.title,
            noteItem.content,
            noteItem.background,
            noteItem.isShowTransparentActionBar
        )
}