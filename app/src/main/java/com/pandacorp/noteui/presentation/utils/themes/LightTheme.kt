package com.pandacorp.noteui.presentation.utils.themes

import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.pandacorp.noteui.app.R

class LightTheme : Theme {
    override fun id(): Int = 4

    override val usesLightColors = true

    override fun getTextColor(): Int {
        return Color.BLACK
    }

    override fun getTextColorSecondary(context: Context): Int {
        return ContextCompat.getColor(context, R.color.LightTheme_textColorSecondary)
    }

    override fun getColorBackground(context: Context): Int {
        return ContextCompat.getColor(context, R.color.LightTheme_colorBackground)
    }

    override fun getColorPrimary(context: Context): Int {
        return ContextCompat.getColor(context, R.color.LightTheme_colorPrimary)
    }

    override fun getColorPrimaryDark(context: Context): Int {
        return ContextCompat.getColor(context, R.color.LightTheme_colorPrimaryDark)
    }

    override fun getColorAccent(context: Context): Int {
        return ContextCompat.getColor(context, R.color.LightTheme_colorAccent)
    }

    override fun getColorSurface(context: Context): Int {
        return ContextCompat.getColor(context, R.color.dark_white)
    }

    override fun getToolbarColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.dark_white)
    }

    override fun getStrokeColor(context: Context): Int {
        return Color.BLACK
    }
}