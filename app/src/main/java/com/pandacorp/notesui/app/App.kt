package com.pandacorp.notesui.app

import android.app.Application
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.notesui.R
import com.pandacorp.notesui.di.appModule
import com.pandacorp.notesui.di.dataModule
import com.pandacorp.notesui.di.domainModule
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
            modules(listOf(appModule, dataModule, domainModule))
        }
        // Here check is app started first time, and if Yes - add basic colors.
        // I do it here to avoid bug when not all ColorItems could load.
        checkIsFirstTime()
    }
    
    private fun checkIsFirstTime() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isFirstTime", true)) {
            //On first time opened add 3 default colors and add button.
            val addColorItem =
                ColorItem(color = R.drawable.ic_add_baseline, type = ColorItem.ADD)
            val yellowColorItem = ColorItem(
                    color = ContextCompat.getColor(this, R.color.yellow))
            val blueColorItem = ColorItem(
                    color = ContextCompat.getColor(this, R.color.blue))
            val redColorItem = ColorItem(
                    color = ContextCompat.getColor(this, R.color.red))
            vm.addColor(addColorItem)
            vm.addColor(yellowColorItem)
            vm.addColor(blueColorItem)
            vm.addColor(redColorItem)
            
            
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean("isFirstTime", false).apply()
        }
        
    }
    
}