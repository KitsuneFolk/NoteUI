package com.pandacorp.noteui.presentation.ui.screen

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.NavBackStackEntry
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeFragment
import com.dolatkia.animatedThemeManager.ThemeManager
import com.fragula2.adapter.NavBackStackAdapter
import com.fragula2.navigation.SwipeBackFragment
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.ScreenSettingsBinding
import com.pandacorp.noteui.presentation.ui.activity.MainActivity
import com.pandacorp.noteui.presentation.utils.dialog.DialogListView
import com.pandacorp.noteui.presentation.utils.dialog.DialogNumberPicker
import com.pandacorp.noteui.presentation.utils.helpers.Constants
import com.pandacorp.noteui.presentation.utils.helpers.PreferenceHandler
import com.pandacorp.noteui.presentation.utils.helpers.app
import com.pandacorp.noteui.presentation.utils.helpers.getPackageInfoCompat
import com.pandacorp.noteui.presentation.utils.helpers.sp
import com.pandacorp.noteui.presentation.utils.themes.ViewHelper

class SettingsScreen : ThemeFragment() {
    private var _binding: ScreenSettingsBinding? = null
    private val binding get() = _binding!!

    private val languageDialog by lazy {
        DialogListView(requireContext(), Constants.Preferences.Key.LANGUAGE).apply {
            onValueAppliedListener = {
                PreferenceHandler.setLanguage(requireContext(), it)
            }
        }
    }
    private val themeDialog by lazy {
        DialogListView(requireContext(), Constants.Preferences.Key.THEME).apply {
            // Using Java reflection remove MainScreen from backstack to recreate it
            val swipeBackFragment = (requireActivity() as MainActivity)
                .navHostFragment!!.childFragmentManager.fragments.first() as SwipeBackFragment
            val adapterField = swipeBackFragment.javaClass.getDeclaredField("navBackStackAdapter")
            adapterField.isAccessible = true
            val adapter = adapterField.get(swipeBackFragment) as NavBackStackAdapter
            val listField = adapter.javaClass.getDeclaredField("currentList")
            listField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val currentList = listField.get(adapter) as MutableList<NavBackStackEntry>
            val mainScreen = currentList[0]
            onValueAppliedListener = {
                binding.themeTextView.text = getThemeFromKey(it)
                ThemeManager.instance.changeTheme(PreferenceHandler.getThemeByKey(requireContext(), it), binding.root)
                currentList.removeAt(0)
                currentList.add(0, mainScreen)
                adapter.notifyItemChanged(0)
            }
        }
    }
    private val titleDialog by lazy {
        DialogNumberPicker(requireContext(), Constants.Preferences.Key.TITLE_TEXT_SIZE).apply {
            onValueAppliedListener = {
                binding.titleSizeTextView.text = it
            }
        }
    }
    private val contentDialog by lazy {
        DialogNumberPicker(requireContext(), Constants.Preferences.Key.CONTENT_TEXT_SIZE).apply {
            onValueAppliedListener = {
                binding.contentSizeTextView.text = it
            }
        }
    }
    private val drawerAnimationDialog by lazy {
        DialogNumberPicker(requireContext(), Constants.Preferences.Key.DRAWER_ANIMATION).apply {
            onValueAppliedListener = {
                binding.drawerAnimationtTextView.text = it
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ScreenSettingsBinding.inflate(layoutInflater.cloneInContext(requireContext()))
        ViewHelper.applyTheme(ViewHelper.currentTheme, binding.root)

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
                    savedValue = titleDialog.selectedValue
                    Constants.Preferences.Key.TITLE_TEXT_SIZE
                }

                contentDialog.isShowing -> {
                    savedValue = contentDialog.selectedValue
                    Constants.Preferences.Key.CONTENT_TEXT_SIZE
                }

                drawerAnimationDialog.isShowing -> {
                    savedValue = drawerAnimationDialog.selectedValue
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
                    restoreValue(savedValue ?: return@apply)
                }

            Constants.Preferences.Key.CONTENT_TEXT_SIZE ->
                contentDialog.apply {
                    show()
                    restoreValue(savedValue ?: return@apply)
                }

            Constants.Preferences.Key.DRAWER_ANIMATION ->
                drawerAnimationDialog.apply {
                    show()
                    restoreValue(savedValue ?: return@apply)
                }
        }
    }

