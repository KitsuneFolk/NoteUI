package com.pandacorp.notesui.utils

import android.util.Log

class Utils {
    
    //This class is needed for coroutines logs work on Xiaomi devices.
    companion object {
        val CONTEXT_MENU_DELETE_ID = 1
        fun setupExceptionHandler() {
            val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                val message = "Uncaught exception in thread ${thread.name}:\n"
                Log.e("AndroidRuntime", message, throwable)
                defaultUncaughtExceptionHandler?.uncaughtException(thread, throwable)
            }
        }
    }
}