package com.pandacorp.notesui.utils


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.pandacorp.notesui.R
import com.pandacorp.notesui.presentation.settings.PreferencesKeys
import java.util.*

class ThemeHandler(private val context: Context) {
    private val TAG = "Utils"
    
    private val Theme_FollowSystem = "follow_system"
    private val Theme_Blue = "blue"
    private val Theme_Dark = "dark"
    private val Theme_Red = "red"
    
    private val Theme_default = Theme_FollowSystem
    
    private val russianLocale = Locale("ru")
    private val englishLocale = Locale("en")
    private val ukrainianLocale = Locale("uk")
    
    private var sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    fun getColorPrimary(): Int {
        val colorPrimary = when (sp.getString(PreferencesKeys.themesKey, Theme_default)) {
            Theme_FollowSystem -> {
                if (isDeviceDarkMode()) R.color.DarkTheme_colorPrimary else R.color.BlueTheme_colorPrimary
            }
            Theme_Blue -> {
                R.color.BlueTheme_colorPrimary
            }
            Theme_Dark -> {
                R.color.DarkTheme_colorPrimary
            }
            Theme_Red -> {
                R.color.RedTheme_colorPrimary
            }
            else -> throw IllegalStateException(
                    "Unexpected value: " + sp.getString(
                            PreferencesKeys.themesKey,
                            "blue"))
        }
        return colorPrimary
    }
    
    fun getColorAccent(): Int {
        val colorAccent = when (sp.getString(PreferencesKeys.themesKey, Theme_default)) {
            Theme_FollowSystem -> {
                if (isDeviceDarkMode()) R.color.DarkTheme_colorAccent else R.color.BlueTheme_colorAccent
            }
            Theme_Blue -> {
                R.color.BlueTheme_colorAccent
            }
            Theme_Dark -> {
                R.color.DarkTheme_colorAccent
            }
            Theme_Red -> {
                R.color.RedTheme_colorAccent
            }
            else -> throw IllegalStateException(
                    "Unexpected value: " + sp.getString(
                            PreferencesKeys.themesKey,
                            "blue"))
        }
        return colorAccent
    }
    
    fun getColorBackground(): Int {
        val colorBackground = when (sp.getString(PreferencesKeys.themesKey, Theme_default)) {
            Theme_FollowSystem -> {
                if (isDeviceDarkMode()) R.color.DarkTheme_colorBackground else R.color.BlueTheme_colorBackground
            }
            Theme_Blue -> {
                R.color.BlueTheme_colorBackground
            }
            Theme_Dark -> {
                R.color.DarkTheme_colorBackground
            }
            Theme_Red -> {
                R.color.RedTheme_colorBackground
            }
            else -> throw IllegalStateException(
                    "Unexpected value: " + sp.getString(
                            PreferencesKeys.themesKey,
                            Theme_default))
        }
        return colorBackground
    }
    
    fun getTheme(context: Context?): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = sp.getString("Themes", Theme_default)
        val themeId: Int = when (theme) {
            Theme_FollowSystem -> if (isDeviceDarkMode()) R.style.DarkTheme else R.style.BlueTheme
            Theme_Blue -> R.style.BlueTheme
            Theme_Dark -> R.style.DarkTheme
            Theme_Red -> R.style.RedTheme
            else -> throw IllegalStateException("Unexpected value: $theme")
        }
        return themeId
    }
    
    fun load() {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = sp.getString(PreferencesKeys.themesKey, Theme_default)!!
        val language = sp.getString(PreferencesKeys.languagesKey, "")!!
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
        }
    }
    
    private fun setMyLanguage(context: Context, language: String) {
        
        when (language) {
            "ru" -> {
                Locale.setDefault(russianLocale)
                val configuration = Configuration()
                configuration.locale = russianLocale
                context.resources.updateConfiguration(configuration, null)
            }
            "en" -> {
                Locale.setDefault(englishLocale)
                val configuration = Configuration()
                configuration.locale = englishLocale
                context.resources.updateConfiguration(configuration, null)
            }
            "uk" -> {
                Locale.setDefault(ukrainianLocale)
                val configuration = Configuration()
                configuration.locale = ukrainianLocale
                context.resources.updateConfiguration(configuration, null)
            }
        }
    }
    
    private fun isDeviceDarkMode(): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
    
}