package com.pandacorp.domain.usecases

import com.pandacorp.domain.repositories.DataRepositoryInterface

class RemoveFromDatabaseByIdUseCase(private val dataRepositoryInterface: DataRepositoryInterface) {
    fun execute(table: String, id: Int){
        dataRepositoryInterface.removeById(table, id)
    }
}