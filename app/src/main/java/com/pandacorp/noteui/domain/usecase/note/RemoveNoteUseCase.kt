package com.pandacorp.noteui.domain.usecase.note

import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoveNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(noteItem: NoteItem) = withContext(Dispatchers.IO) {
        repository.remove(noteItem)
    }
}