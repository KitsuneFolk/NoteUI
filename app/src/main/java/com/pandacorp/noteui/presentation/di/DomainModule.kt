package com.pandacorp.noteui.presentation.di

import com.pandacorp.noteui.domain.usecase.color.AddColorUseCase
import com.pandacorp.noteui.domain.usecase.color.AddColorsUseCase
import com.pandacorp.noteui.domain.usecase.color.GetColorsUseCase
import com.pandacorp.noteui.domain.usecase.color.RemoveAllColorsUseCase
import com.pandacorp.noteui.domain.usecase.color.RemoveColorUseCase
import com.pandacorp.noteui.domain.usecase.note.AddNoteUseCase
import com.pandacorp.noteui.domain.usecase.note.AddNotesUseCase
import com.pandacorp.noteui.domain.usecase.note.GetNotesUseCase
import com.pandacorp.noteui.domain.usecase.note.RemoveNoteUseCase
import com.pandacorp.noteui.domain.usecase.note.RemoveNotesUseCase
import com.pandacorp.noteui.domain.usecase.note.UpdateNoteUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    singleOf(::AddNotesUseCase)
    singleOf(::AddNoteUseCase)
    singleOf(::GetNotesUseCase)
    singleOf(::RemoveNotesUseCase)
    singleOf(::RemoveNoteUseCase)
    singleOf(::UpdateNoteUseCase)

    singleOf(::AddColorUseCase)
    singleOf(::AddColorsUseCase)
    singleOf(::RemoveColorUseCase)
    singleOf(::RemoveAllColorsUseCase)
    singleOf(::GetColorsUseCase)
}