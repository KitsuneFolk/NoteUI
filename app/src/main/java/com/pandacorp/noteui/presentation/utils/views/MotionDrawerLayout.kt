package com.pandacorp.noteui.presentation.utils.views

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.pandacorp.noteui.presentation.utils.helpers.getParcelableExtraSupport

class MotionDrawerLayout : MotionLayout, DrawerLayout.DrawerListener {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    companion object {
        private const val PROGRESS = "PROGRESS"
    }

    private var editText: EditText? = null
    private var maxValue: Int? = null

    override fun onDrawerStateChanged(newState: Int) {}

    override fun onDrawerSlide(
        drawerView: View,
        slideOffset: Float
    ) {
        if (isDisableAnimation()) return
        progress = slideOffset
    }

    override fun onDrawerClosed(drawerView: View) {}

    override fun onDrawerOpened(drawerView: View) {}

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? DrawerLayout)?.addDrawerListener(this)
    }

    /**
     * Attach EditText to observe the characters limit
     * @param maxValue - the maximum value when animation will work
     */
    fun attachEditText(
        editText: EditText,
        maxValue: Int
    ) {
        this.editText = editText
        this.maxValue = maxValue
    }

    /**
     * Calculates if to disable motion layout animation,
     * @return true if maxValue is empty or editText.text.length > maxValue, false if editText.text.length < maxvalue
     */
    private fun isDisableAnimation(): Boolean {
        if (editText != null && maxValue != null) {
            if (editText!!.text.length > maxValue!!) return true
        } else {
            return true
        }
        return false
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        val bundle =
            Bundle().apply {
                putFloat(PROGRESS, progress)
            }
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val bundle = state as Bundle
        onDrawerSlide(View(context), bundle.getFloat(PROGRESS))
        super.onRestoreInstanceState(bundle.getParcelableExtraSupport("superState", Parcelable::class.java))
    }
}