package com.pandacorp.notesui.presentation

import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.annotation.ColorRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.pandacorp.domain.models.ListItem
import com.pandacorp.domain.usecases.GetDatabaseItemByAdapterPositionUseCase
import com.pandacorp.domain.usecases.GetDatabaseItemsUseCase
import com.pandacorp.domain.usecases.UpdateItemInDatabaseUseCase
import com.pandacorp.notesui.R
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
    
    private enum class ClickedActionButtonState {
        NULL,
        FOREGROUND_COLOR,
        BACKGROUND_COLOR
    }
    
    private var clickedActionButtonState: ClickedActionButtonState = ClickedActionButtonState.NULL
    
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
        val lastNote = databaseList.size - 1
        notePositionInAdapter = intent.getIntExtra(intentNotePositionInAdapter, lastNote)
        
        Log.d(TAG, "onCreate: notePositionInAdapter = $notePositionInAdapter")
        
        note = databaseList[notePositionInAdapter!!]
        
        initEditTexts()
        
        
    }
    
    private fun initEditTexts() {
        binding.noteHeaderEditText.setText(Html.fromHtml(note.header))
        Log.d(TAG, "initViews: note.content = ${Html.fromHtml(note.content)}")
        binding.noteContentEditText.setText(Html.fromHtml(note.content))
        
        initActionBottomMenu()
        
        
    }
    
    private fun initActionBottomMenu() {
        binding.noteActionChangeForegroundTextColor.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.FOREGROUND_COLOR
    
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.noteActionLinearLayout)
            animation.addTarget(R.id.noteActionColorsLinearLayout)
            TransitionManager.beginDelayedTransition(binding.noteActionCardView, animation)
            binding.noteActionLinearLayout.visibility = View.GONE
            binding.noteActionColorsLinearLayout.visibility = View.VISIBLE
            
        }
        binding.noteActionChangeTextBackgroundColor.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.BACKGROUND_COLOR
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.noteActionLinearLayout)
            animation.addTarget(R.id.noteActionColorsLinearLayout)
            TransitionManager.beginDelayedTransition(binding.noteActionCardView, animation)
            binding.noteActionLinearLayout.visibility = View.GONE
            binding.noteActionColorsLinearLayout.visibility = View.VISIBLE
    
            
            
        }
        
        binding.noteActionColorsWhiteCardView.setOnClickListener {
            val startSelection = binding.noteContentEditText.selectionStart
            val endSelection: Int = binding.noteContentEditText.selectionEnd
            when (clickedActionButtonState) {
                ClickedActionButtonState.NULL ->
                    throw Exception("clickedActionButtonState cannot be null when color buttons were clicked.")
                ClickedActionButtonState.FOREGROUND_COLOR ->
                    changeTextForegroundColor(R.color.white, startSelection, endSelection)
                ClickedActionButtonState.BACKGROUND_COLOR ->
                    changeTextBackgroundColor(R.color.white, startSelection, endSelection)
                
            }
            
            
        }
        binding.noteActionColorsRedCardView.setOnClickListener {
            val startSelection = binding.noteContentEditText.selectionStart
            val endSelection: Int = binding.noteContentEditText.selectionEnd
            when (clickedActionButtonState) {
                ClickedActionButtonState.NULL ->
                    throw Exception("clickedActionButtonState cannot be null when color buttons were clicked.")
                ClickedActionButtonState.FOREGROUND_COLOR ->
                    changeTextForegroundColor(R.color.red, startSelection, endSelection)
                ClickedActionButtonState.BACKGROUND_COLOR ->
                    changeTextBackgroundColor(R.color.red, startSelection, endSelection)
                
            }
            
        }
        binding.noteActionColorsBlueCardView.setOnClickListener {
            val startSelection = binding.noteContentEditText.selectionStart
            val endSelection: Int = binding.noteContentEditText.selectionEnd
            when (clickedActionButtonState) {
                ClickedActionButtonState.NULL ->
                    throw Exception("clickedActionButtonState cannot be null when color buttons were clicked.")
                ClickedActionButtonState.FOREGROUND_COLOR ->
                    changeTextForegroundColor(R.color.blue, startSelection, endSelection)
                ClickedActionButtonState.BACKGROUND_COLOR ->
                    changeTextBackgroundColor(R.color.blue, startSelection, endSelection)
                
            }
            
        }
        binding.noteActionColorsCloseImageButton.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.NULL
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.noteActionLinearLayout)
            animation.addTarget(R.id.noteActionColorsLinearLayout)
            TransitionManager.beginDelayedTransition(binding.noteActionCardView, animation)
            binding.noteActionColorsLinearLayout.visibility = View.GONE
            binding.noteActionLinearLayout.visibility = View.VISIBLE
            
        }
        
    }
    
    /**
     * This method changes selected text foreground color of noteContentEditText
     */
    private fun changeTextForegroundColor(
        @ColorRes foregroundResColor: Int,
        startPosition: Int,
        endPosition: Int
    ) {
        val resultText: Spannable = binding.noteContentEditText.text.toSpannable()
        
        val foregroundColor = ContextCompat.getColor(this, foregroundResColor)
        
        
        /*
          Here we get spanned text of resultText by startPosition and endPosition and then
          We delete it if there is spanned text, it needs to avoid bug when we convert spanned
          Text to html, where can be a few tags like:
          (White) (Red) "Hello" (/Red) (/White)
        */
        val spans: Array<ForegroundColorSpan> = resultText.getSpans(
                startPosition, endPosition,
                ForegroundColorSpan::class.java)
        repeat(spans.count()) {
            Log.d(TAG, "changeTextColor: removed span at position $it")
            Log.d(TAG, "changeTextColor: removed span = ${spans[it]}")
            resultText.removeSpan(spans[it])
        }
        
        // Here we check if foregroundResColor != R.color.white then create span,
        // else do nothing.
        if (foregroundResColor != R.color.white) {
            resultText.setSpan(
                    ForegroundColorSpan(foregroundColor),
                    startPosition,
                    endPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            
        }
        Log.d(TAG, "changeTextColor: html = ${Html.toHtml(resultText)}")
        binding.noteContentEditText.setText(resultText)
        
    }
    
    /**
     * This method changes selected text background color of noteContentEditText
     */
    private fun changeTextBackgroundColor(
        @ColorRes backgroundResColor: Int,
        startPosition: Int,
        endPosition: Int
    ) {
        val resultText: Spannable = binding.noteContentEditText.text.toSpannable()
        
        val backgroundColor = ContextCompat.getColor(this, backgroundResColor)
        
        /*
         Here we get spanned text of resultText by startPosition and endPosition and then
         We delete it if there is spanned text, it needs to avoid bug when we convert spanned
         Text to html, where can be a few tags like:
         (White) (Red) "Hello" (/Red) (/White)
        */
        
        val spans: Array<BackgroundColorSpan> = resultText.getSpans(
                startPosition, endPosition,
                BackgroundColorSpan::class.java)
        repeat(spans.count()) {
            Log.d(TAG, "changeBackgroundTextColor: removed span at position $it")
            Log.d(TAG, "changeBackgroundTextColor: removed span = ${spans[it]}")
            resultText.removeSpan(spans[it])
        }
        /**
         * Here we check if backgroundResColor != R.color.white then create span,
         * else do nothing.
         *
         **/
        if (backgroundResColor != R.color.white) {
            resultText.setSpan(
                    BackgroundColorSpan(backgroundColor),
                    startPosition,
                    endPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            
        }
        Log.d(TAG, "changeTextColor: html = ${Html.toHtml(resultText)}")
        binding.noteContentEditText.setText(resultText)
        
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            updateNote()
            finish()
        }
        return true
    }
    
    private fun updateNote() {
        val headerSpannableText = binding.noteHeaderEditText.text.toSpannable()
        val contentSpannableText = binding.noteContentEditText.text.toSpannable()
        
        note.header = Html.toHtml(headerSpannableText)
        note.content = Html.toHtml(contentSpannableText)
        
        Log.d(TAG, "setIntentResult: notePositionInAdapter = $notePositionInAdapter")
        Log.d(TAG, "updateNote: header = ${note.header} ")
        Log.d(TAG, "updateNote: content = ${note.content} ")
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "updateNote: item.header = ${note.header}")
            Log.d(TAG, "updateNote: item.content = ${note.content}")
            updateItemInDatabaseUseCase(note)
            
        }
        
    }
    
    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: ")
        updateNote()
        super.onBackPressed()
        
    }
    
    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        updateNote()
        super.onDestroy()
    }
    
    companion object {
        const val intentNotePositionInAdapter = "notePositionInAdapter"
        
    }
}