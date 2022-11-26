package com.pandacorp.notesui.presentation.activity_note.controllers

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.github.dhaval2404.imagepicker.ImagePicker
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.usecases.notes.UpdateNoteUseCase
import com.pandacorp.notesui.databinding.ActivityNoteBinding
import com.pandacorp.notesui.databinding.MenuDrawerEndBinding
import com.pandacorp.notesui.presentation.activity_note.NoteActivity
import com.pandacorp.notesui.presentation.adapter.ImagesRecyclerAdapter
import com.pandacorp.notesui.utils.ThemeHandler
import com.pandacorp.notesui.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class InitSlidingDrawerMenuСontroller(
    private val updateNoteUseCase: UpdateNoteUseCase
) {
    private val TAG = "NoteActivity"
    
    private lateinit var context: Context
    private lateinit var activity: NoteActivity
    private lateinit var note: NoteItem
    private lateinit var noteBinding: ActivityNoteBinding
    private lateinit var menuBinding: MenuDrawerEndBinding
    
    private lateinit var pickImageResult: ActivityResultLauncher<Intent>
    operator fun invoke(
        context: Context,
        activity: NoteActivity,
        note: NoteItem,
        noteBinding: ActivityNoteBinding,
        pickImageResult: ActivityResultLauncher<Intent>
    
    ) {
        this.context = context
        this.activity = activity
        this.note = note
        this.noteBinding = noteBinding
        this.menuBinding = noteBinding.drawerMenuInclude
        this.pickImageResult = pickImageResult
        
        menuBinding.expandChangeBackgroundButton.setOnClickListener {
            if (menuBinding.changeBackgroundExpandableLayout.isExpanded) {
                menuBinding.changeBackgroundExpandableLayout.collapse()
            } else menuBinding.changeBackgroundExpandableLayout.expand()
            
        }
        
        menuBinding.drawerMenuSelectButton.setOnClickListener {
            ImagePicker.with(activity = activity)
                .crop(9f, 16f)
                .createIntent {
                    Log.d(TAG, "invoke: createIntent")
                    pickImageResult.launch(it)
                
                }
            
            
        }
        menuBinding.drawerMenuResetButton.setOnClickListener() {
            val colorBackground = ContextCompat.getColor(context, ThemeHandler.getThemeColor(context = context, colorType = ThemeHandler.BACKGROUND_COLOR))
            noteBinding.contentActivityInclude.noteBackgroundImageView.setImageDrawable(ColorDrawable(colorBackground))
            CoroutineScope(Dispatchers.IO).launch {
                note.background = colorBackground.toString()
                updateNoteUseCase(note)
        
            }
            
        }
        
        initImageRecyclerView()
        
    }
    
    private fun initImageRecyclerView() {
        val imagesList = fillImagesList()
        val imageRecyclerAdapter = ImagesRecyclerAdapter(context, imagesList)
        imageRecyclerAdapter.setOnClickListener(object : ImagesRecyclerAdapter.OnClickListener {
            override fun onItemClick(view: View?, drawable: Drawable, position: Int) {
                // Here we store int as background, then get drawable by position
                // from Utils.backgroundImagesIds and set it.
                noteBinding.contentActivityInclude.noteBackgroundImageView.setImageDrawable(drawable)
                note.background = position.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    updateNoteUseCase(note)
                    
                }
                
            }
            
            override fun onItemLongClick(view: View?, drawable: Drawable, position: Int) {
            
            }
        })
        menuBinding.imageRecyclerView.adapter = imageRecyclerAdapter
        
    }
    
    private fun fillImagesList(): MutableList<Drawable> {
        val imagesList = mutableListOf<Drawable>()
        
        for (drawableResId in Utils.backgroundImagesIds) {
            imagesList.add(ContextCompat.getDrawable(context, drawableResId)!!)
            
        }
        return imagesList
    }
    
}