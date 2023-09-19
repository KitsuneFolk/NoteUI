package com.pandacorp.noteui.presentation.utils.dialog.colorpicker

import android.content.Context
import android.content.res.ColorStateList
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.widget.ImageViewCompat
import com.pandacorp.noteui.app.R
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.flag.FlagView

class CustomBubbleFlag(context: Context?) : FlagView(context, R.layout.colorpicker_flag) {
    private val bubble: AppCompatImageView = findViewById(R.id.bubble)

    /**
     * invoked when selector is moved.
     *
     * @param colorEnvelope provide hsv color, hexCode, argb
     */
    override fun onRefresh(colorEnvelope: ColorEnvelope) {
        ImageViewCompat.setImageTintList(bubble, ColorStateList.valueOf(colorEnvelope.color))
    }
}