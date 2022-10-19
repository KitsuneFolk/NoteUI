package com.pandacorp.notesui.di

import com.pandacorp.domain.usecases.*
import org.koin.dsl.module

val domainModule = module {
    factory {
        AddToDatabaseUseCase(get())
    }
    factory {
        RemoveFromDatabaseUseCase(get())
    }
    factory {
        UpdateItemInDatabaseUseCase(get())
    }
    factory {
        GetDatabaseItemsUseCase(get())
    }
    factory {
        GetDatabaseItemByAdapterPositionUseCase(get())
    }
    
}