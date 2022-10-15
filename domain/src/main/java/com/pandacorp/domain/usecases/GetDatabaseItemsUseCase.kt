package com.pandacorp.domain.usecases

import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.repositories.DataRepositoryInterface

class GetDatabaseItemsUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    fun execute(table: String): MutableList<ListItem>{
        
        return dataRepositoryInterface.getDatabaseItems(table)
    }
}