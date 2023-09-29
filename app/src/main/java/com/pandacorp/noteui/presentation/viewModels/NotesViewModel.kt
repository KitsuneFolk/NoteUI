package com.pandacorp.noteui.presentation.viewModels

import android.util.SparseBooleanArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class NotesViewModel(private val noteRepository: NoteRepository) : ViewModel() {
    val notesList =
        runBlocking {
            withContext(Dispatchers.IO) {
                noteRepository.getAll()
            }
        }
    var selectedNotes = MutableLiveData(SparseBooleanArray())

    var filteredNotes = MutableLiveData<MutableList<NoteItem>>(null)
    var searchViewText = MutableLiveData("")

    suspend fun addNote(noteItem: NoteItem): Long {
        return withContext(Dispatchers.IO) {
            noteRepository.insert(noteItem)
        }
    }

    fun restoreNotes(notes: List<NoteItem>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                noteRepository.insert(notes)
            }
        }
    }

    fun removeNotes(list: List<NoteItem>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                noteRepository.remove(list)
            }
        }
    }

    fun getNoteById(id: Long): NoteItem = notesList.value!!.first { it.id == id }
}