package com.pandacorp.notesui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.usecases.notes.AddNoteUseCase
import com.pandacorp.domain.usecases.notes.GetNotesUseCase
import com.pandacorp.domain.usecases.notes.RemoveNoteUseCase
import kotlinx.coroutines.launch

class MainViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val removeNoteUseCase: RemoveNoteUseCase
) :
    ViewModel() {
    var notesList = MutableLiveData<MutableList<NoteItem>>()
    
    init {
        viewModelScope.launch {
            notesList.postValue(getNotesUseCase())
            
            
        }
    }
    
    fun addNote(noteItem: NoteItem) {
        addNoteUseCase(noteItem)
        
        viewModelScope.launch {
            notesList.postValue(getNotesUseCase())
            
        }
    }
    
    fun removeNote(noteItem: NoteItem) {
        removeNoteUseCase(noteItem)
        
        viewModelScope.launch {
            notesList.postValue(getNotesUseCase())
            
        }
    }
    
    fun update() {
        viewModelScope.launch {
            notesList.value?.clear()
            notesList.postValue(getNotesUseCase()) }
    }
    
    
}