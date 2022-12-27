package com.pandacorp.notesui.presentation.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.*
import androidx.preference.Preference.OnPreferenceChangeListener
import com.pandacorp.notesui.R
import com.pandacorp.notesui.presentation.settings.dialog.DialogListView
import com.pandacorp.notesui.presentation.settings.dialog.DialogNumberPicker
import com.pandacorp.notesui.utils.ThemeHandler
import com.pandacorp.notesui.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var sp: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHandler(this).load()
        Utils.setupExceptionHandler()
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setTitle(R.string.settings)
        
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeButtonEnabled(true)
        }
        sp = getSharedPreferences("sp", MODE_PRIVATE)
        edit = sp.edit()
        
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
            }
            
        }
        return super.onOptionsItemSelected(item)
        
    }
    companion object {
        const val TAG = "SettingsActivity"
    
    }
    class SettingsFragment : PreferenceFragmentCompat(),
        OnPreferenceChangeListener {
        private lateinit var sp: SharedPreferences
        private lateinit var themesListPreference: ListPreference
        private lateinit var languagesListPreference: ListPreference
        private lateinit var versionPreference: Preference
        private lateinit var hideActionBarWhileScrollingSwitch: SwitchPreference
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            
            CoroutineScope(Dispatchers.IO).launch {
                initViews()
                
            }
            
            
        }
        
        private fun initViews() {
            sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
            themesListPreference = findPreference(PreferencesKeys.themesKey)!!
            themesListPreference.onPreferenceChangeListener = this
            languagesListPreference = findPreference(PreferencesKeys.languagesKey)!!
            languagesListPreference.onPreferenceChangeListener = this
            hideActionBarWhileScrollingSwitch =
                findPreference(PreferencesKeys.hideActionBarWhileScrollingKey)!!
            //Here we add title to the version preference.
            versionPreference = findPreference(PreferencesKeys.versionKey)!!
            val version = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
            versionPreference.title =
                resources.getString(R.string.version) + " " + version
        }
        
        override fun onDisplayPreferenceDialog(preference: Preference?) {
            when (preference?.key) {
                PreferencesKeys.themesKey -> { // rounded theme dialog with images
                    DialogListView.newInstance(PreferencesKeys.themesKey).show(parentFragmentManager, null)
                }
                PreferencesKeys.languagesKey -> { // rounded language dialog with images
                    DialogListView.newInstance(PreferencesKeys.languagesKey).show(parentFragmentManager, null)
                }
                PreferencesKeys.contentTextSizeKey -> {
                    DialogNumberPicker.newInstance(PreferencesKeys.contentTextSizeKey).show(parentFragmentManager, null)
                }
                PreferencesKeys.headerTextSizeKey -> {
                    DialogNumberPicker.newInstance(PreferencesKeys.headerTextSizeKey).show(parentFragmentManager, null)
                }
                else -> {
                    super.onDisplayPreferenceDialog(preference)
                }
            }
        }
        
        override fun onPreferenceChange(preference: Preference, newValue: Any) = true
        
    }
}

object PreferencesKeys {
    const val languagesKey = "Languages"
    const val themesKey = "Themes"
    const val hideActionBarWhileScrollingKey = "hide_actionbar_while_scrolling"
    const val versionKey = "Version"
    const val contentTextSizeKey = "ContentTextSize"
    const val headerTextSizeKey = "HeaderTextSize"
    const val contentTextSizeDefaultValue = "18"
    const val headerTextSizeDefaultValue = "20"
    
}