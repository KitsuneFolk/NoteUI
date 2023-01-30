package com.pandacorp.notesui.utils.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.DialogNumberPickerBinding
import com.pandacorp.notesui.presentation.activities.SettingsActivity
import com.pandacorp.notesui.utils.Constans

open class DialogNumberPicker : CustomDialog() {
    private lateinit var binding: DialogNumberPickerBinding
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DialogNumberPickerBinding.inflate(inflater)
        
        val args = requireArguments()
        val preferenceKey = args.getString(Constans.PreferencesKeys.preferenceBundleKey)
        Log.d(TAG, "onCreateView: preferenceKey = $preferenceKey")
        
        val defaultValue = when (preferenceKey) {
            Constans.PreferencesKeys.contentTextSizeKey -> Constans.PreferencesKeys.contentTextSizeDV
            Constans.PreferencesKeys.headerTextSizeKey -> {
                Log.d(TAG, "headerTextSizeDV = ${Constans.PreferencesKeys.headerTextSizeDV}")
                Constans.PreferencesKeys.headerTextSizeDV
            }
            else -> throw IllegalArgumentException("preferenceKey = $preferenceKey")
        }
        val preferenceValue =
            sp.getString(preferenceKey, defaultValue)!!
        
        Log.d(TAG, "onCreateView: preferenceValue = $preferenceValue")
        
        val textSizesList = fillContentTextSizesList()
        
        binding.dialogNumberPickerTitle.setText(
                when (preferenceKey) {
                    Constans.PreferencesKeys.contentTextSizeKey -> R.string.contentTextSize
                    Constans.PreferencesKeys.headerTextSizeKey -> R.string.headerTextSize
                    else -> throw IllegalArgumentException()
                })
        binding.dialogNumberPickerSampleTextView.textSize = preferenceValue.toFloat()
        
        binding.dialogNumberPickerCancel.setOnClickListener {
            dialog!!.cancel()
        }
        
        binding.dialogNumberPickerOk.setOnClickListener {
            val value = textSizesList[binding.dialogNumberPickerNumberPicker.value]
            sp.edit().putString(preferenceKey, value).apply()
            dialog!!.cancel()
            requireActivity().setResult(AppCompatActivity.RESULT_OK)
            requireActivity().startActivity(Intent(context, SettingsActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
        }
        
        binding.dialogNumberPickerNumberPicker.apply {
            displayedValues = textSizesList
            minValue = 0
            maxValue = textSizesList.size - 1
            val valueIndex: Int = textSizesList.indexOf(preferenceValue)
            value = valueIndex
            wrapSelectorWheel = false
            
        }
        binding.dialogNumberPickerNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            vibrate()
            
            binding.dialogNumberPickerSampleTextView.textSize = textSizesList[newVal].toFloat()
        }
        
        
        return binding.root
    }
    
    private fun fillContentTextSizesList(): Array<String> =
        requireContext().resources.getStringArray(R.array.TextSizes_values)
    
    companion object {
        fun newInstance(preferenceKey: String): DialogNumberPicker {
            val args = Bundle()
            val dialog = DialogNumberPicker()
            args.putString(Constans.PreferencesKeys.preferenceBundleKey, preferenceKey)
            dialog.arguments = args
            return dialog
        }
    }
    
}