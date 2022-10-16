package com.pandacorp.notesui.presentation

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.pandacorp.domain.usecases.AddToDatabaseUseCase
import com.pandacorp.domain.usecases.GetDatabaseItemByAdapterPositionUseCase
import com.pandacorp.domain.usecases.GetDatabaseItemsUseCase
import com.pandacorp.domain.usecases.RemoveFromDatabaseUseCase
import com.pandacorp.notesui.R
import com.pandacorp.notesui.presentation.adapter.CustomAdapter
import com.pandacorp.notesui.presentation.settings.SettingsActivity
import com.pandacorp.notesui.utils.ThemeHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    
    private lateinit var recyclerView: RecyclerView
    lateinit var customAdapter: CustomAdapter
    private lateinit var notesList: MutableList<ListItem>
    private var actionModeCallback: ActionModeCallback = ActionModeCallback()
    private var actionMode: ActionMode? = null
    
    private lateinit var addFloatingActionButton: FloatingActionButton
    
    
    //Clean Architecture
    private val addToDatabaseUseCase: AddToDatabaseUseCase by inject()
    
    private val getDatabaseItemByAdapterPositionUseCase: GetDatabaseItemByAdapterPositionUseCase by inject()
    private val getDatabaseItemsUseCase: GetDatabaseItemsUseCase by inject()
    private val removeFromDatabaseUseCase: RemoveFromDatabaseUseCase by inject()
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHandler.load(this)
        setContentView(R.layout.activity_main)
        initViews()
        
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view)
        
    }
    
    override fun onResume() {
        super.onResume()
        CoroutineScope(Dispatchers.Main).launch {
            
            initRecyclerView()
            initAddFloatingActionButton()
        }
    }
    
    private fun initAddFloatingActionButton() {
        addFloatingActionButton = findViewById(R.id.addFloatingActionButton)
        addFloatingActionButton.setOnClickListener {
            val listItem = ListItem(content = "Header", header = "Content")
            val position = 0
            notesList.add(position, listItem)
            customAdapter.notifyItemInserted(position)
            customAdapter.notifyItemRangeChanged(position, notesList.size)
            addToDatabaseUseCase(listItem)
        }
    }
    
    private suspend fun initRecyclerView() {
        notesList = getDatabaseItemsUseCase()
        customAdapter = CustomAdapter(this, notesList)
        customAdapter.setOnClickListener(object : CustomAdapter.OnClickListener {
            override fun onItemClick(view: View?, item: ListItem, pos: Int) {
                if (customAdapter.getSelectedItemCount() > 0) {
                    enableActionMode(pos)
                } else {
                    // val item: ListItem = customAdapter.getItem(pos)
                    
                }
            }
            
            override fun onItemLongClick(view: View?, item: ListItem, pos: Int) {
                enableActionMode(pos)
            }
        })
        recyclerView.adapter = customAdapter
        registerForContextMenu(recyclerView)
        
        
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
        
        for (item in notesList) {
            if (item.header.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        if (filteredList.isEmpty()) {
        
        } else {
            customAdapter.filterList(filteredList)
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
        if (selectedItemPositions.isEmpty()) return
        for (i in selectedItemPositions.indices.reversed()) {
            customAdapter.removeItem(selectedItemPositions[i])
            CoroutineScope(Dispatchers.IO).launch {
                val listItem =
                    getDatabaseItemByAdapterPositionUseCase(selectedItemPositions[i])
                removeFromDatabaseUseCase(listItem)
                
            }
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
    
}