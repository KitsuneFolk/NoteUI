package com.pandacorp.domain.usecases.colors

import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.repositories.DataRepositoryInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddColorUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    operator fun invoke(colorItem: ColorItem) {
        CoroutineScope(Dispatchers.IO).launch {
            dataRepositoryInterface.addColor(colorItem)
            
        }
        
    }
}