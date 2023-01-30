package com.pandacorp.notesui.utils.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.DialogColorPickerBinding
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager

class DialogColorPicker : CustomDialog() {
    private lateinit var binding: DialogColorPickerBinding
    private var colorEnvelopeListener: ColorEnvelopeListener? = null
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // This needed to not recreate fragment after screen rotation, so envelopeListener won't be null
        binding = DialogColorPickerBinding.inflate(layoutInflater)
        
        binding.dialogColorPickerTitle.setText(R.string.alert_dialog_add_color)
        
        binding.dialogColorPickerCancel.setOnClickListener {
            dialog!!.cancel()
        }
        
        binding.dialogColorPickerOk.setOnClickListener {
            dialog!!.cancel()
            colorEnvelopeListener?.onColorSelected(
                    binding.dialogColorPickerColorPicker.colorEnvelope,
                    true)
        }
        binding.dialogColorPickerColorPicker.flagView = BubbleFlag(context)
        // regain selector position
        ColorPickerPreferenceManager.getInstance(context).restoreColorPickerData(binding.dialogColorPickerColorPicker)
        return binding.root
    }
    
    fun setOnPositiveButtonClick(colorEnvelopeListener: ColorEnvelopeListener) {
        this.colorEnvelopeListener = colorEnvelopeListener
        
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        // save color picker position
        ColorPickerPreferenceManager.getInstance(context).saveColorPickerData(binding.dialogColorPickerColorPicker)
        super.onSaveInstanceState(outState)
    }
    
    companion object {
        fun newInstance(): DialogColorPicker {
            val args = Bundle()
            val dialog = DialogColorPicker()
            dialog.arguments = args
            return dialog
        }
        
    }
    
}