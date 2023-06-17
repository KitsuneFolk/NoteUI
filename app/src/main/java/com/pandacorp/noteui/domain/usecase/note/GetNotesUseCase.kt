package com.pandacorp.noteui.domain.usecase.note

import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GetNotesUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(): Flow<List<NoteItem>> = withContext(Dispatchers.IO) {
        return@withContext repository.getAll()
    }
}