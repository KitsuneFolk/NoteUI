package com.pandacorp.domain.usecases.colors

import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.repositories.DataRepositoryInterface

class RemoveColorUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    operator fun invoke(colorItem: ColorItem) {
        dataRepositoryInterface.removeColor(colorItem)
        
    }
}