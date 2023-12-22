package com.pandacorp.noteui.presentation.di.app

import android.app.Application
import com.pandacorp.noteui.presentation.di.dataModule
import com.pandacorp.noteui.presentation.di.domainModule
import com.pandacorp.noteui.presentation.di.viewModelModule
import com.pandacorp.noteui.presentation.utils.helpers.PreferenceHandler
import com.pandacorp.noteui.presentation.utils.themes.ViewHelper
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {
    var isSettingsChanged = false

    override fun onCreate() {
        Thread.setDefaultUncaughtExceptionHandler { _, throwable -> throw (throwable) } // Throw uncaught exceptions
        super.onCreate()

        ViewHelper.currentTheme = PreferenceHandler.getTheme(applicationContext)

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