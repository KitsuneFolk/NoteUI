package com.pandacorp.notesui.presentation.activities

import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.ColorInt
import androidx.annotation.GravityInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.preference.PreferenceManager
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.github.dhaval2404.imagepicker.ImagePicker
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.usecases.notes.database.GetNotesUseCase
import com.pandacorp.domain.usecases.notes.database.UpdateNoteUseCase
import com.pandacorp.domain.usecases.utils.HideToolbarWhileScrollingUseCase
import com.pandacorp.domain.usecases.utils.JsonToSpannableUseCase
import com.pandacorp.domain.usecases.utils.SpannableToJsonUseCase
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.ActivityNoteBinding
import com.pandacorp.notesui.databinding.ContentActivityNoteBinding
import com.pandacorp.notesui.databinding.MenuDrawerEndBinding
import com.pandacorp.notesui.presentation.adapter.ColorsRecyclerAdapter
import com.pandacorp.notesui.presentation.adapter.ImagesRecyclerAdapter
import com.pandacorp.notesui.presentation.settings.PreferencesKeys
import com.pandacorp.notesui.utils.ThemeHandler
import com.pandacorp.notesui.utils.UndoRedoHelper
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
    private lateinit var binding: ActivityNoteBinding
    private lateinit var bindingContent: ContentActivityNoteBinding
    private lateinit var menuBinding: MenuDrawerEndBinding
    
    private lateinit var toolbar: Toolbar
    private var isHideToolbarWhileScrolling: Boolean = true
    
    private lateinit var colorsRecyclerAdapter: ColorsRecyclerAdapter
    
    private val getNotesUseCase: GetNotesUseCase by inject()
    private val updateNoteUseCase: UpdateNoteUseCase by inject()
    private val hideToolbarWhileScrollingUseCase: HideToolbarWhileScrollingUseCase by inject()
    
    private val vm: NoteViewModel by viewModel()
    
    private val spannableToJsonUseCase: SpannableToJsonUseCase by inject()
    private val jsonToSpannableUseCase: JsonToSpannableUseCase by inject()
    
    private lateinit var note: NoteItem
    private var notePositionInAdapter: Int? = null
    
    private lateinit var undoRedoContentEditTextHelper: UndoRedoHelper
    private lateinit var undoRedoHeaderEditTextHelper: UndoRedoHelper
    
    // Enum class to watch what menu button was clicked,
    // foreground text color or background, or button clicked state is null.
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
                updateNoteUseCase(note)
                
            }
        }
        
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeHandler(this).load()
        binding = ActivityNoteBinding.inflate(layoutInflater)
        bindingContent = binding.contentActivityInclude
        menuBinding = binding.drawerMenuInclude
        setContentView(binding.root)
        Utils.setupExceptionHandler()
        toolbar = findViewById(R.id.noteToolbar)
        isHideToolbarWhileScrolling = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(PreferencesKeys.hideActionBarWhileScrollingKey, true)
        hideToolbarWhileScrollingUseCase(toolbar = toolbar, isHide = isHideToolbarWhileScrolling)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        CoroutineScope(Dispatchers.Main).launch {
            initViews()
            initEditTexts()
            initActionBottomMenu()
            initSlidingDrawerMenu()
            
        }
        
        
    }
    
    
    private suspend fun initViews() {
        val databaseList = withContext(Dispatchers.IO) {
            getNotesUseCase()
        }
        // Here we get note by position what we get from intent.
        val lastNote = databaseList.size - 1
        notePositionInAdapter = intent.getIntExtra(intentNotePositionInAdapter, lastNote)
        note = databaseList[notePositionInAdapter!!]
        
        changeNoteBackground(note.background)
        
        // When activity starts, don't show keyboard.
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        
    }
    
    private fun changeNoteBackground(
        background: String
    ) {
        val noteBackgroundImageView = bindingContent.noteBackgroundImageView
        try {
            // note.background is an image drawable from Utils.backgroundImages
            val drawableResId = Utils.backgroundImages[background.toInt()]
            val drawable = ContextCompat.getDrawable(this, drawableResId)
            noteBackgroundImageView.setImageDrawable(drawable)
        } catch (e: ArrayIndexOutOfBoundsException) {
            // note.background is a color.
            val colorBackground = ThemeHandler(this).getColorBackground()
            noteBackgroundImageView.background = ColorDrawable(colorBackground)
        } catch (e: NumberFormatException) {
            // note.background is a image from storage (uri)
            noteBackgroundImageView.setImageURI(Uri.parse(background))
            
        }
        
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
            if (isHideToolbarWhileScrolling) hideToolbarWhileScrollingUseCase(toolbar, !hasFocus)
            invalidateOptionsMenu()
        }
        bindingContent.headerEditText.setOnFocusChangeListener { v, hasFocus ->
            if (isHideToolbarWhileScrolling) hideToolbarWhileScrollingUseCase(toolbar, !hasFocus)
            invalidateOptionsMenu()
        }
        
    }
    
    private fun initActionBottomMenu() {
        
        colorsRecyclerAdapter = ColorsRecyclerAdapter(this, mutableListOf())
        colorsRecyclerAdapter.setOnClickListener(object : ColorsRecyclerAdapter.OnClickListener {
            override fun onItemClick(
                view: View?, colorItem: ColorItem, position: Int
            ) {
                if (colorItem.type == ColorItem.ADD) {
                    //Add button clicked
                    ColorPickerDialog.Builder(this@NoteActivity)
                        .setTitle(resources.getString(R.string.alert_dialog_add_color))
                        .setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton(
                                R.string.select,
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
                    return
                }
                val selectedEditText: EditText =
                    if (bindingContent.contentEditText.isFocused) bindingContent.contentEditText
                    else if (bindingContent.headerEditText.isFocused) bindingContent.headerEditText
                    else return // User didn't selected an edittext;
                val selectionStart = selectedEditText.selectionStart
                val selectionEnd = selectedEditText.selectionEnd
                
                when (clickedActionButtonState) {
                    ClickedActionButtonState.FOREGROUND_COLOR -> {
                        changeTextForegroundColor(
                                selectedEditText,
                                colorItem.color,
                                selectionStart,
                                selectionEnd)
                    }
                    ClickedActionButtonState.BACKGROUND_COLOR -> {
                        changeTextBackgroundColor(
                                selectedEditText,
                                colorItem.color,
                                selectionStart,
                                selectionEnd)
                    }
                    ClickedActionButtonState.NULL -> {}
                }
                selectedEditText.setSelection(
                        selectionStart,
                        selectionEnd)
                
            }
            
            override fun onItemLongClick(view: View?, colorItem: ColorItem, position: Int) {
                if (colorItem.type == ColorItem.ADD) {
                    AlertDialog.Builder(this@NoteActivity, R.style.MaterialAlertDialog)
                        .setTitle(R.string.confirm_colors_reset)
                        .setPositiveButton(R.string.reset) { dialog, which ->
                            vm.resetColors(this@NoteActivity)
                            colorsRecyclerAdapter.notifyDataSetChanged()
                            
                            
                        }
                        .setNegativeButton(getString(android.R.string.cancel)) { dialog, which ->
                            dialog.dismiss()
                            
                        }
                        .show()
                        .window!!.decorView.setBackgroundResource(R.drawable.dialog_rounded_corners)
                    
                } else {
                    
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
                        .window!!.decorView.setBackgroundResource(R.drawable.dialog_rounded_corners)
                    
                    
                }
            }
        })
        bindingContent.actionMenuColorsRecyclerView.adapter =
            colorsRecyclerAdapter
        vm.colorsList.observe(this) {
            colorsRecyclerAdapter.setList(it)
        }
        
        bindingContent.actionMenuGravityLeftImageView.setOnClickListener {
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
            changeTextGravity(
                    Gravity.LEFT,
                    bindingContent.contentEditText.selectionStart,
                    bindingContent.contentEditText.selectionEnd)
            bindingContent.contentEditText.setSelection(
                    startSelection,
                    endSelection)
        }
        bindingContent.actionMenuGravityCenterImageView.setOnClickListener {
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
            changeTextGravity(
                    Gravity.CENTER,
                    bindingContent.contentEditText.selectionStart,
                    bindingContent.contentEditText.selectionEnd)
            bindingContent.contentEditText.setSelection(
                    startSelection,
                    endSelection)
            
        }
        bindingContent.actionMenuGravityRightImageView.setOnClickListener {
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
            changeTextGravity(
                    Gravity.RIGHT,
                    bindingContent.contentEditText.selectionStart,
                    bindingContent.contentEditText.selectionEnd)
            bindingContent.contentEditText.setSelection(
                    startSelection,
                    endSelection)
            
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
            bindingContent.actionMenuColorsLinearLayout.visibility =
                View.VISIBLE
            
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
            bindingContent.actionMenuGravityLinearLayout.visibility =
                View.VISIBLE
            
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
            bindingContent.actionMenuColorsLinearLayout.visibility =
                View.VISIBLE
            
            
        }
        
        bindingContent.actionMenuColorsRemoveImageView.setOnClickListener {
            val selectedEditText: EditText =
                if (bindingContent.contentEditText.isFocused) bindingContent.contentEditText
                else if (bindingContent.headerEditText.isFocused) bindingContent.headerEditText
                else return@setOnClickListener // User didn't selected an edittext;
            val selectionStart = selectedEditText.selectionStart
            val selectionEnd = selectedEditText.selectionEnd
            when (clickedActionButtonState) {
                ClickedActionButtonState.NULL ->
                    throw Exception("clickedActionButtonState cannot be null when color buttons were clicked.")
                ClickedActionButtonState.FOREGROUND_COLOR ->
                    changeTextForegroundColor(
                            selectedEditText,
                            null,
                            startPosition = selectionStart,
                            endPosition = selectionEnd)
                ClickedActionButtonState.BACKGROUND_COLOR ->
                    changeTextBackgroundColor(
                            selectedEditText,
                            null,
                            startPosition = selectionStart,
                            endPosition = selectionEnd)
                
            }
            selectedEditText.setSelection(
                    selectionStart,
                    selectionEnd)
            
            
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
    
    /**
     * This method changes selected text foreground color of contentEditText
     */
    private fun changeTextForegroundColor(
        editText: EditText,
        @ColorInt foregroundColor: Int?,
        startPosition: Int,
        endPosition: Int
    ) {
        val resultText: Spannable =
            editText.text?.toSpannable() ?: return
        
        val spans: Array<ForegroundColorSpan> = resultText.getSpans(
                startPosition, endPosition,
                ForegroundColorSpan::class.java)
        spans.forEach {
            val selectedSpanStart = resultText.getSpanStart(it)
            val selectedSpanEnd = resultText.getSpanEnd(it)
            if (selectedSpanStart >= startPosition && selectedSpanEnd <= endPosition) {
                
                resultText.removeSpan(it)
                
            }
            
        }
        if (foregroundColor == null) {
            resultText.setSpan(
                    ForegroundColorSpan(Color.WHITE),
                    startPosition,
                    endPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            resultText.setSpan(
                    ForegroundColorSpan(foregroundColor),
                    startPosition,
                    endPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            
        }
        
        editText.setText(resultText)
        
    }
    
    /**
     * This method changes selected text background color of contentEditText
     */
    private fun changeTextBackgroundColor(
        editText: EditText,
        @ColorInt backgroundColor: Int?,
        startPosition: Int,
        endPosition: Int
    ) {
        val resultText: Spannable =
            editText.text?.toSpannable() ?: return
        
        val spans: Array<BackgroundColorSpan> = resultText.getSpans(
                startPosition, endPosition,
                BackgroundColorSpan::class.java)
        spans.forEach {
            val selectedSpanStart = resultText.getSpanStart(it)
            val selectedSpanEnd = resultText.getSpanEnd(it)
            if (selectedSpanStart >= startPosition && selectedSpanEnd <= endPosition) {
                resultText.removeSpan(it)
                
            }
            
        }
        if (backgroundColor == null) {
            resultText.setSpan(
                    BackgroundColorSpan(Color.TRANSPARENT),
                    startPosition,
                    endPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            resultText.setSpan(
                    BackgroundColorSpan(backgroundColor),
                    startPosition,
                    endPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            
        }
        
        editText.setText(resultText)
        
        
    }
    
    /**
     * This method changes text gravity.
     */
    private fun changeTextGravity(
        @GravityInt gravity: Int,
        selectionStart: Int,
        selectionEnd: Int
    ) {
        // Won't change text gravity for headerEditText
        if (bindingContent.headerEditText.isFocused) return
        // If contentEditText is not focused then do nothing
        if (!bindingContent.contentEditText.isFocused) return
        
        val resultText =
            bindingContent.contentEditText.text?.toSpannable() ?: return
        val selectedLinePositions =
            getEditTextSelectedLineTextBySelection(
                    bindingContent.contentEditText,
                    selectionStart, selectionEnd)
        val firstSelectedLineStart = selectedLinePositions.first
        val lastSelectedLineEnd = selectedLinePositions.second
        
        val spans: Array<AlignmentSpan> = resultText.getSpans(
                firstSelectedLineStart, lastSelectedLineEnd,
                AlignmentSpan::class.java)
        spans.forEach {
            val selectedSpanStart = resultText.getSpanStart(it)
            val selectedSpanEnd = resultText.getSpanEnd(it)
            if (selectedSpanStart >= selectionStart && selectedSpanEnd <= selectionEnd) {
                resultText.removeSpan(it)
                
            }
            
        }
        
        when (gravity) {
            Gravity.LEFT -> {
                resultText.setSpan(
                        (AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL)),
                        firstSelectedLineStart,
                        lastSelectedLineEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            
            Gravity.CENTER -> {
                resultText.setSpan(
                        (AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)),
                        firstSelectedLineStart,
                        lastSelectedLineEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            
            Gravity.RIGHT -> {
                resultText.setSpan(
                        AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
                        firstSelectedLineStart,
                        lastSelectedLineEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                bindingContent.contentEditText.setText(resultText)
            }
        }
        bindingContent.contentEditText.setText(resultText)
        
    }
    
    /**
     * @return pair of int with start text of line and.
     */
    private fun getEditTextSelectedLineTextBySelection(
        editText: EditText,
        selectionStart: Int,
        selectionEnd: Int
    ): Pair<Int, Int> {
        var selectedLineStart = -1
        var selectedLineEnd = -1
        if (selectionStart != -1) {
            val firstSelectedLine = editText.layout.getLineForOffset(selectionStart)
            val lastSelectedLine = editText.layout.getLineForOffset(selectionEnd)
            selectedLineStart = editText.layout.getLineStart(firstSelectedLine)
            selectedLineEnd = editText.layout.getLineVisibleEnd(lastSelectedLine)
        }
        return Pair(selectedLineStart, selectedLineEnd)
    }
    
    private fun initSlidingDrawerMenu() {
        val showMoreDrawable = ContextCompat.getDrawable(this, R.drawable.ic_show_more_animated)
                as AnimatedVectorDrawable
        val showLessDrawable = ContextCompat.getDrawable(this, R.drawable.ic_show_less_animated)
                as AnimatedVectorDrawable
        
        
        menuBinding.expandChangeBackgroundButton.setOnClickListener {
            if (menuBinding.changeBackgroundExpandableLayout.isExpanded) {
                menuBinding.changeBackgroundButtonImageView.setImageDrawable(showLessDrawable)
                showLessDrawable.start()
                menuBinding.changeBackgroundExpandableLayout.collapse()
            } else {
                menuBinding.changeBackgroundButtonImageView.setImageDrawable(showMoreDrawable)
                showMoreDrawable.start()
                menuBinding.changeBackgroundExpandableLayout.expand()
            }
            
        }
        
        menuBinding.drawerMenuSelectImageButton.setOnClickListener {
            
            ImagePicker.with(activity = this)
                .crop(1f, 2f)
                .createIntent {
                    pickImageResult.launch(it)
                    
                }
            
            
        }
        menuBinding.drawerMenuResetButton.setOnClickListener() {
            val colorBackground = ContextCompat.getColor(
                    this,
                    ThemeHandler(this).getColorBackground())
            bindingContent.noteBackgroundImageView.setImageDrawable(
                    ColorDrawable(colorBackground))
            note.background = colorBackground.toString()
            CoroutineScope(Dispatchers.IO).launch {
                updateNoteUseCase(note)
                
            }
            
        }
        
        initImageRecyclerView()
        initChangeTransparentViewsSwitchCompat()
    }
    
    private fun initImageRecyclerView() {
        val imagesList = fillImagesList()
        val imageRecyclerAdapter = ImagesRecyclerAdapter(this, imagesList)
        imageRecyclerAdapter.setOnClickListener(object : ImagesRecyclerAdapter.OnClickListener {
            override fun onItemClick(view: View?, drawable: Drawable, position: Int) {
                // Here we store int as background, then get drawable by position
                // from Utils.backgroundImagesIds and set it.
                bindingContent.noteBackgroundImageView.setImageDrawable(drawable)
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
        
        for (drawableResId in Utils.backgroundImages) {
            imagesList.add(ContextCompat.getDrawable(this, drawableResId)!!)
            
        }
        return imagesList
    }
    
    private fun initChangeTransparentViewsSwitchCompat() {
        menuBinding.switchTransparentActionBarSwitchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            } else {
                val colorPrimary =
                    ContextCompat.getColor(this, ThemeHandler(this).getColorPrimary())
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(colorPrimary))
            }
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setHomeButtonEnabled(true)
            note.isShowTransparentActionBar = isChecked
            CoroutineScope(Dispatchers.IO).launch {
                updateNoteUseCase(note)
                
            }
            
        }
        menuBinding.switchTransparentActionBarSwitchCompat.isChecked =
            note.isShowTransparentActionBar
        
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        when (bindingContent.headerEditText.isFocused or bindingContent.contentEditText.isFocused) {
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