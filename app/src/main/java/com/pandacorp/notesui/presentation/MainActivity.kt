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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandacorp.domain.models.ListItem
import com.pandacorp.notesui.R
import com.pandacorp.notesui.presentation.adapter.CustomAdapter
import com.pandacorp.notesui.presentation.settings.SettingsActivity
import com.pandacorp.notesui.utils.ThemeHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*



class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    
    private lateinit var recyclerView: RecyclerView
    lateinit var customAdapter: CustomAdapter
    private var actionModeCallback: ActionModeCallback = ActionModeCallback()
    private var actionMode: ActionMode? = null
    
    private lateinit var addFAB: FloatingActionButton
    
    //Clean Architecture
    private val vm: MainViewModel by viewModel()
    
    private val updateCustomAdapterLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        
        try {
            //Here we update changed note.
            vm.update()
            customAdapter.notifyDataSetChanged()
            // val clickedItemPosition =
            //     it.data!!.getIntExtra(NoteActivity.intentNotePositionInAdapter, -1)
            // if (clickedItemPosition == -1) throw Exception("clickedItemPosition cannot be -1")
            // val header = it.data!!.getStringExtra(NoteActivity.intentNoteHeader)
            // val content = it.data!!.getStringExtra(NoteActivity.intentNoteContent)
            // vm.update(clickedItemPosition, header!!, content!!)
            // customAdapter.notifyItemChanged(clickedItemPosition)
            
            // Log.d(TAG, "updateCustomAdapterLauncher: note.header = $header")
            // Log.d(TAG, "updateCustomAdapterLauncher: note.content = $content")
            
            
        } catch (e: Exception) {
            //If NoteActivity screen was rotated and user backed to MainActivity
            // then we do nothing, we don't notify adapter about changes because in OnCreate
            // it will be created again.
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
        ExceptionHandler.setupExceptionHandler()
        ThemeHandler.load(this)
        setContentView(R.layout.activity_main)
        initViews()
        
    }
    
    private fun initViews() {
        CoroutineScope(Dispatchers.Main).launch {
            
            initRecyclerView()
            initAddFloatingActionButton()
        }
        
    }
    
    private fun initAddFloatingActionButton() {
        addFAB = findViewById(R.id.addFAB)
        addFAB.setOnClickListener {
            val listItem = ListItem(content = "", header = "")
            // val position = 0
            // customAdapter.notifyItemInserted(position)
            // val listSize = vm.notesList.value!!.size
            // customAdapter.notifyItemRangeChanged(position, listSize)
            CoroutineScope(Dispatchers.IO).launch {
                this@MainActivity.vm.addNote(listItem)
                
            }
            val intent = Intent(this, NoteActivity::class.java)
            updateCustomAdapterLauncher.launch(intent)
            
            
        }
    }
    
    private fun initRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view)
        customAdapter = CustomAdapter(this, mutableListOf<ListItem>())
        customAdapter.setOnClickListener(object : CustomAdapter.OnClickListener {
            override fun onItemClick(view: View?, item: ListItem, position: Int) {
                if (customAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(position)
                } else {
                    val intent = Intent(this@MainActivity, NoteActivity::class.java)
                    intent.putExtra(NoteActivity.intentNotePositionInAdapter, position)
                    updateCustomAdapterLauncher.launch(intent)
                    
                }
            }
            
            override fun onItemLongClick(view: View?, item: ListItem, position: Int) {
                enableActionMode(position)
                
            }
        })
        recyclerView.adapter = customAdapter
        registerForContextMenu(recyclerView)
        vm.notesList.observe(this) {
            customAdapter.setList(it)
            
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
        
        val filteredList: ArrayList<ListItem> = ArrayList<ListItem>()

        vm.notesList.observe(this){
            for (item in it) {
                if (item.header.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                    filteredList.add(item)
                }
            }
            if (filteredList.isEmpty()) {
                customAdapter.filterList(filteredList)
        
            } else {
                customAdapter.filterList(filteredList)
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
        customAdapter.toggleSelection(position)
        val count: Int = customAdapter.getSelectedItemCount()
        if (count == 0) {
            actionMode!!.finish()
        } else {
            actionMode!!.title = count.toString()
            actionMode!!.invalidate()
        }
    }
    
    private fun removeSelectedItems() {
        val selectedItemPositions: List<Int> = customAdapter.getSelectedItems()
        Log.d(
                TAG,
                "removeSelectedItems: selectedItemPositions.size = ${selectedItemPositions.size}")
        if (selectedItemPositions.isEmpty()) return
        for (i in selectedItemPositions) {
            vm.removeNote(customAdapter.getItem(i))
            
        }
        customAdapter.notifyDataSetChanged()
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
                removeSelectedItems()
                mode.finish()
                return true
            }
            if (id == R.id.recyclerview_menu_select_all) {
                customAdapter.selectAllItems()
                val count: Int = customAdapter.getSelectedItemCount()
                actionMode!!.title = count.toString()
                actionMode!!.invalidate()
            }
            return false
        }
        
        override fun onDestroyActionMode(mode: ActionMode) {
            customAdapter.clearSelections()
            actionMode = null
            
        }
        
    }
    
    object ExceptionHandler {
        fun setupExceptionHandler() {
            val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                val message = "Uncaught exception in thread ${thread.name}:\n"
                Log.e("AndroidRuntime", message, throwable)
                defaultUncaughtExceptionHandler?.uncaughtException(thread, throwable)
            }
        }
    }
    
}