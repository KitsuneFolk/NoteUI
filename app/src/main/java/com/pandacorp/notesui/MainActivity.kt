package com.pandacorp.notesui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.pandacorp.notesui.adapter.CustomAdapter
import com.pandacorp.notesui.adapter.ListItem
import com.pandacorp.notesui.settings.SettingsActivity
import com.pandacorp.notesui.settings.ThemeHandler

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var dbHelper: DBHelper
    
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
    }
    private fun initRecyclerView(){
        val notesList = dbHelper.getDatabaseItems()
        notesList.add(ListItem("Test", "TestContent"))
        notesList.add(ListItem("Test", "TestContent"))
        notesList.add(ListItem("Test", "TestContent"))
        notesList.add(ListItem("Test", "TestContent"))
        val customAdapter = CustomAdapter(this, notesList)
        recyclerView.adapter = customAdapter
        registerForContextMenu(recyclerView)
        
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    
    var resultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(0, 0)
        
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> {
                resultLauncher.launch(Intent(this, SettingsActivity::class.java))
                
            }
        }
        
        return true
    }
}