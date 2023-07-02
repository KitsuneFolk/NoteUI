package com.pandacorp.noteui.domain.usecase.note

import androidx.lifecycle.LiveData
import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetNotesUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(): LiveData<List<NoteItem>> = withContext(Dispatchers.IO) {
        return@withContext repository.getAll()
    }
}