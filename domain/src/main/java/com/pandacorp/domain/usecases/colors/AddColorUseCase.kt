package com.pandacorp.domain.usecases.colors

import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.repositories.DataRepositoryInterface

class AddColorUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    operator fun invoke(colorItem: ColorItem) {
            dataRepositoryInterface.addColor(colorItem)
            
        
    }
}