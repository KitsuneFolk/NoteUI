package com.pandacorp.domain.usecases.notes.database

import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.repositories.DataRepositoryInterface

class AddNoteUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    operator fun invoke(noteItem: NoteItem) {
            dataRepositoryInterface.addNote(noteItem)
            
    }
}