package com.pandacorp.notesui.utils

import com.pandacorp.notesui.R


class Utils {
    
    companion object {
        
        //Images that user can choose to set note background.
        val backgroundImagesIds = listOf(
                R.drawable.image_android,
                R.drawable.image_river,
                R.drawable.image_field,
                R.drawable.image_moon,
                R.drawable.image_flower,
                R.drawable.image_skyscrapers)
        
        //This function is needed for coroutines logs work on Xiaomi devices.
        fun setupExceptionHandler() {
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                throw(throwable)
                
            }
        }
        
    }
    
    
}