package com.pandacorp.domain.usecases.notes

import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.repositories.DataRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RemoveNoteUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    operator fun invoke(noteItem: NoteItem) {
        CoroutineScope(Dispatchers.IO).launch {
            dataRepositoryInterface.removeNote(noteItem)
            
        }
    }
}