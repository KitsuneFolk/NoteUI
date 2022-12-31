package com.pandacorp.notesui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.usecases.notes.database.AddNoteUseCase
import com.pandacorp.domain.usecases.notes.database.GetNotesUseCase
import com.pandacorp.domain.usecases.notes.database.RemoveNoteUseCase
import com.pandacorp.notesui.presentation.activities.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val removeNoteUseCase: RemoveNoteUseCase
) :
    ViewModel() {
    private val TAG = MainActivity.TAG
    var notesList = MutableLiveData<MutableList<NoteItem>>()
    
    init {
        updateNotes()
    }
    
    fun addNote(position: Int, note: NoteItem) {
        notesList.value?.add(position, note)
        notesList.postValue(notesList.value)
        CoroutineScope(Dispatchers.IO).launch {
            addNoteUseCase(note)
        }
    }
    
    fun removeNote(note: NoteItem) {
        notesList.value?.remove(note)
        notesList.postValue(notesList.value)
        CoroutineScope(Dispatchers.IO).launch {
            removeNoteUseCase(note)
        }
    }
    
    fun updateNotes() {
        notesList.value?.clear()
        CoroutineScope(Dispatchers.IO).launch {
            notesList.postValue(getNotesUseCase())
        }
    }
    
    fun restoreNotes(notes: List<Pair<NoteItem, Int>>) {
        notes.forEach { pair ->
            val note = pair.first
            val position = pair.second
            addNote(position, note)
        }
    }
    
}
