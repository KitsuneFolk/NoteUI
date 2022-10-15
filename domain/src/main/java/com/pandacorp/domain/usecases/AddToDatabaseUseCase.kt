package com.pandacorp.domain.usecases

import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.repositories.DataRepositoryInterface

class AddToDatabaseUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    fun execute(table: String, listItem: ListItem){
        dataRepositoryInterface.add(table, listItem)
    }
}