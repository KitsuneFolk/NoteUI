package com.pandacorp.notesui.viewModels

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.usecases.colors.AddColorUseCase
import com.pandacorp.domain.usecases.colors.GetColorsUseCase
import com.pandacorp.domain.usecases.colors.RemoveAllColorsUseCase
import com.pandacorp.domain.usecases.colors.RemoveColorUseCase
import com.pandacorp.notesui.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(
    private val getColorsListUseCase: GetColorsUseCase,
    private val addColorUseCase: AddColorUseCase,
    private val removeColorUseCase: RemoveColorUseCase,
    private val removeAllColorsUseCase: RemoveAllColorsUseCase
) :
    ViewModel() {
    var colorsList = MutableLiveData<MutableList<ColorItem>>()
    
    init {
        viewModelScope.launch {
            colorsList.postValue(getColorsListUseCase())
            
            
        }
    }
    
    fun addColor(colorItem: ColorItem) {
        CoroutineScope(Dispatchers.IO).launch {
            addColorUseCase(colorItem)
            viewModelScope.launch {
                colorsList.postValue(getColorsListUseCase())
            }
            
        }
        
    }
    
    fun removeColor(colorItem: ColorItem) {
        CoroutineScope(Dispatchers.IO).launch {
            removeColorUseCase(colorItem)
            viewModelScope.launch {
                colorsList.postValue(getColorsListUseCase())
                
                
            }
            
        }
    }
    
    fun addBasicColors(context: Context) {
        //On first time opened add add button and 3 default colors.
        val basicColorsList = mutableListOf(
                ColorItem(
                        color = R.drawable.ic_add_baseline,
                        type = ColorItem.ADD),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_yellow)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_green)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_blue)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_pink)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_red)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_purple)),
        )
        CoroutineScope(Dispatchers.IO).launch {
            basicColorsList.forEach { addColorUseCase(it) }
            
            viewModelScope.launch {
                colorsList.postValue(getColorsListUseCase())
                
            }
        }
        
        
    }
    
    fun resetColors(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            removeAllColorsUseCase()
            addBasicColors(context = context)
            viewModelScope.launch {
                colorsList.postValue(getColorsListUseCase())
                
            }
            
        }
    }
}