    override fun syncTheme(appTheme: AppTheme) {
        ViewHelper.applyTheme(newTheme = appTheme, viewGroup = binding.root)
        ViewHelper.currentTheme = appTheme
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

    override fun getContext(): Context {
        val oldContext = super.getContext()
        return ContextThemeWrapper(oldContext, PreferenceHandler.getThemeRes(oldContext!!))
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
        binding.versionLayout.apply {
            val vib =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager =
                        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }
            setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(50L, 20))
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(50L)
                }
            }
            binding.versionTextView.apply {
                val version =
                    requireContext().packageManager.getPackageInfoCompat(requireContext().packageName).versionName
                text = resources.getString(R.string.version, version)
            }
        }

        binding.themeLayout.apply {
            setOnClickListener {
                themeDialog.show()
            }
            binding.themeTextView.apply {
                val themeKey =
                    sp.getString(
                        Constants.Preferences.Key.THEME,
                        requireContext().resources.getString(R.string.settings_theme_default_value)
                    )!!
                text = getThemeFromKey(themeKey)
            }
        }
        binding.languageLayout.apply {
            binding.languageTextView.apply {
                val languageKey =
                    sp.getString(
                        Constants.Preferences.Key.LANGUAGE,
                        requireContext().resources.getString(R.string.settings_language_default_value)
                    )!!
                text = getLanguageFromKey(languageKey)
            }
            setOnClickListener {
                languageDialog.show()
            }
        }

        binding.themeBackgroundLayout.apply {
            binding.themeBackgroundSwitch.isChecked = PreferenceHandler.isShowThemeBackground(requireContext())
            setOnClickListener {
                val newValue = !binding.themeBackgroundSwitch.isChecked
                sp.edit()
                    .putBoolean(Constants.Preferences.Key.SHOW_THEME_BACKGROUND, newValue)
                    .apply()
                binding.themeBackgroundSwitch.isChecked = newValue
                PreferenceHandler.setShowThemeBackground(requireContext(), newValue)
                app.isSettingsChanged = true
            }
        }

        binding.showFabTextLayout.apply {
            binding.showFabTextSwitch.isChecked =
                sp.getBoolean(
                    Constants.Preferences.Key.SHOW_FAB,
                    Constants.Preferences.DefaultValue.SHOW_FAB
                )
            setOnClickListener {
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
                    Constants.Preferences.DefaultValue.HIDE_ACTIONBAR_ON_SCROLL
                )
            setOnClickListener {
                val newValue = !binding.hideActionBarSwitch.isChecked
                sp.edit()
                    .putBoolean(
                        Constants.Preferences.Key.HIDE_ACTIONBAR_ON_SCROLL,
                        newValue
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
                    Constants.Preferences.DefaultValue.DRAWER_ANIMATION
                ).toString()
            setOnClickListener {
                drawerAnimationDialog.show()
            }
        }
        binding.titleSizeLayout.apply {
            binding.titleSizeTextView.text =
                sp.getInt(
                    Constants.Preferences.Key.TITLE_TEXT_SIZE,
                    Constants.Preferences.DefaultValue.TITLE_TEXT_SIZE
                ).toString()
            setOnClickListener {
                titleDialog.show()
            }
        }
        binding.contentSizeLayout.apply {
            binding.contentSizeTextView.text =
                sp.getInt(
                    Constants.Preferences.Key.CONTENT_TEXT_SIZE,
                    Constants.Preferences.DefaultValue.CONTENT_TEXT_SIZE
                ).toString()
            setOnClickListener {
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
}