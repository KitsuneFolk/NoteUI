package com.pandacorp.noteui.domain.usecase.note

import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(item: NoteItem): Long = withContext(Dispatchers.IO) {
       return@withContext repository.insert(item)
    }
}