package com.pandacorp.domain.usecases

import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.repositories.DataRepositoryInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetDatabaseItemByAdapterPositionUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    suspend operator fun invoke(position: Int): ListItem = withContext(Dispatchers.IO) {
            
            val notesList = dataRepositoryInterface.getDatabaseItems()
            return@withContext notesList[position]
    }
}