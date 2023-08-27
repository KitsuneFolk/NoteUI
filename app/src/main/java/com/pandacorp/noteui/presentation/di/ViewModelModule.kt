package com.pandacorp.noteui.presentation.di

import android.app.Application
import android.content.Context
import com.pandacorp.noteui.presentation.viewModels.ColorViewModel
import com.pandacorp.noteui.presentation.viewModels.CurrentNoteViewModel
import com.pandacorp.noteui.presentation.viewModels.NotesViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    single<Context> { get<Application>().applicationContext }

    viewModelOf(::ColorViewModel)
    viewModelOf(::NotesViewModel)
    viewModelOf(::CurrentNoteViewModel)
}