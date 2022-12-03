package com.pandacorp.domain.usecases.colors

import com.pandacorp.domain.repositories.DataRepositoryInterface

class RemoveAllColorsUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    operator fun invoke() {
        dataRepositoryInterface.removeAllColors()
        
    }
}