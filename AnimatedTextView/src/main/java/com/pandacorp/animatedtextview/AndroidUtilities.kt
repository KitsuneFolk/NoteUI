package com.pandacorp.animatedtextview

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.ceil

object AndroidUtilities {
    var displaySize = Point()
    private var density = 1f

    fun lerp(
        a: Float,
        b: Float,
        f: Float
    ): Float {
        return a + f * (b - a)
    }

    fun isRTL(text: CharSequence?): Boolean {
        if (text.isNullOrEmpty()) {
            return false
        }
        var c: Char
        for (element in text) {
            c = element
            if (c.code in 0x590..0x6ff) {
                return true
            }
        }
        return false
    }

    fun dp(value: Float): Int {
        return if (value == 0f) {
            0
        } else {
            ceil((density * value).toDouble()).toInt()
        }
    }

    fun checkDisplaySize(
        activity: Activity,
        newConfiguration: Configuration?
    ) {
        try {
            density = activity.resources.displayMetrics.density
            var configuration: Configuration? = newConfiguration
            if (configuration == null) {
                configuration = activity.resources.configuration
            }

            val manager = activity.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
            manager?.defaultDisplay?.getSize(displaySize)
            if (configuration == null) {
                return
            }
            if (configuration.screenWidthDp !== Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
                val newSize = ceil(configuration.screenWidthDp * density.toDouble()).toInt()
                if (abs(displaySize.x - newSize) > 3) {
                    displaySize.x = newSize
                }
            }
            if (configuration.screenHeightDp !== Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
                val newSize = ceil(configuration.screenHeightDp * density.toDouble()).toInt()
                if (abs(displaySize.y - newSize) > 3) {
                    displaySize.y = newSize
                }
            }
        } catch (_: Exception) {
        }
    }
}