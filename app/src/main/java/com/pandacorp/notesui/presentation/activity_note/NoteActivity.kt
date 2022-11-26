package com.pandacorp.notesui.presentation.activity_note

import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.GravityInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
import com.pandacorp.notesui.databinding.ContentActivityNoteBinding
import com.pandacorp.notesui.databinding.MenuDrawerEndBinding
import com.pandacorp.notesui.presentation.activity_note.controllers.InitSlidingDrawerMenuСontroller
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
    private lateinit var bindingContent: ContentActivityNoteBinding
    private lateinit var bindingDrawerMenuNoteEndDrawerBinding: MenuDrawerEndBinding
    
    private val initSlidingDrawerMenuController: InitSlidingDrawerMenuСontroller by inject()
    
    private val getNotesUseCase: GetNotesUseCase by inject()
    private val updateNoteUseCase: UpdateNoteUseCase by inject()
    
    private val vm: NoteViewModel by viewModel()
    
    private lateinit var databaseList: MutableList<NoteItem>
    private lateinit var note: NoteItem
    private var notePositionInAdapter: Int? = null
    
    private lateinit var colorsRecyclerAdapter: ColorsRecyclerAdapter
    
    private enum class ClickedActionButtonState {
        NULL,
        FOREGROUND_COLOR,
        BACKGROUND_COLOR
    }
    
    private var clickedActionButtonState: ClickedActionButtonState = ClickedActionButtonState.NULL
    
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
        ThemeHandler.load(this)
        binding = ActivityNoteBinding.inflate(layoutInflater)
        bindingContent = binding.contentActivityInclude
        bindingDrawerMenuNoteEndDrawerBinding = binding.drawerMenuInclude
        setContentView(binding.root)
        Utils.setupExceptionHandler()
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        CoroutineScope(Dispatchers.Main).launch {
            initViews()
            initEditTexts()
            initRecyclerView()
            initActionBottomMenu()
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
        //Here we get note by position what we get from intent.
        val lastNote = databaseList.size - 1
        notePositionInAdapter = intent.getIntExtra(intentNotePositionInAdapter, lastNote)
        note = databaseList[notePositionInAdapter!!]
        
        try {
            Log.d(TAG, "initViews: position = ${note.background.toInt()}")
            val drawableResId = Utils.backgroundImagesIds[note.background.toInt()]
            val drawable = ContextCompat.getDrawable(this, drawableResId)
            bindingContent.noteBackgroundImageView.setImageDrawable(drawable)
        } catch (e: Exception) {
            // In case if note.background is a chosen from storage image.
            bindingContent.noteBackgroundImageView.setImageURI(Uri.parse(note.background))
            
        }
        
        // Change action menu linear layout background tint, don't change background,
        // there is a drawable with corners.
        bindingContent.actionMenuParentLayout.backgroundTintList =
            ResourcesCompat.getColorStateList(
                    resources,
                    ThemeHandler.getThemeColor(this, ThemeHandler.PRIMARY_COLOR),
                    null)
        
        
    }
    
    private fun initEditTexts() {
        
        val headerSpannable = Utils.jsonToSpannable(note.header)
        
        val contentSpannable = Utils.jsonToSpannable(note.content)
        bindingContent.headerEditText.setText(headerSpannable)
        bindingContent.contentEditText.setText(contentSpannable)
        
        bindingContent.contentEditText.setOnFocusChangeListener { v, hasFocus ->
            bindingContent.actionMenuParentLayout.visibility = when (hasFocus) {
                true -> View.VISIBLE
                false -> View.GONE
            }
        }
        
    }
    
    private fun initActionBottomMenu() {
        
        bindingContent.actionMenuGravityLeftImageView.setOnClickListener {
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
            changeTextGravity(
                    Gravity.LEFT,
                    bindingContent.contentEditText.selectionStart)
            bindingContent.contentEditText.setSelection(startSelection, endSelection)
        }
        bindingContent.actionMenuGravityCenterImageView.setOnClickListener {
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
            changeTextGravity(
                    Gravity.CENTER,
                    bindingContent.contentEditText.selectionStart)
            bindingContent.contentEditText.setSelection(startSelection, endSelection)
            
        }
        bindingContent.actionMenuGravityRightImageView.setOnClickListener {
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
            changeTextGravity(
                    Gravity.RIGHT,
                    bindingContent.contentEditText.selectionStart)
            bindingContent.contentEditText.setSelection(startSelection, endSelection)
            
        }
        bindingContent.actionMenuGravityCloseImageView.setOnClickListener {
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuGravityLinearLayout)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuGravityLinearLayout.visibility = View.GONE
            bindingContent.actionMenuLinearLayout.visibility = View.VISIBLE
            
        }
        bindingContent.actionMenuButtonChangeTextForegroundColor.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.FOREGROUND_COLOR
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuColorsLinearLayout)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuLinearLayout.visibility = View.GONE
            bindingContent.actionMenuColorsLinearLayout.visibility = View.VISIBLE
            
        }
        bindingContent.actionMenuButtonChangeTextGravity.setOnClickListener {
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuGravityLinearLayout)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuLinearLayout.visibility = View.GONE
            bindingContent.actionMenuGravityLinearLayout.visibility = View.VISIBLE
            
        }
        bindingContent.actionMenuButtonChangeTextBackgroundColor.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.BACKGROUND_COLOR
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuColorsLinearLayout)
            animation.addTarget(R.id.actionMenuColorsRecyclerView)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuLinearLayout.visibility = View.GONE
            bindingContent.actionMenuColorsLinearLayout.visibility = View.VISIBLE
            
            
        }
        
        bindingContent.actionMenuColorsRemoveImageView.setOnClickListener {
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
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
            bindingContent.contentEditText.setSelection(startSelection, endSelection)
            
            
        }
        bindingContent.noteActionColorsCloseImageButton.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.NULL
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuColorsLinearLayout)
            animation.addTarget(R.id.actionMenuColorsRecyclerView)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuColorsLinearLayout.visibility = View.GONE
            bindingContent.actionMenuLinearLayout.visibility = View.VISIBLE
            
        }
        
    }
    
    private fun initRecyclerView() {
        colorsRecyclerAdapter = ColorsRecyclerAdapter(this, mutableListOf())
        colorsRecyclerAdapter.setOnClickListener(object : ColorsRecyclerAdapter.OnClickListener {
            override fun onItemClick(view: View?, colorItem: ColorItem, position: Int) {
                val startPosition = bindingContent.contentEditText.selectionStart
                val endPosition = bindingContent.contentEditText.selectionEnd
                if (colorItem.type == ColorItem.ADD) {
                    //Add button clicked
                    ColorPickerDialog.Builder(this@NoteActivity)
                        .setTitle(resources.getString(R.string.alert_dialog_add_color))
                        .setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton(R.string.select,
                                ColorEnvelopeListener { envelope, fromUser ->
                                    val newColorItem = ColorItem(color = envelope.color)
                                    vm.addColor(newColorItem)
                                    
                                })
                        .setNegativeButton(
                                getString(android.R.string.cancel)
                        ) { dialogInterface, i -> dialogInterface.dismiss() }
                        .attachAlphaSlideBar(true)
                        .attachBrightnessSlideBar(true)
                        .setBottomSpace(12) // set a bottom space between the last slideBar and buttons.
                        .show()
                    bindingContent.contentEditText.setSelection(startPosition, endPosition)
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
                bindingContent.contentEditText.setSelection(startPosition, endPosition)
                
            }
            
            override fun onItemLongClick(view: View?, colorItem: ColorItem, position: Int) {
                if (colorItem.type != ColorItem.COLOR) return
                AlertDialog.Builder(this@NoteActivity, R.style.MaterialAlertDialog)
                    .setTitle(R.string.confirm_color_remove)
                    .setPositiveButton(R.string.remove) { dialog, which ->
                        vm.removeColor(colorItem)
                        colorsRecyclerAdapter.notifyDataSetChanged()
                        
                        
                    }
                    .setNegativeButton(getString(android.R.string.cancel)) { dialog, which ->
                        dialog.dismiss()
                        
                    }
                    .show()
                    .window!!.decorView.setBackgroundResource(R.drawable.alert_dialog_background)
                
                
            }
        })
        
        bindingContent.actionMenuColorsRecyclerView.adapter = colorsRecyclerAdapter
        vm.colorsList.observe(this@NoteActivity) {
            colorsRecyclerAdapter.setList(it)
        }
        
    }
    
    
    /**
     * This method changes selected text foreground color of contentEditText
     */
    private fun changeTextForegroundColor(
        @ColorInt foregroundColor: Int?,
        startPosition: Int,
        endPosition: Int
    ) {
        val resultText: Spannable = bindingContent.contentEditText.text.toSpannable()
        
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
        bindingContent.contentEditText.setText(resultText)
        
    }
    
    /**
     * This method changes selected text background color of contentEditText
     */
    private fun changeTextBackgroundColor(
        @ColorInt backgroundColor: Int?,
        startPosition: Int,
        endPosition: Int
    ) {
        val resultText: Spannable = bindingContent.contentEditText.text.toSpannable()
        
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
        bindingContent.contentEditText.setText(resultText)
        
    }
    
    /**
     * This method changes text gravity.
     */
    private fun changeTextGravity(
        @GravityInt gravity: Int,
        selectionStart: Int
    ) {
        val selectedLinePositions =
            getEditTextSelectedLineTextBySelection(
                    bindingContent.contentEditText,
                    selectionStart)
        
        val selectedLineStart = selectedLinePositions.first
        val selectedLineEnd = selectedLinePositions.second
        
        val resultText: Spannable = bindingContent.contentEditText.text.toSpannable()
        val spans: Array<AlignmentSpan> = resultText.getSpans(
                selectedLineStart, selectedLineEnd,
                AlignmentSpan::class.java)
        repeat(spans.count()) {
            resultText.removeSpan(spans[it])
        }
        
        
        when (gravity) {
            Gravity.LEFT -> {
                // If gravity == LEFT, then remove spans and back to normal state, we remove spans
                // at start, so this should be empty.
                resultText.setSpan(
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
                        selectedLineStart,
                        selectedLineEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            
            Gravity.CENTER -> {
                resultText.setSpan(
                        (AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)),
                        selectedLineStart,
                        selectedLineEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            Gravity.RIGHT -> {
                resultText.setSpan(
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                        selectedLineStart,
                        selectedLineEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                bindingContent.contentEditText.setText(resultText)
            }
        }
        bindingContent.contentEditText.setText(resultText)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return true
    }
    
    /**
     * @return pair of int with start text of line and.
     */
    private fun getEditTextSelectedLineTextBySelection(
        editText: EditText,
        selectionStart: Int
    ): Pair<Int, Int> {
        var selectedLineStart = -1
        var selectedLineEnd = -1
        if (selectionStart != -1) {
            val selectedLine = editText.layout.getLineForOffset(selectionStart)
            selectedLineStart = editText.layout.getLineStart(selectedLine)
            selectedLineEnd = editText.layout.getLineEnd(selectedLine)
        }
        return Pair(selectedLineStart, selectedLineEnd)
    }
    
    private fun updateNote() {
        val headerSpannableText = bindingContent.headerEditText.text.toSpannable()
        val contentSpannableText = bindingContent.contentEditText.text.toSpannable()
        
        val jsonHeader = Utils.spannableToJson(headerSpannableText)
        val jsonContent = Utils.spannableToJson(contentSpannableText)
        note.header = jsonHeader
        note.content = jsonContent
        
        Log.d(TAG, "updateNote: jsonHeader = $jsonHeader")
        Log.d(TAG, "updateNote: jsonContent = $jsonContent")
        
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
    
    companion object {
        const val intentNotePositionInAdapter = "notePositionInAdapter"
        
    }
}