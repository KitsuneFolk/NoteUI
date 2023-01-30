package com.pandacorp.notesui.utils.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.DialogNumberPickerEdittextBinding
import com.pandacorp.notesui.presentation.activities.SettingsActivity
import com.pandacorp.notesui.utils.Constans

class DialogNumberPickerEditText : CustomDialog() {
    private lateinit var binding: DialogNumberPickerEdittextBinding
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        
        binding = DialogNumberPickerEdittextBinding.inflate(inflater)
        
        val preferenceKey =
            requireArguments().getString(Constans.PreferencesKeys.preferenceBundleKey)
        
        val preferenceValue =
            sp.getString(
                    preferenceKey, when (preferenceKey) {
                Constans.PreferencesKeys.disableDrawerAnimationKey -> Constans.PreferencesKeys.disableDrawerAnimationDV
                else -> throw IllegalArgumentException("preferenceKey = $preferenceKey")
            })!!
        val valuesList = fillDrawerMenuValuesArray()
        
        restoreData(preferenceValue)
        
        binding.dialogNumberPickerETTitle.setText(
                when (preferenceKey) {
                    Constans.PreferencesKeys.disableDrawerAnimationKey -> R.string.preferencesDisableDrawerMenuAnimation
                    else -> throw IllegalArgumentException()
                })
        binding.dialogNumberPickerETCancel.setOnClickListener {
            dialog!!.cancel()
        }
        
        binding.dialogNumberPickerETOk.setOnClickListener {
            val value = binding.dialogNumberPickerETEdittext.text.toString()
            sp.edit().putString(preferenceKey, value).apply()
            dialog!!.cancel()
            requireActivity().setResult(AppCompatActivity.RESULT_OK)
            requireActivity().startActivity(Intent(context, SettingsActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
        }
        
        binding.dialogNumberPickerETNumberPicker.apply {
            displayedValues = valuesList
            minValue = 0
            maxValue = valuesList.size - 1
            val valueIndex: Int = valuesList.indexOf(preferenceValue)
            value = valueIndex
            wrapSelectorWheel = false
            
        }
        binding.dialogNumberPickerETNumberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            vibrate()
            
            binding.dialogNumberPickerETEdittext.setText(valuesList[newVal])
            
        }
        
        
        return binding.root
    }
    
    private fun fillDrawerMenuValuesArray(): Array<String> =
        requireContext().resources.getStringArray(R.array.DisableMenuAnimation_values)
    
    private fun restoreData(preferenceValue: String) {
        // if preference value was set in edittext then restore it in edittext
        binding.dialogNumberPickerETEdittext.setText(preferenceValue)
    }
    
    companion object {
        fun newInstance(preferenceKey: String): DialogNumberPickerEditText {
            val args = Bundle()
            val dialog = DialogNumberPickerEditText()
            args.putString(Constans.PreferencesKeys.preferenceBundleKey, preferenceKey)
            dialog.arguments = args
            return dialog
        }
    }
    
}