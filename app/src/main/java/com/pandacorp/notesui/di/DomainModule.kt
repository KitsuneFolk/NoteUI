package com.pandacorp.notesui.di

import com.pandacorp.domain.usecases.colors.AddColorUseCase
import com.pandacorp.domain.usecases.colors.GetColorsUseCase
import com.pandacorp.domain.usecases.colors.RemoveAllColorsUseCase
import com.pandacorp.domain.usecases.colors.RemoveColorUseCase
import com.pandacorp.domain.usecases.notes.SetNoteBackgroundUseCase
import com.pandacorp.domain.usecases.notes.database.AddNoteUseCase
import com.pandacorp.domain.usecases.notes.database.GetNotesUseCase
import com.pandacorp.domain.usecases.notes.database.RemoveNoteUseCase
import com.pandacorp.domain.usecases.notes.database.UpdateNoteUseCase
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
    single {
        SetNoteBackgroundUseCase(get())
    }
    
}