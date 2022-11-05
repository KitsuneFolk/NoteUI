package com.pandacorp.notesui.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.usecases.colors.AddColorUseCase
import com.pandacorp.domain.usecases.colors.GetColorsUseCase
import com.pandacorp.domain.usecases.colors.RemoveColorUseCase
import kotlinx.coroutines.launch

class NoteViewModel(
    private val getListItemsUseCase: GetColorsUseCase,
    private val addNoteUseCase: AddColorUseCase,
    private val removeFromDatabaseUseCase: RemoveColorUseCase
) :
    ViewModel() {
    var colorsList = MutableLiveData<MutableList<ColorItem>>()
    
    init {
        viewModelScope.launch {
            colorsList.postValue(getListItemsUseCase())
            
            
        }
    }
    
    fun addColor(colorItem: ColorItem) {
        
        addNoteUseCase(colorItem)
        viewModelScope.launch {
            colorsList.postValue(getListItemsUseCase())
            
        }
    }
    
    fun removeColor(colorItem: ColorItem) {
        
        removeFromDatabaseUseCase(colorItem)
        viewModelScope.launch {
            colorsList.postValue(getListItemsUseCase())
            
            
        }
    }
}