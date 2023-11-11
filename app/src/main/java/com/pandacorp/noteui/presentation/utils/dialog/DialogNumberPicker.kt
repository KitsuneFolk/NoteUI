package com.pandacorp.noteui.presentation.utils.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.DialogNumberPickerBinding
import com.pandacorp.noteui.presentation.utils.helpers.Constants

class DialogNumberPicker(context: Context, private val preferenceKey: String) :
    CustomDialog(context) {
    private var _binding: DialogNumberPickerBinding? = null
    private val binding get() = _binding!!

    private val values by lazy {
        when (preferenceKey) {
            in listOf(Constants.Preferences.Key.CONTENT_TEXT_SIZE, Constants.Preferences.Key.TITLE_TEXT_SIZE) -> {
                context.resources.getIntArray(R.array.TextSizes_values)
            }

            Constants.Preferences.Key.DRAWER_ANIMATION -> {
                context.resources.getIntArray(R.array.DisableMenuAnimation_values)
            }

            else -> {
                throw IllegalArgumentException("PreferenceKey = $preferenceKey")
            }
        }
    }
    private val preferenceValue by lazy {
        sp.getInt(
            preferenceKey,
            when (preferenceKey) {
                Constants.Preferences.Key.CONTENT_TEXT_SIZE -> {
                    Constants.Preferences.DefaultValue.CONTENT_TEXT_SIZE
                }

                Constants.Preferences.Key.TITLE_TEXT_SIZE -> {
                    Constants.Preferences.DefaultValue.TITLE_TEXT_SIZE
                }

                Constants.Preferences.Key.DRAWER_ANIMATION -> {
                    Constants.Preferences.DefaultValue.DRAWER_ANIMATION
                }

                else -> throw IllegalArgumentException("PreferenceKey = $preferenceKey")
            },
        )
    }
    private val isShowEditText = preferenceKey == Constants.Preferences.Key.DRAWER_ANIMATION

    val selectedValue: Int get() {
        return if (isShowEditText) {
            binding.editText.text.toString().toIntOrNull() ?: 0
        } else {
            values[binding.numberPicker.value]
        }
    }

    fun restoreValue(selectedValue: Int) {
        binding.numberPicker.value = values.indexOf(selectedValue)
        if (isShowEditText) {
            binding.editText.setText(selectedValue.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogNumberPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.title.setText(
            when (preferenceKey) {
                Constants.Preferences.Key.DRAWER_ANIMATION -> R.string.preferencesDisableDrawerMenuAnimation
                Constants.Preferences.Key.CONTENT_TEXT_SIZE -> R.string.contentTextSize
                Constants.Preferences.Key.TITLE_TEXT_SIZE -> R.string.titleTextSize
                else -> throw IllegalArgumentException("PreferenceKey = $preferenceKey")
            },
        )

        binding.ok.setOnClickListener {
            cancel()
            if (sp.getInt(preferenceKey, -1) == selectedValue) return@setOnClickListener
            sp.edit().putInt(preferenceKey, selectedValue).apply()
            onValueAppliedListener(selectedValue.toString())
        }

        binding.numberPicker.apply {
            setDisplayedValues(values.map { it.toString() }.toTypedArray())
            minValue = 0
            maxValue = values.size - 1
            value = values.indexOf(preferenceValue)

            setOnValueChangedListener { _, _, newVal ->
                vibrate()
                if (isShowEditText) {
                    binding.editText.setText(values[newVal].toString())
                } else {
                    binding.sample.textSize = values[newVal].toFloat()
                }
            }
        }

        if (isShowEditText) {
            binding.editText.setText(preferenceValue.toString())
            binding.editText.visibility = View.VISIBLE
            binding.sample.visibility = View.GONE
        } else {
            val sampleText =
                context.getString(
                    if (preferenceKey == Constants.Preferences.Key.TITLE_TEXT_SIZE) {
                        R.string.content
                    } else {
                        R.string.title
                    },
                )
            binding.sample.textSize = preferenceValue.toFloat()
            binding.sample.text = sampleText
            binding.editText.visibility = View.GONE
            binding.sample.visibility = View.VISIBLE
        }
    }
}