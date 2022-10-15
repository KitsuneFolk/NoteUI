package com.pandacorp.notesui.di

import com.pandacorp.domain.usecases.AddToDatabaseUseCase
import com.pandacorp.domain.usecases.GetDatabaseItemIdByPosition
import com.pandacorp.domain.usecases.GetDatabaseItemsUseCase
import com.pandacorp.domain.usecases.RemoveFromDatabaseByIdUseCase
import org.koin.dsl.module

val domainModule = module {
    factory{
        AddToDatabaseUseCase(get())
    }
    factory{
        GetDatabaseItemIdByPosition(get())
    }
    factory{
        GetDatabaseItemsUseCase(get())
    }
    factory{
        RemoveFromDatabaseByIdUseCase(get())
    }
}