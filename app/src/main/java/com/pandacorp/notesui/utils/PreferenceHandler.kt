package com.pandacorp.notesui.utils


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.pandacorp.notesui.R
import java.util.*

class PreferenceHandler(private val context: Context) {
    private val TAG = "Utils"
    
    private val Theme_FollowSystem = "follow_system"
    private val Theme_Blue = "blue"
    private val Theme_Dark = "dark"
    private val Theme_Red = "red"
    private val Theme_Purple = "purple"
    
    private val Theme_default = Theme_FollowSystem
    
    private val russianLocale = Locale("ru")
    private val englishLocale = Locale("en")
    private val ukrainianLocale = Locale("uk")
    
    private var sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    fun load() {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = sp.getString(Constans.PreferencesKeys.themesKey, Theme_default)!!
        val language = sp.getString(Constans.PreferencesKeys.languagesKey, "")!!
        setMyTheme(context, theme)
        setMyLanguage(context, language)
    }
    
    private fun setMyTheme(context: Context, theme: String) {
        when (theme) {
            Theme_FollowSystem -> {
                if (isDeviceDarkMode()) context.setTheme(R.style.DarkTheme)
                else context.setTheme(R.style.BlueTheme)
                
            }
            Theme_Blue -> context.setTheme(R.style.BlueTheme)
            Theme_Dark -> context.setTheme(R.style.DarkTheme)
            Theme_Red -> context.setTheme(R.style.RedTheme)
            Theme_Purple -> context.setTheme(R.style.PurpleTheme)
            
        }
    }
    
    private fun setMyLanguage(context: Context, language: String) {
        val configuration = Configuration()
        when (language) {
            "ru" -> {
                Locale.setDefault(russianLocale)
                configuration.setLocale(russianLocale)
            }
            "en" -> {
                Locale.setDefault(englishLocale)
                configuration.setLocale(englishLocale)
            }
            "uk" -> {
                Locale.setDefault(ukrainianLocale)
                configuration.setLocale(ukrainianLocale)
            }
        }
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
    }
    
    private fun isDeviceDarkMode(): Boolean = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    
}