package com.pandacorp.noteui.presentation.utils.themes

import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.dolatkia.animatedThemeManager.AppTheme

object ViewHelper {
    lateinit var currentTheme: AppTheme
    private val themes = mutableListOf(BlueTheme(), DarkTheme(), LightTheme(), PurpleTheme())

    fun applyTheme(
        newTheme: AppTheme,
        viewGroup: ViewGroup
    ) {
        applyTheme(newTheme as Theme, viewGroup)
    }

    private fun applyTheme(
        newTheme: Theme,
        viewGroup: ViewGroup
    ) {
        val context = viewGroup.context
        val views = viewGroup.children
        val themesTextColor = themes.map { it.getTextColor() }
        val themesTextColorSecondary = themes.map { it.getTextColorSecondary(context) }

        for (view in views) {
            if (view is ViewGroup) {
                applyTheme(newTheme, view)
                view.setBackgroundColor(newTheme.getColorBackground(context))
            }

            if (view is TextView) {
                when (view.currentTextColor) {
                    in themesTextColor -> {
                        view.setTextColor(newTheme.getTextColor())
                    }

                    in themesTextColorSecondary -> {
                        view.setTextColor(newTheme.getTextColorSecondary(context))
                    }
                }
            }

            if (view is androidx.appcompat.widget.Toolbar) {
                view.setBackgroundColor(newTheme.getToolbarColor(context))
                view.setTitleTextColor(newTheme.getTextColor())
            }
        }
    }
}