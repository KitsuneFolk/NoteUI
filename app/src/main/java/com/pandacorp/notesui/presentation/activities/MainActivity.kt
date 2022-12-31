package com.pandacorp.notesui.presentation.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.usecases.notes.database.GetNotesUseCase
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.ActivityMainBinding
import com.pandacorp.notesui.presentation.adapter.NotesRecyclerAdapter
import com.pandacorp.notesui.presentation.settings.SettingsActivity
import com.pandacorp.notesui.utils.Constans
import com.pandacorp.notesui.utils.ThemeHandler
import com.pandacorp.notesui.utils.Utils
import com.pandacorp.notesui.viewModels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    
    private lateinit var toolbar: Toolbar
    
    private lateinit var sp: SharedPreferences
    
    lateinit var notesRecyclerAdapter: NotesRecyclerAdapter
    
    private val filteredList = mutableListOf<NoteItem>()
    
    private var actionModeCallback: ActionModeCallback = ActionModeCallback()
    private var actionMode: ActionMode? = null
    
    private val vm: MainViewModel by viewModel()
    
    private val getNotesUseCase: GetNotesUseCase by inject()
    
    private val noteActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        
        try {
            if (filteredList.isNotEmpty()) {
                notesRecyclerAdapter.setList(filteredList) // if user searched for notes, opened a activity and then
            } else {
                vm.updateNotes()
                
            }
            
            
        } catch (e: Exception) {
            // If activity_note screen was rotated and user backed to MainActivity
            // then we do nothing, we don't notify adapter about changes because in onCreate
            // it will be created again.
        }
        
        
    }
    private val resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(0, R.anim.slide_out_right)
        }
        
        
    }
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHandler(this).load()
        binding = ActivityMainBinding.inflate(layoutInflater)
        Utils.setupExceptionHandler()
        setContentView(binding.root)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        initPreferences()
        CoroutineScope(Dispatchers.Main).launch {
            initViews()
        }
        
    }
    
    private fun initViews() {
        initRecyclerView()
        initAddFloatingActionButton()
        
    }
    
    private fun initPreferences() {
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        val isShowAddNoteFABText =
            sp.getBoolean(Constans.PreferencesKeys.isShowAddNoteFABTextKey, true)
        if (!isShowAddNoteFABText) binding.addFAB.shrink()  // remove text
    }
    
    private fun initAddFloatingActionButton() {
        binding.addFAB.setOnClickListener {
            val colorBackground = ContextCompat.getColor(
                    this,
                    ThemeHandler(this).getColorBackground())
            
            val intent = Intent(this, NoteActivity::class.java)
            val noteHeader = ""
            val noteContent = ""
            val noteBackground = colorBackground.toString()
            val noteIsShowTransparentActionBar = false
            
            val noteItem =
                NoteItem(header = noteHeader, content = noteContent, background = noteBackground)
            this@MainActivity.vm.addNote(0, noteItem)
            
            intent.putExtra(Constans.Bundles.noteHeaderText, noteHeader)
            intent.putExtra(Constans.Bundles.noteContentText, noteContent)
            intent.putExtra(Constans.Bundles.noteBackground, noteBackground)
            intent.putExtra(
                    Constans.Bundles.noteIsShowTransparentActionBar,
                    noteIsShowTransparentActionBar)
            
            noteActivityLauncher.launch(intent)
            
            
        }
    }
    
    private fun initRecyclerView() {
        notesRecyclerAdapter = NotesRecyclerAdapter(this, mutableListOf())
        notesRecyclerAdapter.setOnClickListener(object :
            NotesRecyclerAdapter.OnNoteItemClickListener {
            override fun onClick(view: View?, noteItem: NoteItem, position: Int) {
                if (notesRecyclerAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(position)
                } else {
                    val intent = Intent(this@MainActivity, NoteActivity::class.java)
                    
                    with(intent) {
                        putExtra(Constans.Bundles.noteHeaderText, noteItem.header)
                        putExtra(Constans.Bundles.noteContentText, noteItem.content)
                        putExtra(Constans.Bundles.noteBackground, noteItem.background)
                        putExtra(
                                Constans.Bundles.noteIsShowTransparentActionBar,
                                noteItem.isShowTransparentActionBar)
                        
                        putExtra(NoteActivity.intentNotePositionInAdapter, position)
                    }
                    
                    noteActivityLauncher.launch(intent)
                    
                }
            }
            
        })
        notesRecyclerAdapter.setOnLongClickListener(object :
            NotesRecyclerAdapter.OnNoteItemLongClickListener {
            override fun onLongClick(view: View?, noteItem: NoteItem, position: Int) {
                enableActionMode(position)
            }
            
        })
        binding.recyclerView.adapter = notesRecyclerAdapter
        registerForContextMenu(binding.recyclerView)
        vm.notesList.observe(this) {
            notesRecyclerAdapter.setList(it)
            
        }
        
    }
    
    private var searchJob: Job? = null
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.menu_search)?.actionView as SearchView
        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                
                return true
            }
            
            override fun onQueryTextChange(text: String?): Boolean {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    if (text.isNullOrEmpty()) filter(null)
                    else filter(text)
                    
                }
                return false
            }
            
        })
        return true
    }
    
    private fun filter(text: String?) {
        filteredList.clear()
        CoroutineScope(Dispatchers.IO).launch {
            val notesList = getNotesUseCase()
            // If text is null, then show all notes.
            if (text == null) {
                CoroutineScope(Dispatchers.Main).launch {
                    notesRecyclerAdapter.setList(notesList)
                    
                }
                
            } else {
                for (note in notesList) {
                    if (note.header.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                        filteredList.add(note)
                    }
                }
                if (filteredList.isEmpty()) {
                    CoroutineScope(Dispatchers.Main).launch {
                        notesRecyclerAdapter.setList(filteredList)
                        
                    }
                    
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        notesRecyclerAdapter.setList(filteredList)
                        
                    }
                }
                
            }
            
        }
        
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                resultLauncher.launch(Intent(this, SettingsActivity::class.java))
                
            }
            
        }
        
        return true
    }
    
    private fun enableActionMode(position: Int) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback)
        }
        toggleSelection(position)
    }
    
    private fun toggleSelection(position: Int) {
        notesRecyclerAdapter.toggleSelection(position)
        val count: Int = notesRecyclerAdapter.getSelectedItemCount()
        if (count == 0) {
            actionMode!!.finish()
        } else {
            actionMode!!.title = count.toString()
            actionMode!!.invalidate()
        }
    }
    
    //This class needed for creating multiselect on RecyclerView
    inner class ActionModeCallback : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.recyclerview_select_menu, menu)
            
            return true
        }
        
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            
            return false
        }
        
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            val id = item.itemId
            if (id == R.id.recyclerview_menu_delete) {
                handleSelectedNotesRemoving()
                mode.finish()
                return true
            }
            if (id == R.id.recyclerview_menu_select_all) {
                notesRecyclerAdapter.selectAllItems()
                val count: Int = notesRecyclerAdapter.getSelectedItemCount()
                actionMode!!.title = count.toString()
                actionMode!!.invalidate()
            }
            return false
        }
        
        override fun onDestroyActionMode(mode: ActionMode) {
            notesRecyclerAdapter.clearSelections()
            actionMode = null
            
        }
        
        private fun handleSelectedNotesRemoving() {
            val removedNotes = removeSelectedNotes()
            if (removedNotes.isEmpty()) return
            val snackBarUndoTitle = resources.getText(R.string.snackbar_undo_title)
                .toString() + " " + removedNotes.size.toString()
            val snackBarDuration = 4_000
            val undoSnackbar = Snackbar.make(
                    binding.addFAB,
                    snackBarUndoTitle,
                    snackBarDuration)
            
            undoSnackbar.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            val colorAccent = ContextCompat.getColor(
                    this@MainActivity,
                    ThemeHandler(this@MainActivity).getColorAccent())
            undoSnackbar.setActionTextColor(colorAccent)
            undoSnackbar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
            undoSnackbar.setAction(R.string.undo) {
                vm.restoreNotes(removedNotes)
                val successSnackbar = Snackbar.make(
                        binding.addFAB,
                        snackBarUndoTitle,
                        snackBarDuration)
                successSnackbar.setText(R.string.successfully)
                successSnackbar.duration = 1_000
                successSnackbar.setTextColor(Color.WHITE)
                successSnackbar.show()
            }
            undoSnackbar.show()
        }
        
        /**
         * @return List of Pairs NoteItem and Int, Int - position of the noteItem in the adapter
         */
        private fun removeSelectedNotes(): MutableList<Pair<NoteItem, Int>> {
            val selectedNotesPositions: List<Int> = notesRecyclerAdapter.getSelectedItems()
            if (selectedNotesPositions.isEmpty()) {
                return mutableListOf()
            }
            val selectedNotes: MutableList<NoteItem> = mutableListOf()
            val selectedNotesPairs: MutableList<Pair<NoteItem, Int>> = mutableListOf()
            for (i in selectedNotesPositions) {
                val note = notesRecyclerAdapter.getItem(i)
                selectedNotes.add(note)
                selectedNotesPairs.add(Pair(note, i))
            }
            for (note in selectedNotes) {
                vm.removeNote(note)
                
            }
            return selectedNotesPairs
        }
        
    }
    
    companion object {
        const val TAG = "MainActivity"
    }
    
}