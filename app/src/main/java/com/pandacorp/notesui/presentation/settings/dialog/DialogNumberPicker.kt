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
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.pandacorp.notesui.R
import com.pandacorp.notesui.presentation.settings.SettingsActivity
import com.pandacorp.notesui.utils.Constans

class DialogNumberPicker : CustomDialog() {
    private val TAG = SettingsActivity.TAG
    private lateinit var sp: SharedPreferences
    private lateinit var vibrator: Vibrator
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        val view = inflater.inflate(R.layout.dialog_number_picker, container, false)
    
        val preferenceKey = requireArguments().getString(Constans.PreferencesKeys.preferenceBundleKey)
    
        val title = view.findViewById<TextView>(R.id.dialogNumberPicker_title)
        val okButton = view.findViewById<Button>(R.id.dialogNumberPicker_ok)
        val cancelButton = view.findViewById<Button>(R.id.dialogNumberPicker_cancel)
        val numberPicker =
            view.findViewById<NumberPicker>(R.id.dialogNumberPicker_numberPicker)
        val sampleTextView = view.findViewById<TextView>(R.id.dialogNumberPicker_sampleTextView)
        
        val preferenceValue =
            sp.getString(preferenceKey, when (preferenceKey) {
                Constans.PreferencesKeys.contentTextSizeKey -> Constans.PreferencesKeys.contentTextSizeDefaultValue
                Constans.PreferencesKeys.headerTextSizeKey -> Constans.PreferencesKeys.headerTextSizeDefaultValue
                else -> throw IllegalArgumentException("preferenceKey = $preferenceKey")
            })!!
        
        val textSizesList = fillContentTextSizesList()
        
        title.setText(
                when (preferenceKey) {
                    Constans.PreferencesKeys.contentTextSizeKey -> R.string.contentTextSize
                    Constans.PreferencesKeys.headerTextSizeKey -> R.string.headerTextSize
                    else -> throw IllegalArgumentException()
                })
        sampleTextView.textSize = preferenceValue.toFloat()
        
        cancelButton.setOnClickListener {
            dialog!!.cancel()
        }
        
        okButton.setOnClickListener {
            val value = textSizesList[numberPicker.value]
            sp.edit().putString(preferenceKey, value).apply()
            dialog!!.cancel()
            requireActivity().setResult(AppCompatActivity.RESULT_OK)
            requireActivity().startActivity(Intent(context, SettingsActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
        }
        
        numberPicker.apply {
            displayedValues = textSizesList
            minValue = 0
            maxValue = textSizesList.size - 1
            val valueIndex: Int = textSizesList.indexOf(preferenceValue)
            value = valueIndex
            wrapSelectorWheel = false
            
        }
        numberPicker.setOnValueChangedListener { picker, oldVal, newVal ->
            val amplitude = 5
            vibrate(amplitude)
            
            sampleTextView.textSize = textSizesList[newVal].toFloat()
        }
        
        
        return view
    }
    
    private fun fillContentTextSizesList(): Array<String> =
        requireContext().resources.getStringArray(R.array.TextSizes_values)
    
    private fun vibrate(amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, amplitude))
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