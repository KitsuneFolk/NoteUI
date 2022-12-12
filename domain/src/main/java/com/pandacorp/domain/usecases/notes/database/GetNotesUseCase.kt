package com.pandacorp.domain.usecases.notes.database

import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.repositories.DataRepositoryInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetNotesUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    suspend operator fun invoke(): MutableList<NoteItem> = withContext(Dispatchers.IO) {
        return@withContext dataRepositoryInterface.getNotes()
    }
}