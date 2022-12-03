package com.pandacorp.notesui.di

import com.pandacorp.domain.usecases.colors.AddColorUseCase
import com.pandacorp.domain.usecases.colors.GetColorsUseCase
import com.pandacorp.domain.usecases.colors.RemoveAllColorsUseCase
import com.pandacorp.domain.usecases.colors.RemoveColorUseCase
import com.pandacorp.domain.usecases.notes.AddNoteUseCase
import com.pandacorp.domain.usecases.notes.GetNotesUseCase
import com.pandacorp.domain.usecases.notes.RemoveNoteUseCase
import com.pandacorp.domain.usecases.notes.UpdateNoteUseCase
import org.koin.dsl.module

val domainModule = module {
    single {
        AddNoteUseCase(get())
    }
    single {
        RemoveNoteUseCase(get())
    }
    single {
        UpdateNoteUseCase(get())
    }
    single {
        GetNotesUseCase(get())
    }
    
    single {
        AddColorUseCase(get())
    }
    single {
        RemoveColorUseCase(get())
    }
    single {
        RemoveAllColorsUseCase(get())
    }
    single {
        GetColorsUseCase(get())
    }
    
}