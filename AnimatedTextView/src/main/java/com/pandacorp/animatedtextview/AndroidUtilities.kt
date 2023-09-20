package com.pandacorp.animatedtextview

import android.graphics.Point
import kotlin.math.ceil

object AndroidUtilities {
    var displaySize = Point()
    private var density = 1f
    fun lerp(a: Float, b: Float, f: Float): Float {
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
}