package com.pandacorp.noteui.presentation.utils.helpers

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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

    private fun isDeviceDarkMode(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    fun setTheme(
        context: Context,
        theme: String =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Constants.Preferences.themesKey, themeDefault)!!,
    ) {
        when (theme) {
            themeFollowSystem -> {
                if (isDeviceDarkMode(context)) {
                    context.setTheme(R.style.DarkTheme)
                } else {
                    context.setTheme(R.style.BlueTheme)
                }
            }

            themeBlue -> context.setTheme(R.style.BlueTheme)
            themeDark -> context.setTheme(R.style.DarkTheme)
            themeRed -> context.setTheme(R.style.RedTheme)
            themePurple -> context.setTheme(R.style.PurpleTheme)
        }
    }

    fun setLanguage(
        context: Context,
        language: String =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                    Constants.Preferences.languagesKey,
                    context.resources.getString(R.string.settings_language_default_value),
                )!!,
    ) {
        Locale.setDefault(Locale(language))
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    }
}