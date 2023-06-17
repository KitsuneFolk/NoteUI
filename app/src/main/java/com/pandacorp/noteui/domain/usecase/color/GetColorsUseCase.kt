package com.pandacorp.noteui.domain.usecase.color

import com.pandacorp.noteui.domain.model.ColorItem
import com.pandacorp.noteui.domain.repository.ColorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class GetColorsUseCase(private val repository: ColorRepository) {
    suspend operator fun invoke(): Flow<List<ColorItem>> = withContext(Dispatchers.IO) {
        return@withContext repository.getAll()
    }
}