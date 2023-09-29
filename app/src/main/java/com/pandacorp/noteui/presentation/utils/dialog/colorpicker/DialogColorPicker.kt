package com.pandacorp.noteui.presentation.utils.dialog.colorpicker

import android.content.Context
import android.os.Bundle
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.DialogColorPickerBinding
import com.pandacorp.noteui.presentation.utils.dialog.CustomDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager

class DialogColorPicker(private val context: Context) : CustomDialog(context) {
    private var _binding: DialogColorPickerBinding? = null
    private val binding get() = _binding!!

    private var colorEnvelopeListener: ColorEnvelopeListener? = null

    private fun initViews() {
        binding.title.setText(R.string.addColor)

        binding.cancel.setOnClickListener {
            cancel()
        }

        binding.ok.setOnClickListener {
            cancel()
            colorEnvelopeListener?.onColorSelected(binding.colorPicker.colorEnvelope, true)
        }

        binding.colorPicker.flagView = CustomBubbleFlag(context)

        restoreInstanceState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogColorPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    fun setOnPositiveButtonClick(colorEnvelopeListener: ColorEnvelopeListener) {
        this.colorEnvelopeListener = colorEnvelopeListener
    }

    override fun onSaveInstanceState(): Bundle {
        ColorPickerPreferenceManager.getInstance(context).saveColorPickerData(binding.colorPicker)
        return super.onSaveInstanceState()
    }

    private fun restoreInstanceState() {
        ColorPickerPreferenceManager.getInstance(context).restoreColorPickerData(binding.colorPicker)
    }
}