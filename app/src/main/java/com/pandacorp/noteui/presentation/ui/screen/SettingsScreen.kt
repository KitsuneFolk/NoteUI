package com.pandacorp.noteui.presentation.ui.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.ScreenSettingsBinding
import com.pandacorp.noteui.presentation.utils.dialog.DialogListView
import com.pandacorp.noteui.presentation.utils.dialog.DialogNumberPicker
import com.pandacorp.noteui.presentation.utils.dialog.DialogNumberPickerEditText
import com.pandacorp.noteui.presentation.utils.helpers.Constants
import com.pandacorp.noteui.presentation.utils.helpers.app
import com.pandacorp.noteui.presentation.utils.helpers.getPackageInfoCompat
import com.pandacorp.noteui.presentation.utils.helpers.sp

class SettingsScreen : Fragment() {
    private var _binding: ScreenSettingsBinding? = null
    private val binding get() = _binding!!

    private val languageDialog by lazy {
        DialogListView(requireActivity(), Constants.Preferences.languagesKey).apply {
            setOnValueAppliedListener {
                requireActivity().recreate()
            }
        }
    }
    private val themeDialog by lazy {
        DialogListView(requireActivity(), Constants.Preferences.themesKey).apply {
            setOnValueAppliedListener {
                requireActivity().recreate()
            }
        }
    }
    private val titleDialog by lazy {
        DialogNumberPicker(requireActivity(), Constants.Preferences.titleTextSizeKey).apply {
            setOnValueAppliedListener {
                binding.titleSizeTextView.text = it
            }
        }
    }
    private val contentDialog by lazy {
        DialogNumberPicker(requireActivity(), Constants.Preferences.contentTextSizeKey).apply {
            setOnValueAppliedListener {
                binding.contentSizeTextView.text = it
            }
        }
    }
    private val drawerAnimationDialog by lazy {
        DialogNumberPickerEditText(requireActivity(), Constants.Preferences.disableDrawerAnimationKey).apply {
            setOnValueAppliedListener {
                binding.drawerAnimationtTextView.text = it
            }
        }
    }

