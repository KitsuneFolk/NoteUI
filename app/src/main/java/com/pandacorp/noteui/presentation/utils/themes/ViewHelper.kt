package com.pandacorp.noteui.presentation.utils.themes

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.VectorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import com.dolatkia.animatedThemeManager.AppTheme
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.elevation.ElevationOverlayProvider
import com.pandacorp.numberpicker.NumberPicker
import com.google.android.material.R as materialR

object ViewHelper {
    private var materialThemeColorsThumbTintList: ColorStateList? = null
    private var materialThemeColorsTrackTintList: ColorStateList? = null
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

        if (viewGroup.background is ColorDrawable) {
            viewGroup.setBackgroundColor(newTheme.getColorBackground(context))
        }

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

            if (view is MaterialButton) {
                view.setTextColor(newTheme.getTextColor())
                when (view.backgroundTintList?.defaultColor) {
                    in themesColorSurface -> {
                        view.setBackgroundColor(newTheme.getColorSurface(context))
                    }
                    in themesColorPrimary -> {
                        view.setBackgroundColor(newTheme.getColorPrimary(context))
                    }
                    in themesColorPrimaryDark -> {
                        view.setBackgroundColor(newTheme.getColorPrimaryDark(context))
                    }
                }
                view.strokeColor = ColorStateList.valueOf(newTheme.getStrokeColor(context))
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

            if (view is SwitchCompat) {
                view.thumbTintList = getMaterialThemeColorsThumbTintList(newTheme, view)
                view.trackTintList = getMaterialThemeColorsTrackTintList(newTheme, view)
                when (view.currentTextColor) {
                    in themesTextColor -> {
                        view.setTextColor(newTheme.getTextColor())
                    }
                }
            }

            if (view is ImageView) {
                if (view.drawable is VectorDrawable) {
                    val newDrawable = DrawableCompat.wrap(view.drawable)
                    DrawableCompat.setTint(newDrawable, if (newTheme.usesLightColors) Color.BLACK else Color.WHITE)
                    view.setImageDrawable(newDrawable)
                }
            }

            if (view is NumberPicker) {
                view.textColor = newTheme.getTextColor()
                view.setSelectedTextColor(newTheme.getTextColor())
                view.setDividerColor(newTheme.getTextColor())
            }
        }
    }

    private val ENABLED_CHECKED_STATES =
        arrayOf(
            intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked),
            intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_enabled, android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_enabled, -android.R.attr.state_checked),
        )

    private fun getMaterialThemeColorsThumbTintList(
        newTheme: Theme,
        switch: SwitchCompat
    ): ColorStateList? {
        val elevationOverlayProvider = ElevationOverlayProvider(switch.context)
        val colorSurface = newTheme.getColorSurface(switch.context)
        val colorControlActivated = newTheme.getColorAccent(switch.context)

        @SuppressLint("PrivateResource")
        var thumbElevation: Float = switch.resources.getDimension(materialR.dimen.mtrl_switch_thumb_elevation)
        if (elevationOverlayProvider.isThemeElevationOverlayEnabled) {
            thumbElevation += getParentAbsoluteElevation(switch)
        }
        val colorThumbOff: Int = Color.LTGRAY
        val switchThumbColorsList = IntArray(ENABLED_CHECKED_STATES.size)
        switchThumbColorsList[0] =
            MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL)
        switchThumbColorsList[1] = colorThumbOff
        switchThumbColorsList[2] =
            MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_DISABLED)
        switchThumbColorsList[3] = colorThumbOff
        materialThemeColorsThumbTintList =
            ColorStateList(ENABLED_CHECKED_STATES, switchThumbColorsList)
        return materialThemeColorsThumbTintList
    }

    private fun getMaterialThemeColorsTrackTintList(
        newTheme: Theme,
        switch: SwitchCompat
    ): ColorStateList? {
        val colorSurface = newTheme.getColorSurface(switch.context)
        val colorControlActivated = newTheme.getColorAccent(switch.context)
        val colorOnSurface = newTheme.getTextColor()
        val switchTrackColorsList = IntArray(ENABLED_CHECKED_STATES.size)

        switchTrackColorsList[0] =
            MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_MEDIUM)
        switchTrackColorsList[1] = MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_LOW)
        switchTrackColorsList[2] =
            MaterialColors.layer(
                colorSurface, colorControlActivated, MaterialColors.ALPHA_DISABLED_LOW,
            )
        switchTrackColorsList[3] =
            MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED_LOW)
        materialThemeColorsTrackTintList =
            ColorStateList(ENABLED_CHECKED_STATES, switchTrackColorsList)
        return materialThemeColorsTrackTintList
    }

    private fun getParentAbsoluteElevation(view: View): Float {
        var absoluteElevation = 0f
        var viewParent = view.parent
        while (viewParent is View) {
            absoluteElevation += ViewCompat.getElevation((viewParent as View))
            viewParent = viewParent.getParent()
        }
        return absoluteElevation
    }
}