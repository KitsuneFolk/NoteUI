package com.pandacorp.noteui.presentation.utils.themes

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.dolatkia.animatedThemeManager.AppTheme

interface Theme : AppTheme {
    val usesLightColors: Boolean

    fun getTextColor(): Int = Color.WHITE

    fun getTextColorSecondary(context: Context): Int

    fun getColorBackground(context: Context): Int

    fun getColorPrimary(context: Context): Int

    fun getColorPrimaryDark(context: Context): Int

    fun getColorAccent(context: Context): Int

    fun getColorSurface(context: Context): Int {
        return getColorPrimaryDark(context)
    }

    fun getToolbarColor(context: Context): Int {
        return getColorPrimary(context)
    }

    fun getStrokeColor(context: Context): Int {
        return ContextCompat.getColor(context, android.R.color.darker_gray)
    }

    fun changeStatusbarColor(activity: Activity) {
        activity.window.statusBarColor = getColorPrimary(activity)
    }

    fun changeNavigationBarColor(activity: Activity) {
        val window = activity.window
        window.navigationBarColor = getColorPrimary(activity)
    }
}