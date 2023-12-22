package com.pandacorp.noteui.presentation.utils.dialog.colorpicker

import android.content.Context
import android.os.Bundle
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.DialogColorPickerBinding
import com.pandacorp.noteui.presentation.utils.dialog.CustomDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager

class DialogColorPicker(private val context: Context) : CustomDialog(context) {
    private lateinit var binding: DialogColorPickerBinding

    private var colorEnvelopeListener: ColorEnvelopeListener? = null

    fun setOnColorSelect(colorEnvelopeListener: ColorEnvelopeListener) {
        this.colorEnvelopeListener = colorEnvelopeListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogColorPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    override fun onSaveInstanceState(): Bundle {
        ColorPickerPreferenceManager.getInstance(context).saveColorPickerData(binding.colorPicker)
        return super.onSaveInstanceState()
    }

    private fun initViews() {
        binding.title.setText(R.string.addColor)

        binding.ok.setOnClickListener {
            cancel()
            colorEnvelopeListener?.onColorSelected(binding.colorPicker.colorEnvelope, true)
        }

        binding.colorPicker.flagView = CustomBubbleFlag(context)

        restoreInstanceState()
    }

    private fun restoreInstanceState() {
        ColorPickerPreferenceManager.getInstance(context).restoreColorPickerData(binding.colorPicker)
    }
}