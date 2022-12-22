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
        getNotes()
    }
    
    fun addNote(note: NoteItem) {
        notesList.value?.add(note)
        CoroutineScope(Dispatchers.IO).launch {
            addNoteUseCase(note)
        }
    }
    
    fun removeNote(note: NoteItem) {
        notesList.value?.remove(note)
        CoroutineScope(Dispatchers.IO).launch {
            removeNoteUseCase(note)
        }
    }
    
    fun getNotes() {
        notesList.value?.clear()
        CoroutineScope(Dispatchers.IO).launch {
            notesList.postValue(getNotesUseCase())
        }
    }
    
    
}