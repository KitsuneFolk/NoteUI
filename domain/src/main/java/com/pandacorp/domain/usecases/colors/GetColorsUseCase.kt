package com.pandacorp.domain.usecases.colors

import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.repositories.DataRepositoryInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetColorsUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    suspend operator fun invoke(): MutableList<ColorItem> = withContext(Dispatchers.IO) {
        return@withContext dataRepositoryInterface.getColors()
    }
}