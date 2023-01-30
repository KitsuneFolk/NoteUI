package com.pandacorp.notesui.utils

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.drawerlayout.widget.DrawerLayout

class MotionDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    MotionLayout(context, attrs, defStyleAttr), DrawerLayout.DrawerListener {
    private val TAG = "Utils"
    
    private var editText: EditText? = null
    private var maxValue: String? = null
    
    override fun onDrawerStateChanged(newState: Int) {
    
    }
    
    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        if (isDisableAnimation()) return
        
        progress = slideOffset
        
    }
    
    override fun onDrawerClosed(drawerView: View) {
    
    }
    
    override fun onDrawerOpened(drawerView: View) {
    
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? DrawerLayout)?.addDrawerListener(this)
    }
    
    /**
     * This function adds edittext to disable animation if editText length > maxValue, to remove lags
     * @param maxValue - the maximum value when animation will work
     */
    fun attachEditText(editText: EditText, maxValue: String) {
        this.editText = editText
        this.maxValue = maxValue.filter { !it.isWhitespace() }
    }
    
    /**
     * This function calculates is disable motion layout animation,
     * @return true if maxValue is empty or editText.text.length > maxValue, false if editText.text.length < maxvalue
     */
    private fun isDisableAnimation(): Boolean {
        if (editText != null && maxValue != null) {
            if (maxValue!!.isEmpty()) return false
            if (editText!!.text.length > maxValue!!.toInt()) return true
            
        }
        return false
    }
}