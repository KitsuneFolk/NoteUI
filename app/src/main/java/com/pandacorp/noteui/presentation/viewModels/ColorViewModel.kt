package com.pandacorp.noteui.presentation.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.noteui.domain.model.ColorItem
import com.pandacorp.noteui.domain.repository.ColorRepository
import com.pandacorp.noteui.presentation.utils.helpers.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ColorViewModel(private val application: Application, private val colorRepository: ColorRepository) :
    AndroidViewModel(application) {
    val colorsList = runBlocking {
        withContext(Dispatchers.IO) {
            colorRepository.getAll()
        }
    }

    fun addColor(colorItem: ColorItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                colorRepository.insert(colorItem)
            }
        }
    }

    fun removeColor(colorItem: ColorItem) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                colorRepository.remove(colorItem)
            }
        }
    }

    fun resetColors() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                colorRepository.removeAll()
                colorRepository.insert(Utils.getDefaultColorsList(application))
            }
        }
    }

    fun removeAllColors() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                colorRepository.removeAll()
            }
        }
    }
}