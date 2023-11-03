package com.pandacorp.noteui.presentation.utils.dialog

import android.app.Dialog
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.core.view.children
import androidx.preference.PreferenceManager
import com.pandacorp.noteui.presentation.utils.themes.ViewHelper

abstract class CustomDialog(context: Context) : Dialog(context) {
    companion object {
        private const val VIBRATION_DURATION = 50L
    }

    protected val sp: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    @JvmField
    protected var onValueAppliedListener: (value: String) -> Unit = {}

    fun setOnValueAppliedListener(listener: (value: String) -> Unit) {
        onValueAppliedListener = listener
    }

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // remove the default background so that dialog can be rounded
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // remove the shadow
        }
    }

    private fun syncTheme() {
        val appTheme = ViewHelper.currentTheme
        val decorView = window!!.decorView as ViewGroup
        ViewHelper.applyTheme(newTheme = appTheme, viewGroup = decorView.children.first() as ViewGroup)
    }

    override fun show() {
        super.show()
        syncTheme()
    }

    protected fun vibrate() {
        val vib =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, 20))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(VIBRATION_DURATION)
        }
    }
}