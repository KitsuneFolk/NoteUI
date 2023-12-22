package com.pandacorp.noteui.presentation.utils.dialog

import android.content.Context
import android.os.Bundle
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.DialogListViewBinding
import com.pandacorp.noteui.presentation.ui.adapter.settings.SettingsAdapter
import com.pandacorp.noteui.presentation.ui.adapter.settings.SettingsItem
import com.pandacorp.noteui.presentation.utils.helpers.Constants

class DialogListView(private val context: Context, private val preferenceKey: String) : CustomDialog(context) {
    private lateinit var binding: DialogListViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DialogListViewBinding.inflate(layoutInflater)

        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        binding.dialogListViewTitle.setText(
            when (preferenceKey) {
                Constants.Preferences.Key.THEME -> R.string.theme
                Constants.Preferences.Key.LANGUAGE -> R.string.language
                else -> throw IllegalArgumentException("PreferenceKey = $preferenceKey")
            }
        )

        val itemsList: MutableList<SettingsItem> = when (preferenceKey) {
            Constants.Preferences.Key.THEME -> fillThemesList()
            Constants.Preferences.Key.LANGUAGE -> fillLanguagesList()
            else -> throw IllegalArgumentException()
        }
        binding.dialogListViewListView.adapter = SettingsAdapter(context, itemsList, preferenceKey).apply {
            setOnClickListener { listItem ->
                cancel()
                val value = listItem.value
                if (sp.getString(preferenceKey, "") == value) return@setOnClickListener
                sp.edit().putString(preferenceKey, value).apply()
                onValueAppliedListener(value)
            }
        }
    }

    private fun fillThemesList(): MutableList<SettingsItem> {
        val valuesList = context.resources.getStringArray(R.array.Themes_values)
        val drawablesList = context.resources.obtainTypedArray(R.array.Themes_drawables)
        val titlesList = context.resources.getStringArray(R.array.Themes)
        val themesList: MutableList<SettingsItem> = mutableListOf()
        repeat(valuesList.size) { i ->
            themesList.add(
                SettingsItem(
                    valuesList[i],
                    titlesList[i],
                    drawablesList.getDrawable(i)!!
                )
            )
        }
        drawablesList.recycle()
        return themesList
    }

    private fun fillLanguagesList(): MutableList<SettingsItem> {
        val valuesList = context.resources.getStringArray(R.array.Languages_values)
        val drawablesList = context.resources.obtainTypedArray(R.array.Languages_drawables)
        val titlesList = context.resources.getStringArray(R.array.Languages)
        val languagesList: MutableList<SettingsItem> = mutableListOf()
        repeat(valuesList.size) { i ->
            languagesList.add(
                SettingsItem(
                    valuesList[i],
                    titlesList[i],
                    drawablesList.getDrawable(i)!!
                )
            )
        }
        drawablesList.recycle()
        return languagesList
    }
}