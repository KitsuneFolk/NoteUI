package com.pandacorp.notesui.presentation

import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.toSpannable
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.usecases.notes.GetNotesUseCase
import com.pandacorp.domain.usecases.notes.UpdateNoteUseCase
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.ActivityNoteBinding
import com.pandacorp.notesui.presentation.adapter.ColorsRecyclerAdapter
import com.pandacorp.notesui.utils.ThemeHandler
import com.pandacorp.notesui.utils.Utils
import com.pandacorp.notesui.viewModels.NoteViewModel
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class NoteActivity : AppCompatActivity() {
    private val TAG = "NoteActivity"
    private lateinit var binding: ActivityNoteBinding
    
    private val getNotesUseCase: GetNotesUseCase by inject()
    private val updateNoteUseCase: UpdateNoteUseCase by inject()
    
    private val vm: NoteViewModel by viewModel()
    
    private lateinit var databaseList: MutableList<NoteItem>
    private lateinit var note: NoteItem
    private var notePositionInAdapter: Int? = null
    
    lateinit var colorsRecyclerAdapter: ColorsRecyclerAdapter
    private var colorsList = mutableListOf<ColorItem>()
    
    private enum class ClickedActionButtonState {
        NULL,
        FOREGROUND_COLOR,
        BACKGROUND_COLOR
    }
    
    private var clickedActionButtonState: ClickedActionButtonState = ClickedActionButtonState.NULL
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        ThemeHandler.load(this)
        setContentView(binding.root)
        Utils.setupExceptionHandler()
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        CoroutineScope(Dispatchers.Main).launch {
            initViews()
            
        }
        
        
    }
    
    private suspend fun initViews() {
        // Change action menu linear layout background tint, don't change background,
        // there is drawable with corners.
        binding.noteActionParent.backgroundTintList = ResourcesCompat.getColorStateList(
                resources,
                ThemeHandler.getThemeColor(this, ThemeHandler.PRIMARY_COLOR),
                null)
        databaseList = withContext(Dispatchers.IO) {
            getNotesUseCase()
        }
        //Here we get note by position, which we get from intent.
        val lastNote = databaseList.size - 1
        notePositionInAdapter = intent.getIntExtra(intentNotePositionInAdapter, lastNote)
        note = databaseList[notePositionInAdapter!!]
        
        initEditTexts()
        
        
    }
    
    private fun initEditTexts() {
        val headerTextWithoutNewLines = correctHTML(note.header)
        binding.noteHeaderEditText.setText(Html.fromHtml(headerTextWithoutNewLines))
        Log.d(TAG, "initEditTexts: headerTextWithoutNewLines = $headerTextWithoutNewLines")
        binding.noteContentEditText.setText(Html.fromHtml(note.content))
        
        initActionBottomMenu()
        
        
    }
    
    private fun initActionBottomMenu() {
        initRecyclerView()
        binding.noteActionChangeForegroundTextColor.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.FOREGROUND_COLOR
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.noteActionLinearLayout)
            animation.addTarget(R.id.noteActionColorsLinearLayout)
            animation.addTarget(R.id.noteActionColorsRecyclerView)
            TransitionManager.beginDelayedTransition(binding.noteActionParent, animation)
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
            animation.addTarget(R.id.noteActionColorsRecyclerView)
            TransitionManager.beginDelayedTransition(binding.noteActionParent, animation)
            binding.noteActionLinearLayout.visibility = View.GONE
            binding.noteActionColorsLinearLayout.visibility = View.VISIBLE
            
            
        }
        
        binding.noteActionColorsRemoveImageView.setOnClickListener {
            val startSelection = binding.noteContentEditText.selectionStart
            val endSelection = binding.noteContentEditText.selectionEnd
            when (clickedActionButtonState) {
                ClickedActionButtonState.NULL ->
                    throw Exception("clickedActionButtonState cannot be null when color buttons were clicked.")
                ClickedActionButtonState.FOREGROUND_COLOR ->
                    //Null color means remove
                    changeTextForegroundColor(null, startSelection, endSelection)
                ClickedActionButtonState.BACKGROUND_COLOR ->
                    //Null color means remove
                    changeTextBackgroundColor(null, startSelection, endSelection)
                
            }
            binding.noteContentEditText.setSelection(startSelection, endSelection)
            
            
        }
        binding.noteActionColorsCloseImageButton.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.NULL
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.noteActionLinearLayout)
            animation.addTarget(R.id.noteActionColorsLinearLayout)
            animation.addTarget(R.id.noteActionColorsRecyclerView)
            TransitionManager.beginDelayedTransition(binding.noteActionParent, animation)
            binding.noteActionColorsLinearLayout.visibility = View.GONE
            binding.noteActionLinearLayout.visibility = View.VISIBLE
            
        }
        
    }
    
    private fun initRecyclerView() {
        colorsRecyclerAdapter = ColorsRecyclerAdapter(this, mutableListOf())
        colorsRecyclerAdapter.setOnClickListener(object : ColorsRecyclerAdapter.OnClickListener {
            override fun onItemClick(view: View?, colorItem: ColorItem, position: Int) {
                val startPosition = binding.noteContentEditText.selectionStart
                val endPosition = binding.noteContentEditText.selectionEnd
                if (colorItem.type == ColorItem.ADD) {
                    //Add button clicked
                    ColorPickerDialog.Builder(this@NoteActivity)
                        .setTitle(resources.getString(R.string.alert_dialog_add_color))
                        .setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton(R.string.select,
                                ColorEnvelopeListener { envelope, fromUser ->
                                    val newColorItem = ColorItem(color = envelope.color)
                                    vm.addColor(newColorItem)
                                    Log.d(TAG, "onItemClick: colorItem.id = ${newColorItem.id}")
                                    
                                })
                        .setNegativeButton(
                                getString(android.R.string.cancel)
                        ) { dialogInterface, i -> dialogInterface.dismiss() }
                        .attachAlphaSlideBar(true)
                        .attachBrightnessSlideBar(true)
                        .setBottomSpace(12) // set a bottom space between the last slidebar and buttons.
                        .show()
                    binding.noteContentEditText.setSelection(startPosition, endPosition)
                    return
                }
                when (clickedActionButtonState) {
                    ClickedActionButtonState.NULL -> {}
                    ClickedActionButtonState.FOREGROUND_COLOR -> {
                        changeTextForegroundColor(colorItem.color, startPosition, endPosition)
                    }
                    ClickedActionButtonState.BACKGROUND_COLOR -> {
                        changeTextBackgroundColor(colorItem.color, startPosition, endPosition)
                    }
                }
                binding.noteContentEditText.setSelection(startPosition, endPosition)
                
            }
            
            override fun onItemLongClick(view: View?, colorItem: ColorItem, position: Int) {
                Log.d(TAG, "onItemLongClick: color id = ${colorItem.id}")
                if (colorItem.type != ColorItem.COLOR) return
                AlertDialog.Builder(this@NoteActivity)
                    .setTitle(R.string.confirm_color_remove)
                    .setPositiveButton(R.string.remove) { dialog, which ->
                        Log.d(TAG, "onItemLongClick: colorItem.id = ${colorItem.id}")
                        vm.removeColor(colorItem)
                        colorsRecyclerAdapter.notifyDataSetChanged()
                        
                        
                    }
                    .setNegativeButton(getString(android.R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                        
                    }
                    .show()
                
            }
        })
        
        binding.noteActionColorsRecyclerView.adapter = colorsRecyclerAdapter
        vm.colorsList.observe(this@NoteActivity) {
            colorsRecyclerAdapter.setList(it)
            
        }
        
    }
    
    /**
     * This method changes selected text foreground color of noteContentEditText
     */
    private fun changeTextForegroundColor(
        @ColorInt foregroundColor: Int?,
        startPosition: Int,
        endPosition: Int
    ) {
        val resultText: Spannable = binding.noteContentEditText.text.toSpannable()
        
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
            resultText.removeSpan(spans[it])
        }
        
        /**
         * Here we check if foregroundResColor != R.color.white then create span,
         * else do nothing.
         */
        if (foregroundColor != null) {
            resultText.setSpan(
                    ForegroundColorSpan(foregroundColor),
                    startPosition,
                    endPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            
        }
        binding.noteContentEditText.setText(resultText)
        
    }
    
    /**
     * This method changes selected text background color of noteContentEditText
     */
    private fun changeTextBackgroundColor(
        @ColorInt backgroundColor: Int?,
        startPosition: Int,
        endPosition: Int
    ) {
        val resultText: Spannable = binding.noteContentEditText.text.toSpannable()
        
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
            resultText.removeSpan(spans[it])
        }
        /**
         * Here we check if backgroundResColor != R.color.white then create span,
         * else do nothing.
         *
         **/
        if (backgroundColor != null) {
            resultText.setSpan(
                    BackgroundColorSpan(backgroundColor),
                    startPosition,
                    endPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            
        }
        binding.noteContentEditText.setText(resultText)
        
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }
    
    /**
     * @param rawHTML HTML with spaces and new lines.
     * @return HTML String without <p> tag, without <u> tag,
     * without space at the end of the String.
     * @exception e if there is no tags, that need to replace.
     */
    private fun correctHTML(rawHTML: String): String {
        val newLineFirstExpressionToRemove = "<p dir=\"ltr\">"
        val newLineSecondExpressionToRemove = "</p>"
        val underLineFirstExpressionToRemove = "<u>"
        val underLineSecondExpressionToRemove = "</u>"
        val b = StringBuilder(rawHTML)
        
        try {
            b.replace(
                    rawHTML.lastIndexOf(underLineFirstExpressionToRemove),
                    rawHTML.lastIndexOf(underLineFirstExpressionToRemove) + underLineFirstExpressionToRemove.length,
                    "")
            
        } catch (e: Exception) {
        }
        try {
            b.replace(
                    b.toString().lastIndexOf(underLineSecondExpressionToRemove),
                    b.toString()
                        .lastIndexOf(underLineSecondExpressionToRemove) + underLineSecondExpressionToRemove.length,
                    "")
        } catch (e: Exception) {
        }
        try {
            b.replace(
                    b.toString().lastIndexOf(newLineFirstExpressionToRemove),
                    b.toString()
                        .lastIndexOf(newLineFirstExpressionToRemove) + newLineFirstExpressionToRemove.length,
                    "")
            
        } catch (e: Exception) {
        }
        try {
            b.replace(
                    b.toString().lastIndexOf(newLineSecondExpressionToRemove),
                    b.toString()
                        .lastIndexOf(newLineSecondExpressionToRemove) + newLineSecondExpressionToRemove.length,
                    "")
        } catch (e: Exception) {
        }
        
        
        return b.toString().trim()
        
    }
    
    private fun updateNote() {
        val headerSpannableText = binding.noteHeaderEditText.text.toSpannable()
        val contentSpannableText = binding.noteContentEditText.text.toSpannable()
        
        note.header = correctHTML(Html.toHtml(headerSpannableText))
        note.content = correctHTML(Html.toHtml(contentSpannableText))
        
        
        CoroutineScope(Dispatchers.IO).launch {
            updateNoteUseCase(note)
            
        }
        
        
    }
    
    override fun onPause() {
        Log.d(TAG, "onPause: ")
        binding.noteHeaderEditText.isSelected = false
        updateNote()
        super.onPause()
    }
    
    companion object {
        const val intentNotePositionInAdapter = "notePositionInAdapter"
        
    }
}