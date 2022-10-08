package com.pandacorp.notesui

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pandacorp.notesui.adapter.CustomAdapter
import com.pandacorp.notesui.adapter.ListItem
import com.pandacorp.notesui.settings.SettingsActivity
import com.pandacorp.notesui.settings.ThemeHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*


class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var recyclerView: RecyclerView
    private lateinit var customAdapter: CustomAdapter
    private lateinit var addFloatingActionButton: FloatingActionButton
    private lateinit var dbHelper: DBHelper
    private lateinit var notesList: MutableList<ListItem>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHandler.load(this)
        setContentView(R.layout.activity_main)
        
        initViews()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recycler_view)
        dbHelper = DBHelper(this, null)
        
    }
    
    override fun onResume() {
        super.onResume()
        initRecyclerView()
        initAddFloatingActionButton()
        
    }
    
    private fun initAddFloatingActionButton() {
        addFloatingActionButton = findViewById(R.id.addFloatingActionButton)
        addFloatingActionButton.setOnClickListener {
            val listItem = ListItem("Header", "Content")
            val position = 0
            notesList.add(position, listItem)
            customAdapter.notifyItemInserted(position)
            customAdapter.notifyItemRangeChanged(position, notesList.size)
            dbHelper.add(DBHelper.NOTES_TABLE, ListItem("Header", "Content"))
        }
    }
    
    private fun initRecyclerView() {
        notesList = dbHelper.getDatabaseItems()
        val testString: String = resources.getString(R.string.lorem_ipsum)
        notesList.add(ListItem("This text is very large, isn't it?", testString))
        notesList.add(ListItem("Test", "TestContent"))
        notesList.add(ListItem("Test", "TestContent"))
        notesList.add(ListItem("Test", "TestContent"))
        customAdapter = CustomAdapter(this, notesList)
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
        // creating a new array list to filter our data.
        val filteredlist: ArrayList<ListItem> = ArrayList<ListItem>()
        
        // running a for loop to compare elements.
        for (item in notesList) {
            // checking if the entered string matched with any item of our recycler view.
            if (item.header.lowercase().contains(text.lowercase(Locale.getDefault()))) {
                // if the item is matched we are
                // adding it to our filtered list.
                filteredlist.add(item)
            }
        }
        if (filteredlist.isEmpty()) {
            // if no item is added in filtered list we are
            // displaying a toast message as no data found.
            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show()
        } else {
            // at last we are passing that filtered
            // list to our adapter class.
            customAdapter.filterList(filteredlist)
        }
    }
    
    var resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        Log.d(TAG, "resultLauncher: recreate")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(0, 0)
        
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                resultLauncher.launch(Intent(this, SettingsActivity::class.java))
                
            }
            R.id.menu_search -> {
            
            }
        }
        
        return true
    }
    
}