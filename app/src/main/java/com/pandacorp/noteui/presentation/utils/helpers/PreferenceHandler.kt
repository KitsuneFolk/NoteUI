package com.pandacorp.noteui.presentation.utils.helpers

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.presentation.utils.themes.BlueTheme
import com.pandacorp.noteui.presentation.utils.themes.DarkTheme
import com.pandacorp.noteui.presentation.utils.themes.LightTheme
import com.pandacorp.noteui.presentation.utils.themes.PurpleTheme
import java.util.Locale
import com.pandacorp.noteui.presentation.utils.themes.Theme as CurrentTheme

object PreferenceHandler {
    private object Theme {
        const val FOLLOW_SYSTEM = "follow_system"
        const val BLUE = "blue"
        const val DARK = "dark"
        const val PURPLE = "purple"
        const val LIGHT = "light"
        const val DEFAULT = FOLLOW_SYSTEM
    }

    private fun isDeviceDarkMode(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    fun getTheme(context: Context): CurrentTheme {
        val theme =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Constants.Preferences.Key.THEME, Theme.DEFAULT)!!
        return getThemeByKey(context, theme)
    }

    fun getThemeByKey(
        context: Context,
        key: String
    ): CurrentTheme {
        return when (key) {
            Theme.FOLLOW_SYSTEM -> {
                if (isDeviceDarkMode(context)) {
                    DarkTheme()
                } else {
                    BlueTheme()
                }
            }

            Theme.BLUE -> BlueTheme()
            Theme.DARK -> DarkTheme()
            Theme.PURPLE -> PurpleTheme()
            Theme.LIGHT -> LightTheme()
            else -> throw IllegalArgumentException("Theme not found: $key")
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

    fun getThemeBackground(context: Context): Drawable {
        val drawable =
            when (getThemeKey(context)) {
                Theme.FOLLOW_SYSTEM -> {
                    if (isDeviceDarkMode(context)) {
                        ContextCompat.getDrawable(context, R.drawable.dark_theme_background)
                    } else {
                        ContextCompat.getDrawable(context, R.drawable.blue_theme_background)
                    }
                }

                Theme.BLUE -> ContextCompat.getDrawable(context, R.drawable.blue_theme_background)
                Theme.DARK -> ContextCompat.getDrawable(context, R.drawable.dark_theme_background)
                Theme.PURPLE -> ContextCompat.getDrawable(context, R.drawable.purple_theme_background)
                Theme.LIGHT -> ContextCompat.getDrawable(context, R.drawable.light_theme_background)
                else -> {
                    val tv = TypedValue()
                    context.theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
                    ColorDrawable(tv.data)
                }
            }
        return drawable!!
    }

    private fun getThemeKey(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(Constants.Preferences.Key.THEME, Theme.DEFAULT)!!
    }

    fun isShowThemeBackground(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            Constants.Preferences.Key.SHOW_THEME_BACKGROUND,
            Constants.Preferences.DefaultValue.SHOW_THEME_BACKGROUND,
        )
    }

    fun setShowThemeBackground(
        context: Context,
        value: Boolean
    ) {
        val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
        editor.putBoolean(Constants.Preferences.Key.SHOW_THEME_BACKGROUND, value)
        editor.apply()
    }
}