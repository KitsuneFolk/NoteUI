package com.pandacorp.notesui.app

import android.app.Application
import androidx.preference.PreferenceManager
import com.pandacorp.notesui.di.appModule
import com.pandacorp.notesui.di.dataModule
import com.pandacorp.notesui.di.domainModule
import com.pandacorp.notesui.di.noteActivityControllersModule
import com.pandacorp.notesui.viewModels.NoteViewModel
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class App : Application() {
    private val vm: NoteViewModel by inject()
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@App)
            modules(listOf(appModule, dataModule, domainModule, noteActivityControllersModule))
        }
        // Here check is app started first time, and if Yes - add basic colors.
        // I do it here to avoid bug when not all ColorItems could load.
        checkIsFirstTime()
    }
    
    private fun checkIsFirstTime() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isFirstTime", true)) {
            
            vm.addBasicColors(this)
            
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean("isFirstTime", false).apply()
        }
        
    }
    
}