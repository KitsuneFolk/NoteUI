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
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.preference.PreferenceManager

abstract class CustomDialog(context: Context) : Dialog(context) {
    companion object {
        private const val VIBRATION_DURATION = 100L
    }
    protected val sp: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }

    @JvmField protected var onValueAppliedListener: (value: String) -> Unit = {}

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // remove the default background so that dialog can be rounded
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // remove the shadow
        }
    }

    protected fun vibrate() {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrate(VibrationEffect.createOneShot(VIBRATION_DURATION, 15))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(VIBRATION_DURATION)
        }
    }

    fun setOnValueAppliedListener(listener: (value: String) -> Unit) {
        onValueAppliedListener = listener
    }
}