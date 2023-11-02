package com.pandacorp.noteui.presentation.utils.themes

import android.view.ViewGroup
import androidx.core.view.children
import com.dolatkia.animatedThemeManager.AppTheme

object ViewHelper {
    lateinit var currentTheme: AppTheme

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

        for (view in views) {
            if (view is ViewGroup) {
                view.setBackgroundColor(newTheme.getColorBackground(context))
                applyTheme(newTheme, view)
            }

            if (view is androidx.appcompat.widget.Toolbar) {
                view.setBackgroundColor(newTheme.getToolbarColor(context))
                view.setTitleTextColor(newTheme.getTextColor())
            }
        }
    }
}