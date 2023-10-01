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
        DialogListView(requireActivity(), Constants.Preferences.Key.LANGUAGE).apply {
            setOnValueAppliedListener {
                requireActivity().recreate()
            }
        }
    }
    private val themeDialog by lazy {
        DialogListView(requireActivity(), Constants.Preferences.Key.THEME).apply {
            setOnValueAppliedListener {
                requireActivity().recreate()
            }
        }
    }
    private val titleDialog by lazy {
        DialogNumberPicker(requireActivity(), Constants.Preferences.Key.TITLE_TEXT_SIZE).apply {
            setOnValueAppliedListener {
                binding.titleSizeTextView.text = it
            }
        }
    }
    private val contentDialog by lazy {
        DialogNumberPicker(requireActivity(), Constants.Preferences.Key.CONTENT_TEXT_SIZE).apply {
            setOnValueAppliedListener {
                binding.contentSizeTextView.text = it
            }
        }
    }
    private val drawerAnimationDialog by lazy {
        DialogNumberPickerEditText(requireActivity(), Constants.Preferences.Key.DRAWER_ANIMATION).apply {
            setOnValueAppliedListener {
                binding.drawerAnimationtTextView.text = it
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ScreenSettingsBinding.inflate(layoutInflater)

        initViews()

        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var savedValue: Int? = null
        val dialogKey =
            when {
                themeDialog.isShowing -> Constants.Preferences.Key.THEME
                languageDialog.isShowing -> Constants.Preferences.Key.LANGUAGE

                titleDialog.isShowing -> {
                    savedValue = titleDialog.getSavedValues()
                    Constants.Preferences.Key.TITLE_TEXT_SIZE
                }

                contentDialog.isShowing -> {
                    savedValue = contentDialog.getSavedValues()
                    Constants.Preferences.Key.CONTENT_TEXT_SIZE
                }

                drawerAnimationDialog.isShowing -> {
                    savedValue = drawerAnimationDialog.getValue()
                    Constants.Preferences.Key.DRAWER_ANIMATION
                }

                else -> null
            }

        outState.apply {
            putString(Constants.DialogKey.SHOWED_DIALOG, dialogKey)
            putInt(Constants.DialogKey.SAVED_VALUE, savedValue ?: return@apply)
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val savedValue = savedInstanceState?.getInt(Constants.DialogKey.SAVED_VALUE)
        when (savedInstanceState?.getString(Constants.DialogKey.SHOWED_DIALOG, null)) {
            Constants.Preferences.Key.THEME -> themeDialog.show()
            Constants.Preferences.Key.LANGUAGE -> languageDialog.show()
            Constants.Preferences.Key.TITLE_TEXT_SIZE ->
                titleDialog.apply {
                    show()
                    restoreSavedValues(savedValue ?: return@apply)
                }

            Constants.Preferences.Key.CONTENT_TEXT_SIZE ->
                contentDialog.apply {
                    show()
                    restoreSavedValues(savedValue ?: return@apply)
                }

            Constants.Preferences.Key.DRAWER_ANIMATION ->
                drawerAnimationDialog.apply {
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
                val themeKey =
                    sp.getString(
                        Constants.Preferences.Key.THEME,
                        requireContext().resources.getString(R.string.settings_theme_default_value),
                    )!!
                text = getThemeFromKey(themeKey)
            }
        }
        binding.languageLayout.apply {
            binding.languageTextView.apply {
                val languageKey =
                    sp.getString(
                        Constants.Preferences.Key.LANGUAGE,
                        requireContext().resources.getString(R.string.settings_language_default_value),
                    )!!
                text = getLanguageFromKey(languageKey)
            }
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                languageDialog.show()
            }
        }

        binding.showFabTextLayout.apply {
            binding.showFabTextSwitch.isChecked =
                sp.getBoolean(
                    Constants.Preferences.Key.SHOW_FAB,
                    Constants.Preferences.DefaultValue.SHOW_FAB,
                )
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                val newValue = !binding.showFabTextSwitch.isChecked
                sp.edit()
                    .putBoolean(Constants.Preferences.Key.SHOW_FAB, newValue)
                    .apply()
                binding.showFabTextSwitch.isChecked = newValue
                app.isSettingsChanged = true
            }
        }
        binding.hideActionBarLayout.apply {
            binding.hideActionBarSwitch.isChecked =
                sp.getBoolean(
                    Constants.Preferences.Key.HIDE_ACTIONBAR_ON_SCROLL,
                    Constants.Preferences.DefaultValue.HIDE_ACTIONBAR_ON_SCROLL,
                )
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                val newValue = !binding.hideActionBarSwitch.isChecked
                sp.edit()
                    .putBoolean(
                        Constants.Preferences.Key.HIDE_ACTIONBAR_ON_SCROLL,
                        newValue,
                    )
                    .apply()
                binding.hideActionBarSwitch.isChecked = newValue
                app.isSettingsChanged = true
            }
        }

        binding.drawerMenuAnimationLayout.apply {
            binding.drawerAnimationtTextView.text =
                sp.getInt(
                    Constants.Preferences.Key.DRAWER_ANIMATION,
                    Constants.Preferences.DefaultValue.DRAWER_ANIMATION,
                ).toString()
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                drawerAnimationDialog.show()
            }
        }
        binding.titleSizeLayout.apply {
            binding.titleSizeTextView.text =
                sp.getInt(
                    Constants.Preferences.Key.TITLE_TEXT_SIZE,
                    Constants.Preferences.DefaultValue.TITLE_TEXT_SIZE,
                ).toString()
            setOnClickListener {
                if (isDialogShown()) return@setOnClickListener
                titleDialog.show()
            }
        }
        binding.contentSizeLayout.apply {
            binding.contentSizeTextView.text =
                sp.getInt(
                    Constants.Preferences.Key.CONTENT_TEXT_SIZE,
                    Constants.Preferences.DefaultValue.CONTENT_TEXT_SIZE,
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
}