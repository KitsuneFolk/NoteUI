package com.pandacorp.notesui.di

import com.pandacorp.data.database.DBHelper
import com.pandacorp.data.repositories.DataRepository
import com.pandacorp.domain.repositories.DataRepositoryInterface
import org.koin.dsl.module

val dataModule = module {
    single<DataRepositoryInterface>(){
        DataRepository(DBHelper(get(), null))
    }
}