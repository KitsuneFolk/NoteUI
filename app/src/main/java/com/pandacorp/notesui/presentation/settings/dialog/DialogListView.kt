package com.pandacorp.notesui.presentation.settings.dialog

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
import com.pandacorp.notesui.presentation.settings.ListItem
import com.pandacorp.notesui.presentation.settings.SettingsActivity
import com.pandacorp.notesui.utils.Constans

class DialogListView : CustomDialog() {
    private lateinit var sp: SharedPreferences
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        
        sp = PreferenceManager.getDefaultSharedPreferences(requireContext())
        
        val view = inflater.inflate(R.layout.dialog_preferences_list_view, container, false)
        
        val preferenceKey = requireArguments().getString(Constans.PreferencesKeys.preferenceBundleKey)
        
        val titleTextView = view.findViewById<TextView>(R.id.dialogPreferencesListView_title)
        val cancelButton = view.findViewById<Button>(R.id.dialogPreferencesListView_cancel)
        val listView = view.findViewById<ListView>(R.id.dialogPreferencesListView_listView)
        
        titleTextView.setText(
                when (preferenceKey) {
                    Constans.PreferencesKeys.themesKey -> R.string.theme
                    Constans.PreferencesKeys.languagesKey -> R.string.language
                    else -> throw IllegalArgumentException("PreferenceKey = $preferenceKey")
                }
        )
        
        cancelButton.setOnClickListener {
            dialog!!.cancel()
        }
        
        val itemsList: MutableList<ListItem> = when (preferenceKey) {
            Constans.PreferencesKeys.themesKey -> fillThemesList()
            Constans.PreferencesKeys.languagesKey -> fillLanguagesList()
            else -> throw IllegalArgumentException()
            
        }
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
        
        
        return view
    }
    
    private fun fillThemesList(): MutableList<ListItem> {
        val keysList = requireContext().resources.getStringArray(R.array.Themes_values)
        val titlesList = requireContext().resources.getStringArray(R.array.Themes)
        val itemsList =
            requireContext().resources.obtainTypedArray(R.array.Themes_drawables)
        val themesList: MutableList<ListItem> = mutableListOf()
        repeat(keysList.size) { i ->
            themesList.add(
                    ListItem(
                            keysList[i],
                            titlesList[i],
                            itemsList.getDrawable(i)!!))
        }
        itemsList.recycle()
        return themesList
    }
    
    private fun fillLanguagesList(): MutableList<ListItem> {
        val keysList = requireContext().resources.getStringArray(R.array.Languages_values)
        val drawablesList =
            requireContext().resources.obtainTypedArray(R.array.Languages_drawables)
        val titlesList = requireContext().resources.getStringArray(R.array.Languages)
        val itemsList: MutableList<ListItem> = mutableListOf()
        repeat(keysList.size) { i ->
            itemsList.add(
                    ListItem(
                            keysList[i],
                            titlesList[i],
                            drawablesList.getDrawable(i)!!))
        }
        drawablesList.recycle()
        return itemsList
    }
    
    companion object {
        fun newInstance(preferenceKey: String): DialogListView {
            val args = Bundle()
            args.putString(Constans.PreferencesKeys.preferenceBundleKey, preferenceKey)
            val dialog = DialogListView()
            dialog.arguments = args
            return dialog
        }
    }
    
}