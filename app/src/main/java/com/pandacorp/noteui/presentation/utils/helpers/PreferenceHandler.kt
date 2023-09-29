package com.pandacorp.noteui.presentation.utils.helpers

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import com.pandacorp.noteui.app.R
import java.util.Locale

object PreferenceHandler {
    private object Theme {
        const val FOLLOW_SYSTEM = "follow_system"
        const val BLUE = "blue"
        const val DARK = "dark"
        const val RED = "red"
        const val PURPLE = "purple"
        const val DEFAULT = FOLLOW_SYSTEM
    }

    private fun isDeviceDarkMode(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    fun setTheme(
        context: Context,
        theme: String =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Constants.Preferences.Key.THEME, Theme.DEFAULT)!!,
    ) {
        when (theme) {
            Theme.FOLLOW_SYSTEM -> {
                if (isDeviceDarkMode(context)) {
                    context.setTheme(R.style.DarkTheme)
                } else {
                    context.setTheme(R.style.BlueTheme)
                }
            }

            Theme.BLUE -> context.setTheme(R.style.BlueTheme)
            Theme.DARK -> context.setTheme(R.style.DarkTheme)
            Theme.RED -> context.setTheme(R.style.RedTheme)
            Theme.PURPLE -> context.setTheme(R.style.PurpleTheme)
        }
    }

    fun setLanguage(
        context: Context,
        language: String =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                    Constants.Preferences.Key.LANGUAGE,
                    context.resources.getString(R.string.settings_language_default_value),
                )!!,
    ) {
        Locale.setDefault(Locale(language))
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language))
    }
}