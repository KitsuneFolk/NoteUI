package com.pandacorp.noteui.presentation.utils.themes

import android.content.Context
import androidx.core.content.ContextCompat
import com.pandacorp.noteui.app.R

class PurpleTheme : Theme {
    override fun id(): Int = 3

    override val usesLightColors = false

    override fun getTextColorSecondary(context: Context): Int {
        return ContextCompat.getColor(context, R.color.PurpleTheme_textColorSecondary)
    }

    override fun getColorBackground(context: Context): Int {
        return ContextCompat.getColor(context, R.color.PurpleTheme_colorBackground)
    }

    override fun getColorPrimary(context: Context): Int {
        return ContextCompat.getColor(context, R.color.PurpleTheme_colorPrimary)
    }

    override fun getColorPrimaryDark(context: Context): Int {
        return ContextCompat.getColor(context, R.color.PurpleTheme_colorPrimaryDark)
    }

    override fun getColorAccent(context: Context): Int {
        return ContextCompat.getColor(context, R.color.PurpleTheme_colorAccent)
    }

    override fun getStrokeColor(context: Context): Int {
        return ContextCompat.getColor(context, android.R.color.darker_gray)
    }
}