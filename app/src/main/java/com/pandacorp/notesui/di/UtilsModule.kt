package com.pandacorp.notesui.di

import com.pandacorp.domain.usecases.utils.HideToolbarWhileScrollingUseCase
import com.pandacorp.domain.usecases.utils.SpannableToJsonUseCase
import com.pandacorp.domain.usecases.utils.text.*
import org.koin.dsl.module

val utilsModule = module {
    single {
        SpannableToJsonUseCase()
    }
    single {
        HideToolbarWhileScrollingUseCase()
    }
    
    single {
        ChangeTextBackgroundColorUseCase()
    }
    single {
        ChangeTextForegroundColorUseCase()
    }
    single {
        ChangeTextGravityUseCase(get())
    }
    single {
        GetEditTextSelectedLinesUseCase()
    }
    single {
        InsertImageInEditTextUseCase(get())
    }
    single {
        MakeTextBoldUseCase()
    }
    single {
        MakeTextItalicUseCase()
    }
}