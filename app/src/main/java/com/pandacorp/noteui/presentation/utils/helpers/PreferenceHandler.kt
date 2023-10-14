package com.pandacorp.noteui.presentation.utils.helpers

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
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
        theme: String = getTheme(context),
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

    fun getThemeBackground(context: Context): Drawable? {
        val drawable = when (getTheme(context)) {
            Theme.FOLLOW_SYSTEM -> {
                if (isDeviceDarkMode(context)) {
                    ContextCompat.getDrawable(context, R.drawable.dark_theme_background)
                } else {
                    ContextCompat.getDrawable(context, R.drawable.blue_theme_background)
                }
            }

            Theme.BLUE -> ContextCompat.getDrawable(context, R.drawable.blue_theme_background)
            Theme.DARK -> ContextCompat.getDrawable(context, R.drawable.dark_theme_background)
            Theme.RED -> ColorDrawable(ContextCompat.getColor(context, R.color.RedTheme_colorBackground))
            Theme.PURPLE -> ContextCompat.getDrawable(context, R.drawable.purple_theme_background)
            else -> throw IllegalArgumentException("Theme = ${getTheme(context)}")
        }
        return drawable
    }

    private fun getTheme(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(Constants.Preferences.Key.THEME, Theme.DEFAULT)!!
    }
}