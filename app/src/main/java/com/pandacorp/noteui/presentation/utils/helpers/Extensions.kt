package com.pandacorp.noteui.presentation.utils.helpers

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.AppBarLayout
import com.pandacorp.noteui.presentation.di.app.App

val Fragment.sp: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(requireContext())

val Fragment.app get() = (requireActivity().application as App)

fun Toolbar.hideToolbarWhileScrolling(isHide: Boolean) {
    val layoutParams = layoutParams as AppBarLayout.LayoutParams
    if (isHide) {
        layoutParams.scrollFlags =
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
    } else {
        layoutParams.scrollFlags = 0
    }
    this.layoutParams = layoutParams
}

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

/**
 * A compatibility wrapper around PackageManager's `getPackageInfo()` method that allows
 * developers to use either the old flag-based API or the new enum-based API depending on the
 * version of Android running on the device.
 *
 * @param packageName The name of the package for which to retrieve package information.
 * @param flags Additional flags to control the behavior of the method.
 * @return A PackageInfo object containing information about the specified package.
 */
fun PackageManager.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
    } else {
        getPackageInfo(packageName, flags)
    }

/**
 * A compatibility wrapper around Bundle's `getParcelableExtra()` method that allows
 * developers to get a Parcelable extra from an Bundle object regardless of the version of
 * Android running on the device.
 *
 * @param name The name of the extra to retrieve.
 * @param clazz The class of the extra to retrieve.
 * @return The Parcelable extra with the specified name and class, or null if it does not exist.
 */
inline fun <reified T : Parcelable> Bundle.getParcelableExtraSupport(name: String, clazz: Class<T>): T? {
    val extra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(name, clazz)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(name)
            as? T
    }
    if (extra is T) return extra
    return null
}