package com.pandacorp.searchbar

import android.animation.Animator
import android.animation.ValueAnimator
import android.graphics.drawable.Drawable
import android.view.View

/**
 * Animates the alpha (transparency) property of a Drawable from one value to another over a specified duration.
 *
 * @param fromAlpha The starting alpha value (0 for fully transparent, 255 for fully opaque).
 * @param toAlpha The ending alpha value (0 for fully transparent, 255 for fully opaque).
 * @param duration The duration of the animation in milliseconds.
 * @param onAnimationEnd A callback function to be invoked when the animation ends (optional).
 *
 * @throws IllegalArgumentException if [fromAlpha] or [toAlpha] is not within the range [0, 255].
 */
fun Drawable.animateAlpha(fromAlpha: Int, toAlpha: Int, duration: Long, onAnimationEnd: (() -> Unit)? = null) {
    val animator = ValueAnimator.ofInt(fromAlpha, toAlpha).apply {
        this.duration = duration
        addUpdateListener {
            alpha = it.animatedValue as Int
        }
        onAnimationEnd?.let {
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    it.invoke()
                }
            })
        }
    }
    animator.start()
}

/**
 * Animates the alpha (transparency) property of a View from one value to another over a specified duration.
 *
 * @param isAnimating Indicates whether the animation should be performed, if the value is false
 * then toAlpha is assigned immediately and onAnimationEnd called.
 * @param fromAlpha The starting alpha value (0.0f for transparent, 1.0f for opaque).
 * @param toAlpha The ending alpha value (0.0f for transparent, 1.0f for opaque).
 * @param duration The duration of the animation in milliseconds.
 * @param onAnimationEnd A callback function to be invoked when the animation ends (optional).
 *
 * @throws IllegalArgumentException if [fromAlpha] or [toAlpha] is not within the range [0.0f, 1.0f].
 */
fun View.animateAlpha(
    isAnimating: Boolean = true,
    fromAlpha: Float,
    toAlpha: Float,
    duration: Long,
    onAnimationEnd: (() -> Unit)? = null,
) {
    if (isAnimating) {
        val animator = ValueAnimator.ofFloat(fromAlpha, toAlpha).apply {
            this.duration = duration
            addUpdateListener {
                alpha = it.animatedValue as Float
            }
            onAnimationEnd?.let {
                addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                    override fun onAnimationEnd(animation: Animator) {
                        it.invoke()
                    }
                })
            }
        }
        animator.start()
    } else {
        onAnimationEnd?.invoke()
    }
}