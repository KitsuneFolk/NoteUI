package com.pandacorp.notesui.utils


import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.pandacorp.notesui.R
import java.util.*

class ThemeHandler(private val context: Context) {
    private val TAG = "ThemeHandler"
    
    private val Theme_Blue = "blue"
    private val Theme_Dark = "dark"
    private val Theme_Red = "red"
    
    private var sp: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    
    fun getColorPrimary(): Int {
        val colorPrimary = when (sp.getString("Themes", "blue")) {
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
                            "Themes",
                            "blue"))
        }
        return colorPrimary
    }
    
    fun getColorAccent(): Int {
        val colorAccent = when (sp.getString("Themes", "blue")) {
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
                            "Themes",
                            "blue"))
        }
        return colorAccent
    }
    
    fun getColorBackground(): Int {
        val colorBackground = when (sp.getString("Themes", "blue")) {
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
                            "Themes",
                            "blue"))
        }
        return colorBackground
    }
    
    fun getTheme(context: Context?): Int {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = sp.getString("Themes", Theme_Blue)
        val themeId: Int = when (theme) {
            Theme_Blue -> R.style.BlueTheme
            Theme_Dark -> R.style.DarkTheme
            Theme_Red -> R.style.RedTheme
            else -> throw IllegalStateException("Unexpected value: $theme")
        }
        return themeId
    }
    
    fun load() {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = sp.getString("Themes", Theme_Blue)!!
        val language = sp.getString("Languages", "")!!
        setMyTheme(context, theme)
        setMyLanguage(context, language)
    }
    
    private fun setMyTheme(context: Context, theme: String) {
        when (theme) {
            Theme_Blue -> context.setTheme(R.style.BlueTheme)
            Theme_Dark -> context.setTheme(R.style.DarkTheme)
            Theme_Red -> context.setTheme(R.style.RedTheme)
        }
    }
    
    private fun setMyLanguage(context: Context, language: String) {
        when (language) {
            "ru" -> {
                val russian_locale = Locale("ru")
                Locale.setDefault(russian_locale)
                val configuration = Configuration()
                configuration.locale = russian_locale
                context.resources.updateConfiguration(configuration, null)
            }
            "en" -> {
                val english_locale = Locale("en")
                Locale.setDefault(english_locale)
                val configuration = Configuration()
                configuration.locale = english_locale
                context.resources.updateConfiguration(configuration, null)
            }
            "uk" -> {
                val ukrainian_locale = Locale("uk")
                Locale.setDefault(ukrainian_locale)
                val configuration = Configuration()
                configuration.locale = ukrainian_locale
                context.resources.updateConfiguration(configuration, null)
            }
        }
    }
    
}