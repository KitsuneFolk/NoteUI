package com.pandacorp.noteui.presentation.utils.dialog

import android.content.Context
import android.os.Bundle
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.DialogNumberPickerEdittextBinding
import com.pandacorp.noteui.presentation.utils.helpers.Constants

class DialogNumberPickerEditText(private val context: Context, private val preferenceKey: String) :
    CustomDialog(context) {
    private var _binding: DialogNumberPickerEdittextBinding? = null
    private val binding get() = _binding!!

    fun getValue(): Int = binding.editText.text.toString().toIntOrNull() ?: 0

    fun restoreValue(selectedValue: Int) {
        // Set value to the NumberPicker if there is such value
        binding.numberPicker.value = getListValues().indexOf(selectedValue)
        binding.editText.setText(selectedValue.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogNumberPickerEdittextBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.title.setText(
            when (preferenceKey) {
                Constants.Preferences.disableDrawerAnimationKey -> R.string.preferencesDisableDrawerMenuAnimation
                else -> throw IllegalArgumentException()
            },
        )
        binding.cancel.setOnClickListener {
            cancel()
        }

        binding.ok.setOnClickListener {
            cancel()
            val value = binding.editText.text.toString().toIntOrNull() ?: 0
            if (sp.getInt(preferenceKey, -1) == value) return@setOnClickListener
            sp.edit().putInt(preferenceKey, value).apply()
            onValueAppliedListener(value.toString())
        }

        binding.numberPicker.apply {
            val preferenceValue =
                sp.getInt(
                    preferenceKey,
                    when (preferenceKey) {
                        Constants.Preferences.disableDrawerAnimationKey ->
                            Constants.Preferences.disableDrawerAnimationDefaultValue

                        else -> throw IllegalArgumentException("preferenceKey = $preferenceKey")
                    },
                )
            val valuesList = getListValues()
            val valueIndex = valuesList.indexOf(preferenceValue)

            displayedValues = valuesList.map { it.toString() }.toTypedArray()
            minValue = 0
            maxValue = valuesList.size - 1
            value = valueIndex
            wrapSelectorWheel = false

            binding.editText.setText(preferenceValue.toString())

            setOnValueChangedListener { _, _, newVal ->
                vibrate()
                binding.editText.setText(valuesList[newVal].toString())
            }
        }
    }

    private fun getListValues(): IntArray =
        context.resources.getIntArray(R.array.DisableMenuAnimation_values)
}