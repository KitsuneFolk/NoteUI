package com.pandacorp.notesui.di

import com.pandacorp.notesui.controllers.InitSlidingDrawerMenuСontroller
import org.koin.dsl.module

val noteActivityControllersModule = module{
    single {
        InitSlidingDrawerMenuСontroller(get())
    }
}