package com.pandacorp.noteui.presentation.utils.themes

import android.content.Context
import androidx.core.content.ContextCompat
import com.pandacorp.noteui.app.R

class DarkTheme : Theme {
    override fun id(): Int = 1

    override val usesLightColors = false

    override fun getTextColorSecondary(context: Context): Int {
        return ContextCompat.getColor(context, R.color.DarkTheme_textColorSecondary)
    }

    override fun getColorBackground(context: Context): Int {
        return ContextCompat.getColor(context, R.color.DarkTheme_colorBackground)
    }

    override fun getColorPrimary(context: Context): Int {
        return ContextCompat.getColor(context, R.color.DarkTheme_colorPrimary)
    }

    override fun getColorPrimaryDark(context: Context): Int {
        return ContextCompat.getColor(context, R.color.DarkTheme_colorPrimaryDark)
    }

    override fun getColorAccent(context: Context): Int {
        return ContextCompat.getColor(context, R.color.DarkTheme_colorAccent)
    }
}