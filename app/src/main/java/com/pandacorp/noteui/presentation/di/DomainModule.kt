package com.pandacorp.noteui.presentation.di

import com.pandacorp.noteui.domain.usecase.text.JsonToSpannableUseCase
import com.pandacorp.noteui.domain.usecase.text.SpannableToJsonUseCase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val domainModule = module {
    singleOf(::JsonToSpannableUseCase)
    singleOf(::SpannableToJsonUseCase)
}