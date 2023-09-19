package com.pandacorp.noteui.presentation.utils.dialog

import android.content.Context
import android.os.Bundle
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.DialogNumberPickerBinding
import com.pandacorp.noteui.presentation.utils.helpers.Constants

open class DialogNumberPicker(private val context: Context, private val preferenceKey: String) :
    CustomDialog(context) {
    private var _binding: DialogNumberPickerBinding? = null
    private val binding get() = _binding!!

    fun getSavedValues(): Int = getTextSizeList()[binding.numberPicker.value]

    fun restoreSavedValues(selectedValue: Int) {
        binding.numberPicker.value = getTextSizeList().indexOf(selectedValue)
        binding.sample.textSize = selectedValue.toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogNumberPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        val defaultValue = when (preferenceKey) {
            Constants.Preferences.contentTextSizeKey -> Constants.Preferences.contentTextSizeDefaultValue
            Constants.Preferences.titleTextSizeKey -> Constants.Preferences.titleTextSizeDefaultValue
            else -> throw IllegalArgumentException("preferenceKey = $preferenceKey")
        }

        val preferenceValue = sp.getInt(preferenceKey, defaultValue)

        val textSizesList = getTextSizeList()

        binding.title.setText(
            when (preferenceKey) {
                Constants.Preferences.contentTextSizeKey -> R.string.contentTextSize
                Constants.Preferences.titleTextSizeKey -> R.string.titleTextSize
                else -> throw IllegalArgumentException()
            },
        )
        binding.sample.textSize = preferenceValue.toFloat()

        binding.cancel.setOnClickListener {
            cancel()
        }

        binding.ok.setOnClickListener {
            cancel()
            val value = textSizesList[binding.numberPicker.value]
            if (sp.getInt(preferenceKey, defaultValue) == value) return@setOnClickListener
            sp.edit().putInt(preferenceKey, value).apply()
            onValueAppliedListener(value.toString())
        }

        binding.numberPicker.apply {
            displayedValues = textSizesList.map { it.toString() }.toTypedArray()
            minValue = 0
            maxValue = textSizesList.size - 1
            value = textSizesList.indexOf(preferenceValue)
            wrapSelectorWheel = false

            setOnValueChangedListener { _, _, newVal ->
                vibrate()
                binding.sample.textSize = textSizesList[newVal].toFloat()
            }
        }
    }

    private fun getTextSizeList(): IntArray =
        context.resources.getIntArray(R.array.TextSizes_values)
}