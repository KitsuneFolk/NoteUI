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
import com.pandacorp.notesui.presentation.activities.NoteActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(
    private val context: Context,
    private val getColorsListUseCase: GetColorsUseCase,
    private val addColorUseCase: AddColorUseCase,
    private val removeColorUseCase: RemoveColorUseCase
) :
    ViewModel() {
    private val TAG = NoteActivity.TAG
    
    var colorsList = MutableLiveData<MutableList<ColorItem>>()
    
    init {
        viewModelScope.launch { colorsList.postValue(getColorsListUseCase()) }
    }
    
    fun addColor(colorItem: ColorItem) {
        colorsList.value?.add(colorItem)
        colorsList.postValue(colorsList.value)
        CoroutineScope(Dispatchers.IO).launch { addColorUseCase(colorItem) }
    }
    
    fun removeColor(colorItem: ColorItem) {
        colorsList.value?.remove(colorItem)
        colorsList.postValue(colorsList.value)
        
        CoroutineScope(Dispatchers.IO).launch { removeColorUseCase(colorItem) }
    }
    
    fun addBasicColors(addAddItem: Boolean = true) {
        //On first time opened add add button and 3 default colors.
        val basicColorsList = mutableListOf(
                ColorItem(color = 0, type = ColorItem.ADD),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_yellow)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_green)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_blue)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_pink)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_lime)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_red)),
                ColorItem(color = ContextCompat.getColor(context, R.color.light_purple)),
        )
        if (!addAddItem) basicColorsList.removeAt(0)
        basicColorsList.forEach { colorsList.value?.add(it) }
        colorsList.postValue(colorsList.value)
        CoroutineScope(Dispatchers.IO).launch { basicColorsList.forEach { addColorUseCase(it) } }
        
    }
    
    fun resetColors() {
        CoroutineScope(Dispatchers.IO).launch {
            removeAllColors()
            addBasicColors(addAddItem = false)
        }
    }
    
    fun removeAllColors() {
        val tempList = colorsList.value?.toMutableList()
        tempList?.forEach { colorItem ->
            if (colorItem.type == ColorItem.ADD) return@forEach // don't remove add button
            else {
                colorsList.value?.remove(colorItem)
                CoroutineScope(Dispatchers.IO).launch {
                    removeColorUseCase(colorItem) }
            }
            
        }
        colorsList.postValue(colorsList.value)
        
    }
}