package com.pandacorp.domain.usecases

import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.repositories.DataRepositoryInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetDatabaseItemsUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    suspend operator fun invoke(): MutableList<ListItem> = withContext(Dispatchers.IO) {
        return@withContext dataRepositoryInterface.getDatabaseItems()
    }
}