package com.pandacorp.notesui.utils


import android.content.Context
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.pandacorp.notesui.R
import java.util.*

class ThemeHandler {
    companion object {
        private const val TAG = "ThemeHandler"
        const val Theme_Blue = "blue"
        const val Theme_Dark = "dark"
        const val Theme_Red = "red"
        const val BACKGROUND_COLOR = "BACKGROUND_COLOR"
        const val ACCENT_COLOR = "ACCENT_COLOR"
        const val PRIMARY_COLOR = "PRIMARY_COLOR"
        fun getThemeColor(context: Context?, color: String): Int {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val background_color: Int
            val accent_color: Int
            val primary_color: Int
            when (sp.getString("Themes", "blue")) {
                Theme_Blue -> {
                    background_color = R.color.BlueTheme_Background
                    accent_color = R.color.BlueTheme_colorAccent
                    primary_color = R.color.BlueTheme_colorPrimary
                }
                Theme_Dark -> {
                    background_color = R.color.DarkTheme_Background
                    accent_color = R.color.DarkTheme_colorAccent
                    primary_color = R.color.DarkTheme_colorPrimary
                }
                Theme_Red -> {
                    background_color = R.color.RedTheme_Background
                    accent_color = R.color.RedTheme_colorAccent
                    primary_color = R.color.RedTheme_colorPrimary
                }
                else -> throw IllegalStateException(
                        "Unexpected value: " + sp.getString(
                                "Themes",
                                "blue"))
            }
            if (color == BACKGROUND_COLOR) return background_color
            if (color == ACCENT_COLOR) return accent_color
            return if (color == PRIMARY_COLOR) primary_color else -1
        }
        
        fun getTheme(context: Context?): Int {
            val sp = PreferenceManager.getDefaultSharedPreferences(context)
            val theme = sp.getString("Themes", Theme_Blue)
            val themeId: Int
            when (theme) {
                Theme_Blue -> themeId = R.style.BlueTheme
                Theme_Dark -> themeId = R.style.DarkTheme
                Theme_Red -> themeId = R.style.RedTheme
                else -> throw IllegalStateException("Unexpected value: $theme")
            }
            return themeId
        }
        
        fun load(context: Context) {
            //Обработка при первом запуске приложения, когда SharedPreferences ещё не созданы.
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
    
    
}