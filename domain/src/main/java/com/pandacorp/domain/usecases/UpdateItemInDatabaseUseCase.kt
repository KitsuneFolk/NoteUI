package com.pandacorp.domain.usecases

import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.repositories.DataRepositoryInterface

class UpdateItemInDatabaseUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    operator fun invoke(listItem: ListItem){
        dataRepositoryInterface.update(listItem)
        
    }
}