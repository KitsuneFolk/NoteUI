package com.pandacorp.noteui.presentation.viewModels

import android.util.SparseBooleanArray
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.domain.usecase.note.AddNoteUseCase
import com.pandacorp.noteui.domain.usecase.note.AddNotesUseCase
import com.pandacorp.noteui.domain.usecase.note.GetNotesUseCase
import com.pandacorp.noteui.domain.usecase.note.RemoveNotesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class NotesViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val addNotesUseCase: AddNotesUseCase,
    private val removeNotesUseCase: RemoveNotesUseCase,
) : ViewModel() {

    val notesList = runBlocking {
        withContext(Dispatchers.IO) {
            getNotesUseCase()
        }
    }
    var selectedNotes = MutableLiveData(SparseBooleanArray())

    var filteredNotes = MutableLiveData<MutableList<NoteItem>>(null)
    var searchViewText = MutableLiveData("")

    suspend fun addNote(noteItem: NoteItem): Long {
        return withContext(Dispatchers.IO) {
            addNoteUseCase(noteItem)
        }
    }

    // Test if the restored notes will be on their positions
    fun restoreNotes(notes: List<Pair<NoteItem, Int>>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addNotesUseCase(notes.map { it.first })
            }
        }
    }

    fun removeNotes(list: List<NoteItem>) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                removeNotesUseCase(list)
            }
        }
    }
}
