package com.pandacorp.noteui.domain.usecase.color

import com.pandacorp.noteui.domain.repository.ColorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoveAllColorsUseCase(private val repository: ColorRepository) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        repository.removeAll()
    }
}