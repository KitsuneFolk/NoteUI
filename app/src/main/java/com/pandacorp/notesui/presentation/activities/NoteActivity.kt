package com.pandacorp.notesui.presentation.activities

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.*
import android.util.TypedValue
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
import com.pandacorp.domain.usecases.utils.Constans.imgId
import com.pandacorp.domain.usecases.utils.HideToolbarWhileScrollingUseCase
import com.pandacorp.domain.usecases.utils.JsonToSpannableUseCase
import com.pandacorp.domain.usecases.utils.SpannableToJsonUseCase
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.ActivityNoteBinding
import com.pandacorp.notesui.databinding.ContentActivityNoteBinding
import com.pandacorp.notesui.databinding.MenuNoteDrawerBinding
import com.pandacorp.notesui.presentation.adapter.ColorsRecyclerAdapter
import com.pandacorp.notesui.presentation.adapter.ImagesRecyclerAdapter
import com.pandacorp.notesui.presentation.settings.dialog.DialogColorPicker
import com.pandacorp.notesui.utils.Constans
import com.pandacorp.notesui.utils.PreferenceHandler
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
    private lateinit var bindingMenu: MenuNoteDrawerBinding
    
    private lateinit var sp: SharedPreferences
    
    private lateinit var toolbar: Toolbar
    private var isHideToolbarWhileScrolling: Boolean = true
    
    private lateinit var colorsRecyclerAdapter: ColorsRecyclerAdapter
    
    private val getNotesUseCase: GetNotesUseCase by inject()
    private val updateNoteUseCase: UpdateNoteUseCase by inject()
    private val hideToolbarWhileScrollingUseCase: HideToolbarWhileScrollingUseCase by inject()
    
    private val vm: NoteViewModel by viewModel()
    
    private val spannableToJsonUseCase: SpannableToJsonUseCase = SpannableToJsonUseCase()
    private val jsonToSpannableUseCase: JsonToSpannableUseCase =
        JsonToSpannableUseCase(this)
    
    private lateinit var note: NoteItem
    private lateinit var noteBackground: String
    private var notePositionInAdapter: Int? = null
    
    private lateinit var undoRedoContentEditTextHelper: UndoRedoHelper
    private lateinit var undoRedoHeaderEditTextHelper: UndoRedoHelper
    
    private var clickedActionMenuButton = Constans.ClickedActionMenu.NULL
    
    private val pickNoteBackgroundImageResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val imageUri = it.data!!.data
            
            bindingContent.noteBackgroundImageView.setImageURI(imageUri)
            note.background = imageUri.toString()
            noteBackground = note.background
            CoroutineScope(Dispatchers.IO).launch {
                updateNoteUseCase(note)
                
            }
        }
        
    }
    
    private val pickImageToAddResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val imageUri = it.data!!.data
            val editText: EditText =
                getFocusedEditText() ?: throw IllegalArgumentException("Focused edittext = null")
            
            val selectionStart = editText.selectionStart
            
            // get drawable from uri
            imageUri?.also {
                insertImage(editText, imageUri)
                
            }
            editText.setSelection(selectionStart)
            
        }
    }
    
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferenceHandler(this).load()
        binding = ActivityNoteBinding.inflate(layoutInflater)
        bindingContent = binding.contentActivityInclude
        bindingMenu = binding.drawerMenuInclude
        setContentView(binding.root)
        Utils.setupExceptionHandler()
        toolbar = findViewById(R.id.noteToolbar)
        sp = PreferenceManager.getDefaultSharedPreferences(this)
        isHideToolbarWhileScrolling = sp
            .getBoolean(Constans.PreferencesKeys.isHideActionBarOnScrollKey, true)
        hideToolbarWhileScrollingUseCase(toolbar = toolbar, isHide = isHideToolbarWhileScrolling)
        setSupportActionBar(toolbar)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        restoreSavedData(savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
            initViews()
            CoroutineScope(Dispatchers.Main).launch {
                initEditTexts()
                initActionBottomMenu()
                initSlidingDrawerMenu()
                
            }
            
        }
        
        
    }
    
    /**
     * This method restores data when screen gets rotated and gets the data from intent if activity started
     */
    private fun restoreSavedData(savedInstanceState: Bundle?) {
        val headerSpannable: Spannable?
        val contentSpannable: Spannable?
        val background: String?
        val isShowTransparentActionBar: Boolean?
        if (savedInstanceState != null) {
            headerSpannable =
                jsonToSpannableUseCase(
                        bindingContent.headerEditText,
                        savedInstanceState.getString(Constans.Bundles.noteHeaderText)!!)
            contentSpannable =
                jsonToSpannableUseCase(
                        bindingContent.contentEditText,
                        savedInstanceState.getString(Constans.Bundles.noteContentText)!!)
            background = savedInstanceState.getString(Constans.Bundles.noteBackground)!!
            isShowTransparentActionBar =
                savedInstanceState.getBoolean(Constans.Bundles.noteIsShowTransparentActionBar)
            
        } else {
            headerSpannable =
                jsonToSpannableUseCase(
                        bindingContent.headerEditText,
                        intent.getStringExtra(Constans.Bundles.noteHeaderText)!!)
            contentSpannable =
                jsonToSpannableUseCase(
                        bindingContent.contentEditText,
                        intent.getStringExtra(Constans.Bundles.noteContentText)!!)
            background = intent.getStringExtra(Constans.Bundles.noteBackground)!!
            isShowTransparentActionBar =
                intent.getBooleanExtra(Constans.Bundles.noteIsShowTransparentActionBar, false)
        }
        bindingContent.headerEditText.setText(headerSpannable)
        bindingContent.contentEditText.setText(contentSpannable)
        changeNoteBackground(background)
        if (isShowTransparentActionBar)
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        else {
            val tv = TypedValue()
            theme.resolveAttribute(android.R.attr.colorPrimary, tv, true)
            val colorPrimary = tv.data
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(colorPrimary))
        }
        
        // Text size
        val contentTextSize = sp.getString(
                Constans.PreferencesKeys.contentTextSizeKey,
                Constans.PreferencesKeys.contentTextSizeDefaultValue)!!.toFloat()
        val headerTextSize = sp.getString(
                Constans.PreferencesKeys.headerTextSizeKey,
                Constans.PreferencesKeys.headerTextSizeDefaultValue)!!.toFloat()
        changeEditTextTextSize(bindingContent.contentEditText, contentTextSize)
        changeEditTextTextSize(bindingContent.headerEditText, headerTextSize)
        
        savedInstanceState?.also {
            clickedActionMenuButton = it.getInt(Constans.ClickedActionMenu.BUNDLE_KEY)
            when (clickedActionMenuButton) {
                Constans.ClickedActionMenu.NULL -> {}
                Constans.ClickedActionMenu.FOREGROUND -> {
                    bindingContent.actionMenuButtonsLayout.visibility = View.GONE
                    bindingContent.actionMenuColorsLayout.visibility = View.VISIBLE
                    
                }
                Constans.ClickedActionMenu.BACKGROUND -> {
                    bindingContent.actionMenuButtonsLayout.visibility = View.GONE
                    bindingContent.actionMenuColorsLayout.visibility = View.VISIBLE
                }
                Constans.ClickedActionMenu.GRAVITY -> {
                    bindingContent.actionMenuButtonsLayout.visibility = View.GONE
                    bindingContent.actionMenuGravityLayout.visibility = View.VISIBLE
                }
            }
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val headerText =
            spannableToJsonUseCase((bindingContent.headerEditText.text ?: "").toSpannable())
        val contentText =
            spannableToJsonUseCase((bindingContent.contentEditText.text ?: "").toSpannable())
        
        // if noteBackground has been not chosen, then get it's value from intent
        if (!this::noteBackground.isInitialized) {
            noteBackground = intent.getStringExtra(Constans.Bundles.noteBackground)!!
        }
        outState.putString(Constans.Bundles.noteHeaderText, headerText)
        outState.putString(Constans.Bundles.noteContentText, contentText)
        outState.putString(Constans.Bundles.noteBackground, noteBackground)
        outState.putBoolean(
                Constans.Bundles.noteIsShowTransparentActionBar,
                bindingMenu.switchTransparentActionBarSwitchCompat.isChecked
        )
        outState.putInt(Constans.ClickedActionMenu.BUNDLE_KEY, clickedActionMenuButton)
    }
    
    private suspend fun initViews() {
        val databaseList = withContext(Dispatchers.IO) {
            getNotesUseCase()
        }
        // Here we get note by position what we get from intent.
        val lastNote = databaseList.size - 1
        notePositionInAdapter = intent.getIntExtra(intentNotePositionInAdapter, lastNote)
        note = databaseList[notePositionInAdapter!!]
        
        // When activity starts, don't show keyboard.
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        
    }
    
    private fun changeNoteBackground(background: String) {
        val noteBackgroundImageView = bindingContent.noteBackgroundImageView
        try {
            // note.background is an image drawable from Utils.backgroundImages
            val drawableResId = Utils.backgroundImages[background.toInt()]
            val drawable = ContextCompat.getDrawable(this, drawableResId)
            noteBackgroundImageView.setImageDrawable(drawable)
        } catch (e: ArrayIndexOutOfBoundsException) {
            // note.background is a color.
            val typedValue = TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
            val colorBackground = typedValue.data
            noteBackgroundImageView.background = ColorDrawable(colorBackground)
        } catch (e: NumberFormatException) {
            // note.background is a image from storage (uri)
            noteBackgroundImageView.setImageURI(Uri.parse(background))
            
        }
        
    }
    
    private fun changeEditTextTextSize(editText: EditText, size: Float) {
        editText.textSize = size
    }
    
    private fun initEditTexts() {
        
        undoRedoContentEditTextHelper =
            UndoRedoHelper(binding.contentActivityInclude.contentEditText)
        undoRedoHeaderEditTextHelper = UndoRedoHelper(binding.contentActivityInclude.headerEditText)
        
        bindingContent.headerEditText.setOnFocusChangeListener { v, hasFocus ->
            if (isHideToolbarWhileScrolling) hideToolbarWhileScrollingUseCase(toolbar, !hasFocus)
            invalidateOptionsMenu()
        }
        bindingContent.contentEditText.setOnFocusChangeListener { v, hasFocus ->
            if (isHideToolbarWhileScrolling) hideToolbarWhileScrollingUseCase(toolbar, !hasFocus)
            invalidateOptionsMenu()
        }
        
    }
    
    private fun initActionBottomMenu() {
        
        colorsRecyclerAdapter = ColorsRecyclerAdapter(this, mutableListOf())
        colorsRecyclerAdapter.setOnClickListener(object :
            ColorsRecyclerAdapter.OnColorItemClickListener {
            override fun onClick(view: View?, colorItem: ColorItem, position: Int) {
                if (colorItem.type == ColorItem.ADD) {
                    //Add button clicked
                    val dialog = DialogColorPicker.newInstance()
                    dialog.setOnPositiveButtonClick { envelope, fromUser ->
                        val newColorItem = ColorItem(color = envelope.color)
                        vm.addColor(newColorItem)
                        
                    }
                    dialog.show(this@NoteActivity.supportFragmentManager, null)
                    return
                }
                val selectedEditText: EditText = getFocusedEditText() ?: return
                val selectionStart = selectedEditText.selectionStart
                val selectionEnd = selectedEditText.selectionEnd
                
                when (clickedActionMenuButton) {
                    Constans.ClickedActionMenu.FOREGROUND -> {
                        changeTextForegroundColor(
                                selectedEditText,
                                colorItem.color,
                                selectionStart,
                                selectionEnd)
                    }
                    Constans.ClickedActionMenu.BACKGROUND -> {
                        changeTextBackgroundColor(
                                selectedEditText,
                                colorItem.color,
                                selectionStart,
                                selectionEnd)
                    }
                    Constans.ClickedActionMenu.NULL -> {}
                }
                selectedEditText.setSelection(
                        selectionStart,
                        selectionEnd)
                
            }
        })
        colorsRecyclerAdapter.setOnLongClickListener(object :
            ColorsRecyclerAdapter.OnColorItemLongClickListener {
            override fun onLongClick(view: View?, colorItem: ColorItem, position: Int) {
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
            animation.addTarget(R.id.actionMenuButtonsLayout)
            animation.addTarget(R.id.actionMenuGravityLayout)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuGravityLayout.visibility = View.GONE
            bindingContent.actionMenuButtonsLayout.visibility = View.VISIBLE
            clickedActionMenuButton = Constans.ClickedActionMenu.NULL
            
        }
        
        bindingContent.actionMenuButtonChangeTextForegroundColor.setOnClickListener {
            clickedActionMenuButton = Constans.ClickedActionMenu.FOREGROUND
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuButtonsLayout)
            animation.addTarget(R.id.actionMenuColorsLayout)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuButtonsLayout.visibility = View.GONE
            bindingContent.actionMenuColorsLayout.visibility =
                View.VISIBLE
            clickedActionMenuButton = Constans.ClickedActionMenu.FOREGROUND
            
        }
        bindingContent.actionMenuButtonChangeTextGravity.setOnClickListener {
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuButtonsLayout)
            animation.addTarget(R.id.actionMenuGravityLayout)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuButtonsLayout.visibility = View.GONE
            bindingContent.actionMenuGravityLayout.visibility = View.VISIBLE
            clickedActionMenuButton = Constans.ClickedActionMenu.GRAVITY
            
        }
        bindingContent.actionMenuButtonChangeTextBackgroundColor.setOnClickListener {
            clickedActionMenuButton = Constans.ClickedActionMenu.BACKGROUND
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuButtonsLayout)
            animation.addTarget(R.id.actionMenuColorsLayout)
            animation.addTarget(R.id.actionMenuColorsRecyclerView)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuButtonsLayout.visibility = View.GONE
            bindingContent.actionMenuColorsLayout.visibility = View.VISIBLE
            clickedActionMenuButton = Constans.ClickedActionMenu.BACKGROUND
            
        }
        
        bindingContent.actionMenuColorsRemoveImageView.setOnClickListener {
            val selectedEditText: EditText = getFocusedEditText() ?: return@setOnClickListener
            val selectionStart = selectedEditText.selectionStart
            val selectionEnd = selectedEditText.selectionEnd
            when (clickedActionMenuButton) {
                Constans.ClickedActionMenu.NULL ->
                    throw IllegalArgumentException("Constans.ClickedActionMenu cannot be null when color buttons were clicked.")
                Constans.ClickedActionMenu.FOREGROUND ->
                    changeTextForegroundColor(
                            selectedEditText,
                            null,
                            startPosition = selectionStart,
                            endPosition = selectionEnd)
                Constans.ClickedActionMenu.BACKGROUND ->
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
            clickedActionMenuButton = Constans.ClickedActionMenu.NULL
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuButtonsLayout)
            animation.addTarget(R.id.actionMenuColorsLayout)
            animation.addTarget(R.id.actionMenuColorsRecyclerView)
            TransitionManager.beginDelayedTransition(
                    bindingContent.actionMenuParentLayout,
                    animation)
            bindingContent.actionMenuColorsLayout.visibility = View.GONE
            bindingContent.actionMenuButtonsLayout.visibility = View.VISIBLE
            clickedActionMenuButton = Constans.ClickedActionMenu.NULL
            
        }
        
        bindingContent.actionMenuButtonAddImage.setOnClickListener {
            if (getFocusedEditText() != null) {
                
                ImagePicker.Builder(activity = this)
                    .createIntent { resultIntent ->
                        pickImageToAddResult.launch(resultIntent)
                        
                    }
            }
            
        }
        
        bindingContent.actionMenuButtonBold.setOnClickListener {
            val editText = getFocusedEditText() ?: return@setOnClickListener
            
            makeBold(editText = editText)
        }
        bindingContent.actionMenuButtonItalic.setOnClickListener {
            val editText = getFocusedEditText() ?: return@setOnClickListener
            
            makeItalic(editText = editText)
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
        if (getFocusedEditText() != bindingContent.contentEditText) return // don't change gravity for headerEdittext
        
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
     * This function inserts images at position inside edittext
     */
    private fun insertImage(editText: EditText, uri: Uri) {
        if (editText == bindingContent.headerEditText) return // Don't insert image for header edittext
        
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        
        val inputStream = this.contentResolver.openInputStream(uri)
        val drawable = Drawable.createFromStream(inputStream, uri.toString())
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        
        val imageSpan = ImageSpan(drawable, uri.toString(), ImageSpan.ALIGN_BASELINE)
        val builder = SpannableStringBuilder(imgId)
        builder.setSpan(imageSpan, 0, imgId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        editText.text.replace(selectionStart, selectionEnd, builder)
        
    }
    
    /**
     * This function inserts bold span at selected edittext positions, if there is already spans it removes and adds nothing, else adds a span
     * @param editText an edittext where you need to insert bold span;
     */
    private fun makeBold(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
    
        val span = editText.text.toSpannable()
        
        val styleSpans = span.getSpans(start, end, StyleSpan::class.java)
        val boldSpans: MutableList<StyleSpan> = mutableListOf()
        
        // fill list
        styleSpans.forEach { styleSpan ->
            if (styleSpan.style == Typeface.BOLD) boldSpans.add(styleSpan)
        }
        boldSpans.forEach { boldSpan ->
            val selectedSpanStart = span.getSpanStart(boldSpan)
            val selectedSpanEnd = span.getSpanEnd(boldSpan)
            
            if (selectedSpanStart >= start && selectedSpanEnd <= end) {
                span.removeSpan(boldSpan)
                
            }
        }
        if (boldSpans.isEmpty()) {
            // add spans
            span.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            // remove spans, what we do above
        }
        
        editText.setText(span)
        editText.setSelection(start, end)
    }
    
    private fun makeItalic(editText: EditText) {
        val start = editText.selectionStart
        val end = editText.selectionEnd
    
        val span = editText.text.toSpannable()
        
        val styleSpans = span.getSpans(start, end, StyleSpan::class.java)
        val italicSpans: MutableList<StyleSpan> = mutableListOf()
        
        // fill list
        styleSpans.forEach { styleSpan ->
            if (styleSpan.style == Typeface.ITALIC) italicSpans.add(styleSpan)
        }
        
        italicSpans.forEach { italicSpan ->
            val selectedSpanStart = span.getSpanStart(italicSpan)
            val selectedSpanEnd = span.getSpanEnd(italicSpan)
            
            if (selectedSpanStart >= start && selectedSpanEnd <= end) {
                span.removeSpan(italicSpan)
                
            }
        }
        if (italicSpans.isEmpty()) {
            // add spans
            span.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        } else {
            // remove spans, what we do above
        }
        
        editText.setText(span)
        editText.setSelection(start, end)
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
        
        
        bindingMenu.expandChangeBackgroundButton.setOnClickListener {
            if (bindingMenu.changeBackgroundExpandableLayout.isExpanded) {
                bindingMenu.changeBackgroundButtonImageView.setImageDrawable(showLessDrawable)
                showLessDrawable.start()
                bindingMenu.changeBackgroundExpandableLayout.collapse()
            } else {
                bindingMenu.changeBackgroundButtonImageView.setImageDrawable(showMoreDrawable)
                showMoreDrawable.start()
                bindingMenu.changeBackgroundExpandableLayout.expand()
            }
            
        }
        
        bindingMenu.drawerMenuSelectImageButton.setOnClickListener {
            
            ImagePicker.with(activity = this)
                .crop(1f, 2f)
                .createIntent {
                    pickNoteBackgroundImageResult.launch(it)
                    
                }
            
            
        }
        bindingMenu.drawerMenuResetButton.setOnClickListener {
            val tv = TypedValue()
            theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
            val colorBackground = tv.data
            bindingContent.noteBackgroundImageView.setImageDrawable(
                    ColorDrawable(colorBackground))
            note.background = colorBackground.toString()
            noteBackground = note.background
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
        imageRecyclerAdapter.setOnClickListener(object :
            ImagesRecyclerAdapter.OnImageItemClickListener {
            override fun onClick(view: View?, drawable: Drawable, position: Int) {
                // Here we store int as background, then get drawable by position
                // from Utils.backgroundImagesIds and set it.
                bindingContent.noteBackgroundImageView.setImageDrawable(drawable)
                note.background = position.toString()
                noteBackground = note.background
                CoroutineScope(Dispatchers.IO).launch {
                    updateNoteUseCase(note)
                    
                }
            }
        })
        bindingMenu.imageRecyclerView.adapter = imageRecyclerAdapter
        
    }
    
    private fun fillImagesList(): MutableList<Drawable> {
        val imagesList = mutableListOf<Drawable>()
        
        for (drawableResId in Utils.backgroundImages) {
            imagesList.add(ContextCompat.getDrawable(this, drawableResId)!!)
            
        }
        return imagesList
    }
    
    private fun initChangeTransparentViewsSwitchCompat() {
        bindingMenu.switchTransparentActionBarSwitchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            } else {
                val tv = TypedValue()
                theme.resolveAttribute(android.R.attr.colorPrimary, tv, true)
                val colorPrimary = tv.data
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(colorPrimary))
            }
            note.isShowTransparentActionBar = isChecked
            CoroutineScope(Dispatchers.IO).launch {
                updateNoteUseCase(note)
                
            }
            
        }
        bindingMenu.switchTransparentActionBarSwitchCompat.isChecked =
            note.isShowTransparentActionBar
        
    }
    
    private fun getFocusedEditText(): EditText? {
        return if (bindingContent.headerEditText.isFocused)
            bindingContent.headerEditText
        else if (bindingContent.contentEditText.isFocused)
            bindingContent.contentEditText
        else null
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Show undo and redo button if edittext is focused.
        if (getFocusedEditText() == null)
            menuInflater.inflate(R.menu.menu_note, menu)
        // Show menu hamburger icon if edittext is not focused.
        else
            menuInflater.inflate(R.menu.menu_note_extended, menu)
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
            //When app was stopped, and then user rotates the phone in another app, it can cause an Exception.
        }
        super.onPause()
    }
    
    companion object {
        const val TAG = "NoteActivity"
        const val intentNotePositionInAdapter = "notePositionInAdapter"
        
    }
}
    