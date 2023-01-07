package com.pandacorp.notesui.di

import com.pandacorp.domain.usecases.utils.HideToolbarWhileScrollingUseCase
import com.pandacorp.domain.usecases.utils.SpannableToJsonUseCase
import org.koin.dsl.module

val utilsModule = module {
    single {
        SpannableToJsonUseCase()
    }
    single {
        HideToolbarWhileScrollingUseCase()
    }
    
    
}