package com.pandacorp.noteui.presentation.utils.themes

import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import com.dolatkia.animatedThemeManager.AppTheme
import com.google.android.material.card.MaterialCardView

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
        val themesColorSurface = themes.map { it.getColorSurface(context) }
        val themesColorPrimary = themes.map { it.getColorPrimary(context) }
        val themesColorPrimaryDark = themes.map { it.getColorPrimaryDark(context) }
        val themesTextColor = themes.map { it.getTextColor() }
        val themesTextColorSecondary = themes.map { it.getTextColorSecondary(context) }

        for (view in views) {
            if (view is ViewGroup) {
                applyTheme(newTheme, view)
                if (view.background is ColorDrawable) {
                    view.setBackgroundColor(newTheme.getColorBackground(context))
                }
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

            if (view is MaterialCardView) {
                when (view.cardBackgroundColor.defaultColor) {
                    in themesColorSurface -> {
                        view.setCardBackgroundColor(newTheme.getColorSurface(context))
                    }

                    in themesColorPrimary -> {
                        view.setCardBackgroundColor(newTheme.getColorPrimary(context))
                    }

                    in themesColorPrimaryDark -> {
                        view.setCardBackgroundColor(newTheme.getColorPrimaryDark(context))
                    }
                }
                view.strokeColor = newTheme.getStrokeColor(context)
            }
        }
    }
}