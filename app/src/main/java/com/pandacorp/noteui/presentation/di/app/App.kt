package com.pandacorp.noteui.presentation.di.app

import android.app.Application
import com.pandacorp.noteui.presentation.di.dataModule
import com.pandacorp.noteui.presentation.di.domainModule
import com.pandacorp.noteui.presentation.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {
    var isSettingsChanged = false

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)

            modules(
                listOf(
                    viewModelModule,
                    dataModule,
                    domainModule
                )
            )
        }
    }

}