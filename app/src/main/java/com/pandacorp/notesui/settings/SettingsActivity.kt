package com.pandacorp.notesui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.pandacorp.notesui.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    private val TAG = "SettingsActivity"
    private lateinit var sp: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHandler.load(this)
        setContentView(R.layout.activity_settings)
        
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
    
    class SettingsFragment : PreferenceFragmentCompat(),
        OnPreferenceChangeListener {
        private val TAG = "SettingsActivity"
        var language: String? = null
        var theme: String? = null
        var sp: SharedPreferences? = null
        lateinit var themes_listPreference: ListPreference
        lateinit var languages_listPreference: ListPreference
        lateinit var version_Preference: Preference
        
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            
            CoroutineScope(Dispatchers.IO).launch {
                initViews()
                
            }
            
            
        }
        
        private fun initViews() {
            sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
            themes_listPreference = findPreference("Themes")!!
            themes_listPreference.onPreferenceChangeListener = this
            languages_listPreference = findPreference("Languages")!!
            languages_listPreference.onPreferenceChangeListener = this
            //Тут происходит добавление загаловка в виде версии к пункту настроек.
            version_Preference = findPreference("Version")!!
            val version_name = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0).versionName
            version_Preference.title =
                resources.getString(R.string.version) + " " + version_name
        }
        
        
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
            language = sp!!.getString("Languages", "")
            theme = sp!!.getString("Themes", "")
            requireActivity().setResult(RESULT_OK)
            requireActivity().startActivity(Intent(context, SettingsActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
            
            return true
        }
    }
}