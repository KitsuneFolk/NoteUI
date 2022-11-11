package com.pandacorp.notesui.presentation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.ActivityMainBinding
import com.pandacorp.notesui.presentation.adapter.NotesRecyclerAdapter
import com.pandacorp.notesui.presentation.settings.SettingsActivity
import com.pandacorp.notesui.utils.ThemeHandler
import com.pandacorp.notesui.utils.Utils
import com.pandacorp.notesui.viewModels.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    
    private lateinit var binding: ActivityMainBinding
    
    lateinit var notesRecyclerAdapter: NotesRecyclerAdapter
    private var actionModeCallback: ActionModeCallback = ActionModeCallback()
    private var actionMode: ActionMode? = null
    
    private val vm: MainViewModel by viewModel()
    
    private val updateCustomAdapterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        
        try {
            Log.d(TAG, "resultLauncher: ")
            vm.update()
            notesRecyclerAdapter.notifyDataSetChanged()
            
            
        } catch (e: Exception) {
            /**
             * If NoteActivity screen was rotated and user backed to MainActivity
             * then we do nothing, we don't notify adapter about changes because in onCreate
             * it will be created again.
             *
             **/
        }
        
        
    }
    private val resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            overridePendingTransition(0, 0)
        }
        
        
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.setupExceptionHandler()
        ThemeHandler.load(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
        
    }
    
    private fun initViews() {
        CoroutineScope(Dispatchers.Main).launch {
            initRecyclerView()
            initAddFloatingActionButton()
        }
        
    }
    
    private fun initAddFloatingActionButton() {
        binding.addFAB.setOnClickListener {
            val noteItem = NoteItem(content = "", header = "")
            // val position = 0
            // notesRecyclerAdapter.notifyItemInserted(position)
            // val listSize = vm.notesList.value!!.size
            // notesRecyclerAdapter.notifyItemRangeChanged(position, listSize)
            CoroutineScope(Dispatchers.IO).launch {
                this@MainActivity.vm.addNote(noteItem)
                
            }
            val intent = Intent(this, NoteActivity::class.java)
            updateCustomAdapterLauncher.launch(intent)
            
            
        }
    }
    
    private fun initRecyclerView() {
        notesRecyclerAdapter = NotesRecyclerAdapter(this, mutableListOf())
        notesRecyclerAdapter.setOnClickListener(object : NotesRecyclerAdapter.OnClickListener {
            override fun onItemClick(view: View?, item: NoteItem, position: Int) {
                if (notesRecyclerAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(position)
                } else {
                    val intent = Intent(this@MainActivity, NoteActivity::class.java)
                    intent.putExtra(NoteActivity.intentNotePositionInAdapter, position)
                    updateCustomAdapterLauncher.launch(intent)
                    // overridePendingTransition(R.anim.slide_out_up, R.anim.slide_in_up)
                    
                }
            }
            
            override fun onItemLongClick(view: View?, item: NoteItem, position: Int) {
                Log.d(TAG, "onItemLongClick: position = $position")
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
        menuInflater.inflate(R.menu.menu, menu)
        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.menu_search)!!.actionView as SearchView
        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))
        searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener,
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                
                return true
            }
            
            override fun onQueryTextChange(text: String?): Boolean {
                searchJob?.cancel()
                searchJob = CoroutineScope(Dispatchers.Main).launch {
                    if (text != null) {
                        filter(text)
                        
                    }
                }
                
                return false
            }
            
        })
        return true
    }
    
    private fun filter(text: String) {
        
        val filteredList: ArrayList<NoteItem> = ArrayList<NoteItem>()
        
        vm.notesList.observe(this) {
            for (item in it) {
                if (item.header.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                    filteredList.add(item)
                }
            }
            if (filteredList.isEmpty()) {
                notesRecyclerAdapter.filterList(filteredList)
                
            } else {
                notesRecyclerAdapter.filterList(filteredList)
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
    
    private fun removeSelectedItems(): Int {
        val selectedItemPositions: List<Int> = notesRecyclerAdapter.getSelectedItems()
        Log.d(
                TAG,
                "removeSelectedItems: selectedItemPositions.size = ${selectedItemPositions.size}")
        if (selectedItemPositions.isEmpty()) {
            return 0
        }
        for (i in selectedItemPositions) {
            vm.removeNote(notesRecyclerAdapter.getItem(i))
            
        }
        notesRecyclerAdapter.notifyDataSetChanged()
        return selectedItemPositions.size
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
                initSnackBar()
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
        
        private fun initSnackBar() {
            removeSelectedItems()
            val removedItemsCount = removeSelectedItems()
            if (removedItemsCount == 0) return
            val snackbar_undo_title = resources.getText(R.string.snackbar_undo_title)
                .toString() + " " + removedItemsCount.toString()
            // val snackbar_undo_button_title = resources.getText(R.string.snackbar_undo)
            val snackBarDuration = 4_000
            val snackBar = Snackbar.make(
                    binding.addFAB,
                    snackbar_undo_title,
                    snackBarDuration)
            
            // snackBar.setAction(snackbar_undo_button_title) {
            //
            // }
            snackBar.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            val colorAccent = ContextCompat.getColor(
                    this@MainActivity,
                    ThemeHandler.getThemeColor(this@MainActivity, ThemeHandler.ACCENT_COLOR))
            snackBar.setActionTextColor(colorAccent)
            snackBar.animationMode = Snackbar.ANIMATION_MODE_SLIDE
            snackBar.show()
        }
    }
    
    
}