package com.pandacorp.notesui.presentation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.usecases.AddToDatabaseUseCase
import com.pandacorp.domain.usecases.GetDatabaseItemsUseCase
import com.pandacorp.domain.usecases.RemoveFromDatabaseUseCase
import kotlinx.coroutines.launch

class MainViewModel(
    private val getListItemsUseCase: GetDatabaseItemsUseCase,
    private val addToDatabaseUseCase: AddToDatabaseUseCase,
    private val removeFromDatabaseUseCase: RemoveFromDatabaseUseCase
) :
    ViewModel() {
    var notesList = MutableLiveData<MutableList<ListItem>>()
    
    init {
        Log.d("MainActivity", "MainViewModel: init")
        viewModelScope.launch {
            notesList.postValue(getListItemsUseCase()!!)
            
            
        }
    }
    
    fun addNote(listItem: ListItem) {
        addToDatabaseUseCase(listItem)
        
        viewModelScope.launch {
            notesList.postValue(getListItemsUseCase()!!)
            
        }
    }
    
    fun removeNote(listItem: ListItem) {
        removeFromDatabaseUseCase(listItem)
        
        viewModelScope.launch {
            notesList.postValue(getListItemsUseCase()!!)
            
            
        }
    }
    
    // fun update(position: Int, header: String, content: String) {
    //     with(notesList.value!![position]) {
    //         this.header = header
    //         this.content = content
    //     }
    //     // viewModelScope.launch {
    //     //     notesList.postValue(getListItemsUseCase()!!)
    //     //
    //     // }
    //
    // }
    fun update() {
        viewModelScope.launch {
            notesList.value!!.clear()
            notesList.postValue(getListItemsUseCase()) }
    }
    
    
}