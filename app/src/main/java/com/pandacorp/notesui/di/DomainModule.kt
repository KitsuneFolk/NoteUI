package com.pandacorp.notesui.di

import com.pandacorp.domain.usecases.colors.AddColorUseCase
import com.pandacorp.domain.usecases.colors.GetColorsUseCase
import com.pandacorp.domain.usecases.colors.RemoveColorUseCase
import com.pandacorp.domain.usecases.notes.AddNoteUseCase
import com.pandacorp.domain.usecases.notes.GetNotesUseCase
import com.pandacorp.domain.usecases.notes.RemoveNoteUseCase
import com.pandacorp.domain.usecases.notes.UpdateNoteUseCase
import org.koin.dsl.module

val domainModule = module {
    factory {
        AddNoteUseCase(get())
    }
    factory {
        RemoveNoteUseCase(get())
    }
    factory {
        UpdateNoteUseCase(get())
    }
    factory {
        GetNotesUseCase(get())
    }
    
    factory {
        AddColorUseCase(get())
    }
    factory {
        RemoveColorUseCase(get())
    }
    factory {
        GetColorsUseCase(get())
    }
    
}