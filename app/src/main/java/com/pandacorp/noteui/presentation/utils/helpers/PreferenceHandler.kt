package com.pandacorp.noteui.presentation.utils.helpers

import android.content.Context
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import com.pandacorp.noteui.app.R
import java.util.Locale


object PreferenceHandler {
    private const val themeFollowSystem = "follow_system"
    private const val themeBlue = "blue"
    private const val themeDark = "dark"
    private const val themeRed = "red"
    private const val themePurple = "purple"

    private const val themeDefault = themeFollowSystem


    private val russianLocale = Locale("ru")
    private val englishLocale = Locale("en")
    private val ukrainianLocale = Locale("uk")

    fun load(context: Context) {
        val sp = PreferenceManager.getDefaultSharedPreferences(context)
        val theme = sp.getString(Constants.Preferences.themesKey, themeDefault)!!
        val language = sp.getString(Constants.Preferences.languagesKey, "")!!
        setMyTheme(context, theme)
        setMyLanguage(context, language)
    }

    private fun isDeviceDarkMode(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    private fun setMyTheme(context: Context, theme: String) {
        when (theme) {
            themeFollowSystem -> {
                if (isDeviceDarkMode(context)) context.setTheme(R.style.DarkTheme)
                else context.setTheme(R.style.BlueTheme)

            }

            themeBlue -> context.setTheme(R.style.BlueTheme)
            themeDark -> context.setTheme(R.style.DarkTheme)
            themeRed -> context.setTheme(R.style.RedTheme)
            themePurple -> context.setTheme(R.style.PurpleTheme)
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
}