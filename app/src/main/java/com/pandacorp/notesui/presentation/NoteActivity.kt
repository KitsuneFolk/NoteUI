package com.pandacorp.notesui.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.usecases.GetDatabaseItemByAdapterPositionUseCase
import com.pandacorp.domain.usecases.GetDatabaseItemsUseCase
import com.pandacorp.domain.usecases.UpdateItemInDatabaseUseCase
import com.pandacorp.notesui.databinding.ActivityNoteBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

class NoteActivity : AppCompatActivity() {
    private val TAG = "NoteActivity"
    private lateinit var binding: ActivityNoteBinding
    
    private val getDatabaseItemByAdapterPositionUseCase: GetDatabaseItemByAdapterPositionUseCase by inject()
    private val getDatabaseItemsUseCase: GetDatabaseItemsUseCase by inject()
    private val updateItemInDatabaseUseCase: UpdateItemInDatabaseUseCase by inject()
    
    private lateinit var databaseList: MutableList<ListItem>
    private lateinit var note: ListItem
    private var notePositionInAdapter: Int? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        CoroutineScope(Dispatchers.Main).launch {
            initViews()
            
        }
        
        
    }
    
    private suspend fun initViews() {
        databaseList = withContext(Dispatchers.IO) {
            getDatabaseItemsUseCase()
        }
        val lastNote = databaseList.size-1
        notePositionInAdapter = intent.getIntExtra(intentNotePositionInAdapter, lastNote)
        
        Log.d(TAG, "onCreate: notePositionInAdapter = $notePositionInAdapter")
        
        note = databaseList[notePositionInAdapter!!]
        binding.noteHeaderEditText.setText(note.header)
        binding.noteContentEditText.setText(note.content)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setIntentResult()
            CoroutineScope(Dispatchers.IO).launch {
                updateItemInDatabaseUseCase(note)
            }
            finish()
        }
        return true
    }
    
    private fun setIntentResult(){
        //This function sets result and puts data about note: Header and Content,
        //So in MainActivity we can update the note.
        note.header = binding.noteHeaderEditText.text.toString()
        note.content = binding.noteContentEditText.text.toString()
        Log.d(TAG, "setIntentResult: notePositionInAdapter = $notePositionInAdapter")
        val resultIntent = Intent()
        resultIntent.putExtra(intentNotePositionInAdapter, notePositionInAdapter)
        resultIntent.putExtra(intentNoteHeader, note.header)
        resultIntent.putExtra(intentNoteContent, note.content)
        setResult(RESULT_OK, resultIntent)
    
    }
    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        setIntentResult()
        super.onBackPressed()
        
    }
    
    companion object {
        const val intentNotePositionInAdapter = "notePositionInAdapter"
        const val intentNoteHeader = "noteHeader"
        const val intentNoteContent = "intentNoteContent"
        
    }
}