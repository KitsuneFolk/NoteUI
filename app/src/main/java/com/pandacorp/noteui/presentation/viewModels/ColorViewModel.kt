package com.pandacorp.noteui.presentation.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.noteui.domain.model.ColorItem
import com.pandacorp.noteui.domain.usecase.color.AddColorUseCase
import com.pandacorp.noteui.domain.usecase.color.AddColorsUseCase
import com.pandacorp.noteui.domain.usecase.color.GetColorsUseCase
import com.pandacorp.noteui.domain.usecase.color.RemoveAllColorsUseCase
import com.pandacorp.noteui.domain.usecase.color.RemoveColorUseCase
import com.pandacorp.noteui.presentation.utils.helpers.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ColorViewModel(
    private val application: Application,
    private val getColorsUseCase: GetColorsUseCase,
    private val addColorUseCase: AddColorUseCase,
    private val addColorsUseCase: AddColorsUseCase,
    private val removeColorCase: RemoveColorUseCase,
    private val removeAllUseCase: RemoveAllColorsUseCase
) :
    AndroidViewModel(application) {

    val colorsList = runBlocking {
        withContext(Dispatchers.IO) {
            getColorsUseCase()
        }
    }

    fun addColor(colorItem: ColorItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                addColorUseCase(colorItem)
            }
        }
    }

    fun removeColor(colorItem: ColorItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                removeColorCase(colorItem)
            }
        }
    }

    fun resetColors() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                removeAllUseCase()
                addColorsUseCase(Utils.getDefaultColorsList(application))
            }
        }
    }

    fun removeAllColors() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                removeAllUseCase()
            }
        }
    }
}