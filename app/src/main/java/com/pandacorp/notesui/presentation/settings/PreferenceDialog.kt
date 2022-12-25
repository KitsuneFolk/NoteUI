package com.pandacorp.notesui.presentation.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.pandacorp.notesui.R
import com.pandacorp.notesui.presentation.adapter.ListAdapter

class PreferenceDialog(private val preferenceKey: String) : CustomDialog() {
    private lateinit var sp: SharedPreferences
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        
        val view = inflater.inflate(R.layout.dialog_preferences, container, false)
        
        val titleTextView = view.findViewById<TextView>(R.id.dialogPreferencesTitle)
        val cancelButton = view.findViewById<Button>(R.id.dialogPreferencesCancel)
        val listView = view.findViewById<ListView>(R.id.dialogPreferencesListView)
        titleTextView.setText(
                when (preferenceKey) {
                    PreferencesKeys.themesKey -> R.string.theme
                    PreferencesKeys.languagesKey -> R.string.language
                    else -> throw IllegalArgumentException()
                })
        
        val itemsList: MutableList<ListItem> = when (preferenceKey) {
            PreferencesKeys.themesKey -> fillThemesList()
            PreferencesKeys.languagesKey -> fillLanguagesList()
            else -> throw IllegalArgumentException()
        }
        titleTextView.setText(
                when (preferenceKey) {
                    PreferencesKeys.themesKey -> R.string.theme
                    PreferencesKeys.languagesKey -> R.string.language
                    else -> throw IllegalArgumentException()
                })
        
        val adapter = ListAdapter(requireContext(), itemsList, preferenceKey)
        adapter.setOnClickListener(object : ListAdapter.OnListItemClickListener {
            override fun onClick(view: View?, listItem: ListItem, position: Int) {
                sp.edit().putString(preferenceKey, listItem.value).apply()
                dialog!!.cancel()
                requireActivity().setResult(AppCompatActivity.RESULT_OK)
                requireActivity().startActivity(Intent(context, SettingsActivity::class.java))
                requireActivity().finish()
                requireActivity().overridePendingTransition(0, 0)
            }
        })
        listView.adapter = adapter
        
        cancelButton.setOnClickListener {
            dialog!!.cancel()
        }
        
        return view
    }
    
    private fun fillThemesList(): MutableList<ListItem> {
        val themesKeysList = requireContext().resources.getStringArray(R.array.Themes_values)
        val themesTitlesList = requireContext().resources.getStringArray(R.array.Themes)
        val themesDrawablesList =
            requireContext().resources.obtainTypedArray(R.array.Themes_drawables)
        val themesList: MutableList<ListItem> = mutableListOf()
        repeat(themesKeysList.size) { i ->
            themesList.add(
                    ListItem(
                            themesKeysList[i],
                            themesTitlesList[i],
                            themesDrawablesList.getDrawable(i)!!))
        }
        themesDrawablesList.recycle()
        return themesList
    }
    
    private fun fillLanguagesList(): MutableList<ListItem> {
        val languagesKeysList = requireContext().resources.getStringArray(R.array.Languages_values)
        val languagesDrawablesList =
            requireContext().resources.obtainTypedArray(R.array.Languages_drawables)
        val languagesTitlesList = requireContext().resources.getStringArray(R.array.Languages)
        val languagesList: MutableList<ListItem> = mutableListOf()
        repeat(languagesKeysList.size) { i ->
            languagesList.add(
                    ListItem(
                            languagesKeysList[i],
                            languagesTitlesList[i],
                            languagesDrawablesList.getDrawable(i)!!))
        }
        languagesDrawablesList.recycle()
        return languagesList
    }
    
    
}