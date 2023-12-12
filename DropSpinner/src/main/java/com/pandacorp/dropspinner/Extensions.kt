package com.pandacorp.dropspinner

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.ListView

fun ListView.setHeightBasedOnChildren() {
    val listAdapter = adapter ?: return
    var totalHeight = 0
    val desiredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.UNSPECIFIED)
    val maxCount = if (listAdapter.count > 4) 4 else listAdapter.count
    for (i in 0 until maxCount) {
        val listItem = listAdapter.getView(i, null, this)
        listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
        totalHeight += listItem.measuredHeight
    }
    val params = layoutParams
    params.height = totalHeight + dividerHeight * (listAdapter.count - 1)
    layoutParams = params
    requestLayout()
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
    val extra =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(name, clazz)
        } else {
            @Suppress("DEPRECATION")
            getParcelable(name)
                as? T
        }
    if (extra is T) return extra
    return null
}