package com.pandacorp.notesui.di

import com.pandacorp.notesui.viewModels.MainViewModel
import com.pandacorp.notesui.viewModels.NoteViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{
    viewModel<MainViewModel>{
        MainViewModel(get(), get(), get())
    }
    viewModel<NoteViewModel>{
        NoteViewModel(get(), get(), get(), get())
    }
    
}