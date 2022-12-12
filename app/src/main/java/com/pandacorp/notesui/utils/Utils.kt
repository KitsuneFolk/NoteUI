package com.pandacorp.notesui.utils

import com.pandacorp.notesui.R


class Utils {
    
    companion object {
        
        //Images that user can choose to set note background.
        val backgroundImages = listOf(
                R.drawable.image_night_city,
                R.drawable.image_city,
                R.drawable.image_nature,
                R.drawable.image_moon,
                R.drawable.image_nature2)
        
        //This function is needed for coroutines logs work on Xiaomi devices.
        fun setupExceptionHandler() {
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                throw(throwable)
                
            }
        }
        
    }
    
    
}