package com.pandacorp.notesui.presentation

import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.toSpannable
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.usecases.notes.SetNoteBackgroundUseCase
import com.pandacorp.domain.usecases.notes.database.GetNotesUseCase
import com.pandacorp.domain.usecases.notes.database.UpdateNoteUseCase
import com.pandacorp.domain.usecases.utils.JsonToSpannableUseCase
import com.pandacorp.domain.usecases.utils.SpannableToJsonUseCase
import com.pandacorp.notesui.R
import com.pandacorp.notesui.controllers.InitActionBottomMenuController
import com.pandacorp.notesui.controllers.InitSlidingDrawerMenuController
import com.pandacorp.notesui.databinding.ActivityNoteBinding
import com.pandacorp.notesui.databinding.ContentActivityNoteBinding
import com.pandacorp.notesui.databinding.MenuDrawerEndBinding
import com.pandacorp.notesui.utils.ThemeHandler
import com.pandacorp.notesui.utils.UndoRedoHelper
import com.pandacorp.notesui.utils.Utils
import com.pandacorp.notesui.viewModels.NoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class NoteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteBinding
    private lateinit var bindingContent: ContentActivityNoteBinding
    private lateinit var bindingDrawerMenuNoteEndDrawerBinding: MenuDrawerEndBinding
    
    private val initActionBottomMenuController: InitActionBottomMenuController by inject()
    private val initSlidingDrawerMenuController: InitSlidingDrawerMenuController by inject()
    
    private val getNotesUseCase: GetNotesUseCase by inject()
    private val updateNoteUseCase: UpdateNoteUseCase by inject()
    
    private val setNoteBackgroundUseCase: SetNoteBackgroundUseCase by inject()
    
    private val vm: NoteViewModel by viewModel()
    
    private val spannableToJsonUseCase: SpannableToJsonUseCase by inject()
    private val jsonToSpannableUseCase: JsonToSpannableUseCase by inject()
    private lateinit var databaseList: MutableList<NoteItem>
    private lateinit var note: NoteItem
    private var notePositionInAdapter: Int? = null
    
    private lateinit var undoRedoContentEditTextHelper: UndoRedoHelper
    private lateinit var undoRedoHeaderEditTextHelper: UndoRedoHelper
    
    private val pickImageResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val imageUri = it.data!!.data
            
            bindingContent.noteBackgroundImageView.setImageURI(imageUri)
            note.background = imageUri.toString()
            CoroutineScope(Dispatchers.IO).launch {
                updateNoteUseCase.invoke(note)
                
            }
        }
        
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHandler(this).load()
        binding = ActivityNoteBinding.inflate(layoutInflater)
        bindingContent = binding.contentActivityInclude
        bindingDrawerMenuNoteEndDrawerBinding = binding.drawerMenuInclude
        setContentView(binding.root)
        Utils.setupExceptionHandler()
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        CoroutineScope(Dispatchers.Main).launch {
            initViews()
            initEditTexts()
            initActionBottomMenuController(
                    context = this@NoteActivity,
                    activity = this@NoteActivity,
                    note = note,
                    noteBinding = binding,
                    vm = vm)
            initSlidingDrawerMenuController(
                    context = this@NoteActivity,
                    activity = this@NoteActivity,
                    note = note,
                    noteBinding = binding,
                    pickImageResult = pickImageResult
            )
            
        }
        
        
    }
    
    private suspend fun initViews() {
        databaseList = withContext(Dispatchers.IO) {
            getNotesUseCase()
            
        }
        // Here we get note by position what we get from intent.
        val lastNote = databaseList.size - 1
        notePositionInAdapter = intent.getIntExtra(intentNotePositionInAdapter, lastNote)
        note = databaseList[notePositionInAdapter!!]
        setNoteBackgroundUseCase(
                note,
                Utils.backgroundImages,
                bindingContent.noteBackgroundImageView)
        
        // When activity starts, don't show keyboard.
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        
    }
    
    private fun initEditTexts() {
        
        val headerSpannable = jsonToSpannableUseCase(note.header)
        
        val contentSpannable = jsonToSpannableUseCase(note.content)
        
        bindingContent.headerEditText.setText(headerSpannable)
        bindingContent.contentEditText.setText(contentSpannable)
        
        undoRedoContentEditTextHelper =
            UndoRedoHelper(binding.contentActivityInclude.contentEditText)
        undoRedoHeaderEditTextHelper = UndoRedoHelper(binding.contentActivityInclude.headerEditText)
        
        bindingContent.contentEditText.setOnFocusChangeListener { v, hasFocus ->
            invalidateOptionsMenu()
        }
        bindingContent.headerEditText.setOnFocusChangeListener { v, hasFocus ->
            invalidateOptionsMenu()
        }
        
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        when (bindingContent.headerEditText.isFocused || bindingContent.contentEditText.isFocused) {
            // Show undo and redo button if edittext is focused.
            true -> {
                menuInflater.inflate(R.menu.menu_note_extended, menu)
                
            }
            
            // Show menu hamburger icon if edittext is not focused.
            false -> menuInflater.inflate(R.menu.menu_note, menu)
            
        }
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                
            }
            R.id.menu_note_hamburger -> {
                if (binding.drawerMenu.isDrawerOpen(Gravity.RIGHT)) {
                    binding.drawerMenu.closeDrawer(Gravity.RIGHT)
                    binding.contentMotionLayout.transitionToStart()
                } else {
                    binding.drawerMenu.openDrawer(Gravity.RIGHT)
                    binding.contentMotionLayout.transitionToEnd()
                }
                
            }
            
            R.id.menu_note_extended_undo -> {
                if (bindingContent.headerEditText.hasFocus()) {
                    if (undoRedoHeaderEditTextHelper.canUndo) {
                        undoRedoHeaderEditTextHelper.undo()
                        
                    }
                    
                }
                if (bindingContent.contentEditText.hasFocus()) {
                    if (undoRedoContentEditTextHelper.canUndo) {
                        undoRedoContentEditTextHelper.undo()
                        
                    }
                    
                }
            }
            R.id.menu_note_extended_redo -> {
                if (bindingContent.headerEditText.hasFocus()) {
                    if (undoRedoHeaderEditTextHelper.canRedo) {
                        undoRedoHeaderEditTextHelper.redo()
                        
                    }
                    
                }
                if (bindingContent.contentEditText.hasFocus()) {
                    if (undoRedoContentEditTextHelper.canRedo) {
                        undoRedoContentEditTextHelper.redo()
                        
                    }
                    
                }
                
            }
        }
        return true
    }
    
    private fun updateNote() {
        val headerSpannableText = bindingContent.headerEditText.text ?: ""
        val contentSpannableText = bindingContent.contentEditText.text ?: ""
        val jsonHeader = spannableToJsonUseCase(headerSpannableText.toSpannable())
        note.header = jsonHeader
        val jsonContent = spannableToJsonUseCase(contentSpannableText.toSpannable())
        note.content = jsonContent
        
        
        CoroutineScope(Dispatchers.IO).launch {
            updateNoteUseCase(note)
            
        }
        
    }
    
    override fun onPause() {
        try {
            updateNote()
            
        } catch (e: UninitializedPropertyAccessException) {
            //When app was stopped, and then user rotates the phone in another app, it can cause
            //Exception.
        }
        super.onPause()
    }
    
    override fun onStop() {
        super.onStop()
        // Clear edittext focuses
        bindingContent.headerEditText.clearFocus()
        bindingContent.contentEditText.clearFocus()
    }
    
    companion object {
        const val TAG = "NoteActivity"
        const val intentNotePositionInAdapter = "notePositionInAdapter"
        
    }
}