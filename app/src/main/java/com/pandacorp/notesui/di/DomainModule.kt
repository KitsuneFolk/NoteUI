package com.pandacorp.notesui.di

import com.pandacorp.domain.usecases.AddToDatabaseUseCase
import com.pandacorp.domain.usecases.GetDatabaseItemByAdapterPositionUseCase
import com.pandacorp.domain.usecases.GetDatabaseItemsUseCase
import com.pandacorp.domain.usecases.RemoveFromDatabaseUseCase
import org.koin.dsl.module

val domainModule = module {
    factory{
        AddToDatabaseUseCase(get())
    }
    factory{
        GetDatabaseItemsUseCase(get())
    }
    factory{
        RemoveFromDatabaseUseCase(get())
    }
    factory{
        GetDatabaseItemByAdapterPositionUseCase(get())
    }
}