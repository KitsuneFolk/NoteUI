package com.pandacorp.notesui.presentation.settings.dialog

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.DialogNumberPickerBinding
import com.pandacorp.notesui.presentation.settings.SettingsActivity
import com.pandacorp.notesui.utils.Constans

class DialogNumberPicker : CustomDialog() {
    private val TAG = SettingsActivity.TAG
    private lateinit var sp: SharedPreferences
    private lateinit var vibrator: Vibrator
    
    private lateinit var binding: DialogNumberPickerBinding
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        binding = DialogNumberPickerBinding.inflate(inflater)
        
        val preferenceKey =
            requireArguments().getString(Constans.PreferencesKeys.preferenceBundleKey)
        
        val preferenceValue =
            sp.getString(
                    preferenceKey, when (preferenceKey) {
                Constans.PreferencesKeys.contentTextSizeKey -> Constans.PreferencesKeys.contentTextSizeDefaultValue
                Constans.PreferencesKeys.headerTextSizeKey -> Constans.PreferencesKeys.headerTextSizeDefaultValue
                else -> throw IllegalArgumentException("preferenceKey = $preferenceKey")
            })!!
        
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
            val amplitude = 1
            vibrate(amplitude)
            
            binding.dialogNumberPickerSampleTextView.textSize = textSizesList[newVal].toFloat()
        }
        
        
        return binding.root
    }
    
    private fun fillContentTextSizesList(): Array<String> =
        requireContext().resources.getStringArray(R.array.TextSizes_values)
    
    private fun vibrate(amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, amplitude))
        }
    }
    
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