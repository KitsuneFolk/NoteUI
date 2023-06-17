package com.pandacorp.noteui.domain.usecase.color

import com.pandacorp.noteui.domain.model.ColorItem
import com.pandacorp.noteui.domain.repository.ColorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AddColorsUseCase(private val repository: ColorRepository) {
    suspend operator fun invoke(list: List<ColorItem>) = withContext(Dispatchers.IO) {
        return@withContext repository.insert(list)
    }
}