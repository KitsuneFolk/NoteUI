package com.pandacorp.noteui.domain.usecase.color

import com.pandacorp.noteui.domain.model.ColorItem
import com.pandacorp.noteui.domain.repository.ColorRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RemoveColorUseCase(private val repository: ColorRepository) {
    suspend operator fun invoke(item: ColorItem) = withContext(Dispatchers.IO) {
        repository.remove(item)
    }
}