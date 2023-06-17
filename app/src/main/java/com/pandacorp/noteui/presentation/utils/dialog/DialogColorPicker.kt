package com.pandacorp.noteui.presentation.utils.dialog

import android.app.Activity
import android.os.Bundle
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.DialogColorPickerBinding
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager

class DialogColorPicker(private val activity: Activity) : CustomDialog(activity) {
    private var _binding: DialogColorPickerBinding? = null
    private val binding get() = _binding!!

    private var colorEnvelopeListener: ColorEnvelopeListener? = null
    private fun initViews() {
        binding.dialogColorPickerTitle.setText(R.string.alert_dialog_add_color)

        binding.dialogColorPickerCancel.setOnClickListener {
            cancel()
        }

        binding.dialogColorPickerOk.setOnClickListener {
            cancel()
            colorEnvelopeListener?.onColorSelected(binding.dialogColorPickerColorPicker.colorEnvelope, true)
        }
        binding.dialogColorPickerColorPicker.flagView = BubbleFlag(context)

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
        ColorPickerPreferenceManager.getInstance(activity).saveColorPickerData(binding.dialogColorPickerColorPicker)
        return super.onSaveInstanceState()
    }

    private fun restoreInstanceState() {
        ColorPickerPreferenceManager.getInstance(activity)
            .restoreColorPickerData(binding.dialogColorPickerColorPicker)
    }

}