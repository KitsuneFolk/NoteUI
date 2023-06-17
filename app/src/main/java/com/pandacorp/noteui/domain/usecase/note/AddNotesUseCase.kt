package com.pandacorp.noteui.domain.usecase.note

import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddNotesUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(list: List<NoteItem>) = withContext(Dispatchers.IO) {
        repository.insert(list)
    }
}