package com.pandacorp.splashscreen

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.Drawable

/**
 * A wrapper around a `drawable` that clip it to fit in a circle of diameter `maskDiameter`.
 * @param drawable The drawable to clip
 * @param maskDiameter The diameter of the mask used to clip the drawable.
 */
class MaskedDrawable(
    private val drawable: Drawable,
    private val maskDiameter: Float
) : Drawable() {
    private val mask = Path().apply {
        val radius = maskDiameter / 2f
        addCircle(0f, 0f, radius, Path.Direction.CW)
    }

    override fun draw(canvas: Canvas) {
        canvas.clipPath(mask)
        drawable.draw(canvas)
    }

    override fun setAlpha(alpha: Int) {
        drawable.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        drawable.colorFilter = colorFilter
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun getOpacity() = drawable.opacity

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        drawable.bounds = bounds
        mask.offset(bounds.exactCenterX(), bounds.exactCenterY())
    }
}