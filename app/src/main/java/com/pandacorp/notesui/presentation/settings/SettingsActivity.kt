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
import com.pandacorp.notesui.utils.Constans
import com.pandacorp.notesui.utils.PreferenceHandler
import com.pandacorp.notesui.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private lateinit var sp: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceHandler(this).load()
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
        private lateinit var isShowAddNoteFABText: SwitchPreference
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            
            CoroutineScope(Dispatchers.IO).launch {
                initViews()
                
            }
            
            
        }
        
        private fun initViews() {
            sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
            themesListPreference = findPreference(Constans.PreferencesKeys.themesKey)!!
            themesListPreference.onPreferenceChangeListener = this
            languagesListPreference = findPreference(Constans.PreferencesKeys.languagesKey)!!
            languagesListPreference.onPreferenceChangeListener = this
            isShowAddNoteFABText = findPreference(Constans.PreferencesKeys.isShowAddNoteFABTextKey)!!
            isShowAddNoteFABText.setOnPreferenceChangeListener { preference, newValue ->
                sp.edit().putBoolean(Constans.PreferencesKeys.isShowAddNoteFABTextKey, newValue as Boolean).apply()
                requireActivity().setResult(RESULT_OK)
                return@setOnPreferenceChangeListener true
            }
            hideActionBarWhileScrollingSwitch =
                findPreference(Constans.PreferencesKeys.isHideActionBarOnScrollKey)!!
            //Here we add title to the version preference.
            versionPreference = findPreference(Constans.PreferencesKeys.versionKey)!!
            val version = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
            versionPreference.title =
                resources.getString(R.string.version) + " " + version
        }
        
        override fun onDisplayPreferenceDialog(preference: Preference?) {
            when (preference?.key) {
                Constans.PreferencesKeys.themesKey -> { // rounded theme dialog with images
                    DialogListView.newInstance(Constans.PreferencesKeys.themesKey).show(parentFragmentManager, null)
                }
                Constans.PreferencesKeys.languagesKey -> { // rounded language dialog with images
                    DialogListView.newInstance(Constans.PreferencesKeys.languagesKey).show(parentFragmentManager, null)
                }
                Constans.PreferencesKeys.contentTextSizeKey -> {
                    DialogNumberPicker.newInstance(Constans.PreferencesKeys.contentTextSizeKey).show(parentFragmentManager, null)
                }
                Constans.PreferencesKeys.headerTextSizeKey -> {
                    DialogNumberPicker.newInstance(Constans.PreferencesKeys.headerTextSizeKey).show(parentFragmentManager, null)
                }
                else -> {
                    super.onDisplayPreferenceDialog(preference)
                }
            }
        }
        
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            return true
            
        }
        
    }
}