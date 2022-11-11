package com.pandacorp.notesui.viewModels

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.usecases.colors.AddColorUseCase
import com.pandacorp.domain.usecases.colors.GetColorsUseCase
import com.pandacorp.domain.usecases.colors.RemoveColorUseCase
import com.pandacorp.notesui.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(
    private val getColorsListUseCase: GetColorsUseCase,
    private val addColorUseCase: AddColorUseCase,
    private val removeColorUseCase: RemoveColorUseCase
) :
    ViewModel() {
    var colorsList = MutableLiveData<MutableList<ColorItem>>()
    
    init {
        viewModelScope.launch {
            colorsList.postValue(getColorsListUseCase())
            
            
        }
    }
    
    fun addColor(colorItem: ColorItem) {
        addColorUseCase(colorItem)
        viewModelScope.launch {
            colorsList.postValue(getColorsListUseCase())
            
        }
    }
    
    fun removeColor(colorItem: ColorItem) {
        
        removeColorUseCase(colorItem)
        viewModelScope.launch {
            colorsList.postValue(getColorsListUseCase())
            
            
        }
    }
    fun addBasicColors(context: Context){
        //On first time opened add add button and 3 default colors.
        val addColorItem =
            ColorItem(color = R.drawable.ic_add_baseline, type = ColorItem.ADD)
        val yellowColorItem = ColorItem(
                color = ContextCompat.getColor(context, R.color.yellow))
        val blueColorItem = ColorItem(
                color = ContextCompat.getColor(context, R.color.blue))
        val redColorItem = ColorItem(
                color = ContextCompat.getColor(context, R.color.red))
        
        CoroutineScope(Dispatchers.IO).launch {
            addColorUseCase(addColorItem)
            addColorUseCase(yellowColorItem)
            addColorUseCase(blueColorItem)
            addColorUseCase(redColorItem)
            viewModelScope.launch {
                colorsList.postValue(getColorsListUseCase())
        
            }
        }
       
        
    }
}