    private fun initViews() {
        binding.toolbarInclude.toolbar.apply {
            binding.toolbarInclude.toolbar.setTitle(R.string.settings)
            setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
            setNavigationOnClickListener {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        // Retrieve the version from build.gradle and assign it to the TextView
        binding.versionTextView.apply {
            val version =
                requireContext().packageManager.getPackageInfoCompat(requireContext().packageName).versionName
            text = resources.getString(R.string.version, version)
        }
        binding.themeLayout.apply {
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                themeDialog.show()
            }
            binding.themeTextView.apply {
                val themeKey = sp.getString(
                    Constants.Preferences.themesKey,
                    requireContext().resources.getString(R.string.settings_theme_default_value)
                )!!
                text = getThemeFromKey(themeKey)
            }
        }
        binding.languageLayout.apply {
            binding.languageTextView.apply {
                val languageKey = sp.getString(
                    Constants.Preferences.languagesKey,
                    requireContext().resources.getString(R.string.settings_language_default_value)
                )!!
                text = getLanguageFromKey(languageKey)
            }
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                languageDialog.show()
            }
        }

        binding.showFabTextLayout.apply {
            binding.showFabTextSwitch.isChecked = sp.getBoolean(
                Constants.Preferences.isShowFabTextKey,
                Constants.Preferences.isShowFabTextDefaultValue
            )
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                val newValue = !binding.showFabTextSwitch.isChecked
                sp.edit()
                    .putBoolean(Constants.Preferences.isShowFabTextKey, newValue)
                    .apply()
                binding.showFabTextSwitch.isChecked = newValue
                app.isSettingsChanged = true
            }
        }
        binding.hideActionBarLayout.apply {
            binding.hideActionBarSwitch.isChecked = sp.getBoolean(
                Constants.Preferences.isHideActionBarOnScrollKey,
                Constants.Preferences.isHideActionBarOnScrollDefaultValue
            )
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                val newValue = !binding.hideActionBarSwitch.isChecked
                sp.edit()
                    .putBoolean(
                        Constants.Preferences.isHideActionBarOnScrollKey,
                        newValue
                    )
                    .apply()
                binding.hideActionBarSwitch.isChecked = newValue
                app.isSettingsChanged = true
            }
        }

        binding.drawerMenuAnimationLayout.apply {
            binding.drawerAnimationtTextView.text = sp.getInt(
                Constants.Preferences.disableDrawerAnimationKey,
                Constants.Preferences.disableDrawerAnimationDefaultValue
            ).toString()
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                drawerAnimationDialog.show()
            }
        }
        binding.titleSizeLayout.apply {
            binding.titleSizeTextView.text = sp.getInt(
                Constants.Preferences.titleTextSizeKey,
                Constants.Preferences.titleTextSizeDefaultValue
            ).toString()
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                titleDialog.show()
            }
        }
        binding.contentSizeLayout.apply {
            binding.contentSizeTextView.text = sp.getInt(
                Constants.Preferences.contentTextSizeKey,
                Constants.Preferences.contentTextSizeDefaultValue
            ).toString()
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                contentDialog.show()
            }
        }
    }

    private fun getThemeFromKey(key: String): String {
        val themes = resources.getStringArray(R.array.Themes)
        val keys = resources.getStringArray(R.array.Themes_values)

        val index = keys.indexOf(key)
        return themes[index]
    }

    private fun getLanguageFromKey(key: String): String {
        val languages = resources.getStringArray(R.array.Languages)
        val keys = resources.getStringArray(R.array.Languages_values)

        val index = keys.indexOf(key)
        return languages[index]
    }

    private fun isDialogShown(): Boolean =
        (contentDialog.isShowing || languageDialog.isShowing || themeDialog.isShowing || titleDialog.isShowing)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ScreenSettingsBinding.inflate(layoutInflater)
        initViews()
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var savedValue: Int? = null
        val dialogKey = when {
            themeDialog.isShowing -> Constants.Preferences.themesKey
            languageDialog.isShowing -> Constants.Preferences.languagesKey

            titleDialog.isShowing -> {
                savedValue = titleDialog.getSavedValues()
                Constants.Preferences.titleTextSizeKey
            }

            contentDialog.isShowing -> {
                savedValue = contentDialog.getSavedValues()
                Constants.Preferences.contentTextSizeKey
            }

            drawerAnimationDialog.isShowing -> {
                savedValue = drawerAnimationDialog.getValue()
                Constants.Preferences.disableDrawerAnimationKey
            }

            else -> null
        }

        outState.apply {
            putString(Constants.Preferences.SHOWED_DIALOG, dialogKey)
            putInt(Constants.Preferences.SAVED_VALUE, savedValue ?: return@apply)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val savedValue = savedInstanceState?.getInt(Constants.Preferences.SAVED_VALUE)
        when (savedInstanceState?.getString(Constants.Preferences.SHOWED_DIALOG, null)) {
            Constants.Preferences.themesKey -> themeDialog.show()
            Constants.Preferences.languagesKey -> languageDialog.show()
            Constants.Preferences.titleTextSizeKey -> titleDialog.apply {
                show()
                restoreSavedValues(savedValue ?: return@apply)
            }

            Constants.Preferences.contentTextSizeKey -> contentDialog.apply {
                show()
                restoreSavedValues(savedValue ?: return@apply)
            }

            Constants.Preferences.disableDrawerAnimationKey -> drawerAnimationDialog.apply {
                show()
                restoreValue(savedValue ?: return@apply)
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        titleDialog.dismiss()
        contentDialog.dismiss()
        themeDialog.dismiss()
        languageDialog.dismiss()
        drawerAnimationDialog.dismiss()
        super.onDestroy()
    }
}