package com.pandacorp.notesui.di

import com.pandacorp.notesui.controllers.InitActionBottomMenuController
import com.pandacorp.notesui.controllers.InitSlidingDrawerMenuController
import org.koin.dsl.module

val noteActivityControllersModule = module{
    single {
        InitActionBottomMenuController()
    }
    single {
        InitSlidingDrawerMenuController(get())
    }
}