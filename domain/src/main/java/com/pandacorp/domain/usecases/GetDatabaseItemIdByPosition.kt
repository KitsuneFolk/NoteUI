package com.pandacorp.domain.usecases

import com.pandacorp.domain.repositories.DataRepositoryInterface

class GetDatabaseItemIdByPosition(private val dataRepository: DataRepositoryInterface) {
    fun execute(table: String, id: Int): Int?{
        return dataRepository.getDatabaseItemIdByRecyclerViewItemId(table, id)
    }
}