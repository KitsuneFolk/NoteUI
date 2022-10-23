package com.pandacorp.notesui.di

import com.pandacorp.notesui.presentation.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{
    viewModel<MainViewModel>{
        MainViewModel(get(), get(), get())
    }
}