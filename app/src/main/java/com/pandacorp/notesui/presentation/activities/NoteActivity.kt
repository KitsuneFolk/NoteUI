package com.pandacorp.notesui.presentation.activities

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.util.TypedValue
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
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
import com.pandacorp.domain.usecases.notes.database.UpdateNoteUseCase
import com.pandacorp.domain.usecases.utils.HideToolbarWhileScrollingUseCase
import com.pandacorp.domain.usecases.utils.JsonToSpannableUseCase
import com.pandacorp.domain.usecases.utils.SpannableToJsonUseCase
import com.pandacorp.domain.usecases.utils.text.*
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.ActivityNoteBinding
import com.pandacorp.notesui.databinding.ContentActivityNoteBinding
import com.pandacorp.notesui.databinding.MenuNoteDrawerBinding
import com.pandacorp.notesui.presentation.adapter.ColorsRecyclerAdapter
import com.pandacorp.notesui.presentation.adapter.ImagesRecyclerAdapter
import com.pandacorp.notesui.utils.Constans
import com.pandacorp.notesui.utils.PreferenceHandler
import com.pandacorp.notesui.utils.UndoRedoHelper
import com.pandacorp.notesui.utils.Utils
import com.pandacorp.notesui.utils.dialog.CustomBottomSheetDialog
import com.pandacorp.notesui.utils.dialog.DialogColorPicker
import com.pandacorp.notesui.viewModels.MainViewModel
import com.pandacorp.notesui.viewModels.NoteViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    
    private val updateNoteUseCase: UpdateNoteUseCase by inject()
    private val hideToolbarWhileScrollingUseCase: HideToolbarWhileScrollingUseCase by inject()
    
    private val notesVM: MainViewModel by viewModel()
    private val colorsVM: NoteViewModel by viewModel()
    
    private val spannableToJsonUseCase: SpannableToJsonUseCase = SpannableToJsonUseCase()
    private val jsonToSpannableUseCase: JsonToSpannableUseCase = JsonToSpannableUseCase(this)
    
    private val changeTextForegroundColorUseCase: ChangeTextForegroundColorUseCase by inject()
    private val changeTextBackgroundColorUseCase: ChangeTextBackgroundColorUseCase by inject()
    private val changeTextGravityUseCase: ChangeTextGravityUseCase by inject()
    private val insertImageInEditTextUseCase: InsertImageInEditTextUseCase by inject()
    private val makeTextBoldUseCase: MakeTextBoldUseCase by inject()
    private val makeTextItalicUseCase: MakeTextItalicUseCase by inject()
    
    private lateinit var note: NoteItem
    
    private lateinit var undoRedoContentEditTextHelper: UndoRedoHelper
    private lateinit var undoRedoHeaderEditTextHelper: UndoRedoHelper
    
    private var clickedActionMenuButton = Constans.ClickedActionButton.NULL
    
    private val pickNoteBackgroundImageResult = registerForActivityResult(
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
    
    private val pickImageToAddResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val imageUri = it.data!!.data
            val editText: EditText =
                getFocusedEditText() ?: throw IllegalArgumentException("Focused edittext = null")
            
            val selectionStart = editText.selectionStart
            
            // get drawable from uri
            imageUri?.also {
                insertImageInEditTextUseCase(editText, imageUri)
                
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
        initViews()
        restoreSavedData(savedInstanceState)
        CoroutineScope(Dispatchers.IO).launch {
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
        savedInstanceState?.also {
            clickedActionMenuButton = it.getInt(Constans.ClickedActionButton.KEY)
            when (clickedActionMenuButton) {
                Constans.ClickedActionButton.NULL -> {}
                Constans.ClickedActionButton.FOREGROUND -> {
                    bindingContent.actionMenuButtonsLayout.visibility = View.GONE
                    bindingContent.actionMenuColorsLayout.visibility = View.VISIBLE
                    
                }
                Constans.ClickedActionButton.BACKGROUND -> {
                    bindingContent.actionMenuButtonsLayout.visibility = View.GONE
                    bindingContent.actionMenuColorsLayout.visibility = View.VISIBLE
                }
                Constans.ClickedActionButton.GRAVITY -> {
                    bindingContent.actionMenuButtonsLayout.visibility = View.GONE
                    bindingContent.actionMenuGravityLayout.visibility = View.VISIBLE
                }
            }
        }
        
        
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(Constans.ClickedActionButton.KEY, clickedActionMenuButton)
    }
    
    private fun initViews() {
        // Here we get note by position what we get from intent.
        val notesList = notesVM.getNotes()
        note = notesList[intent.getIntExtra(intentNotePosition, notesList.lastIndex)]
        
        if (note.isShowTransparentActionBar)
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        else {
            val tv = TypedValue()
            theme.resolveAttribute(android.R.attr.colorPrimary, tv, true)
            val colorPrimary = tv.data
            supportActionBar!!.setBackgroundDrawable(ColorDrawable(colorPrimary))
        }
        
        changeNoteBackground(note.background)
        
        // Text size
        CoroutineScope(Dispatchers.Main).launch {
            val contentTextSize = sp.getString(
                    Constans.PreferencesKeys.contentTextSizeKey,
                    Constans.PreferencesKeys.contentTextSizeDV)!!.toFloat()
            val headerTextSize = sp.getString(
                    Constans.PreferencesKeys.headerTextSizeKey,
                    Constans.PreferencesKeys.headerTextSizeDV)!!.toFloat()
            changeEditTextTextSize(bindingContent.contentEditText, contentTextSize)
            changeEditTextTextSize(bindingContent.headerEditText, headerTextSize)
    
        }
       
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
        undoRedoHeaderEditTextHelper =
            UndoRedoHelper(binding.contentActivityInclude.headerEditText)
        
        bindingContent.headerEditText.setOnFocusChangeListener { v, hasFocus ->
            if (isHideToolbarWhileScrolling) hideToolbarWhileScrollingUseCase(
                    toolbar,
                    !hasFocus)
            invalidateOptionsMenu()
        }
        bindingContent.contentEditText.setOnFocusChangeListener { v, hasFocus ->
            if (isHideToolbarWhileScrolling) hideToolbarWhileScrollingUseCase(
                    toolbar,
                    !hasFocus)
            invalidateOptionsMenu()
        }
        binding.contentMotionLayout.attachEditText(
                bindingContent.contentEditText,
                sp.getString(
                        Constans.PreferencesKeys.disableDrawerAnimationKey,
                        Constans.PreferencesKeys.disableDrawerAnimationDV)!!)
        // Text
        val headerSpannable: Spannable? =
            jsonToSpannableUseCase(bindingContent.headerEditText, note.header)
        val contentSpannable: Spannable? =
            jsonToSpannableUseCase(bindingContent.contentEditText, note.content)
        bindingContent.headerEditText.setText(headerSpannable)
        bindingContent.contentEditText.setText(contentSpannable)
    
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
                        colorsVM.addColor(newColorItem)
                        
                    }
                    dialog.show(this@NoteActivity.supportFragmentManager, null)
                    return
                }
                val selectedEditText: EditText = getFocusedEditText() ?: return
                val selectionStart = selectedEditText.selectionStart
                val selectionEnd = selectedEditText.selectionEnd
                
                when (clickedActionMenuButton) {
                    Constans.ClickedActionButton.FOREGROUND -> {
                        changeTextForegroundColorUseCase(
                                selectedEditText,
                                colorItem.color,
                                selectionStart,
                                selectionEnd)
                    }
                    Constans.ClickedActionButton.BACKGROUND -> {
                        changeTextBackgroundColorUseCase(
                                selectedEditText,
                                colorItem.color,
                                selectionStart,
                                selectionEnd)
                    }
                    Constans.ClickedActionButton.NULL -> {}
                }
                selectedEditText.setSelection(
                        selectionStart,
                        selectionEnd)
                
            }
        })
        colorsRecyclerAdapter.setOnLongClickListener(object :
            ColorsRecyclerAdapter.OnColorItemLongClickListener {
            override fun onLongClick(
                view: View?, colorItem: ColorItem, position: Int
            ) {
                if (colorItem.type == ColorItem.ADD) {
                    val dialog = CustomBottomSheetDialog.newInstance(ColorItem.ADD)
                    dialog.setOnResetButtonClickListener {
                        colorsVM.resetColors()
                        dialog.dismiss()
                    }
                    dialog.setOnRemoveButtonClickListener() {
                        colorsVM.removeAllColors()
                        dialog.dismiss()
                    }
                    dialog.show(this@NoteActivity.supportFragmentManager, null)
                    
                } else {
                    val dialog = CustomBottomSheetDialog.newInstance(ColorItem.COLOR)
                    dialog.setOnRemoveButtonClickListener() {
                        colorsVM.removeColor(colorItem)
                        dialog.dismiss()
                    }
                    dialog.show(this@NoteActivity.supportFragmentManager, null)
                }
            }
        })
        bindingContent.actionMenuColorsRecyclerView.adapter =
            colorsRecyclerAdapter
        colorsVM.colorsList.observe(this) {
            colorsRecyclerAdapter.setList(it)
        }
        
        bindingContent.actionMenuGravityLeftImageView.setOnClickListener {
            val editText = getFocusedEditText()
            if (editText != bindingContent.contentEditText) return@setOnClickListener // don't change gravity for headerEdittext
            
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
            changeTextGravityUseCase(
                    editText,
                    Gravity.LEFT,
                    bindingContent.contentEditText.selectionStart,
                    bindingContent.contentEditText.selectionEnd)
            bindingContent.contentEditText.setSelection(
                    startSelection,
                    endSelection)
        }
        bindingContent.actionMenuGravityCenterImageView.setOnClickListener {
            val editText = getFocusedEditText()
            if (editText != bindingContent.contentEditText) return@setOnClickListener // don't change gravity for headerEdittext
            
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
            changeTextGravityUseCase(
                    editText,
                    Gravity.CENTER,
                    bindingContent.contentEditText.selectionStart,
                    bindingContent.contentEditText.selectionEnd)
            bindingContent.contentEditText.setSelection(
                    startSelection,
                    endSelection)
            
        }
        bindingContent.actionMenuGravityRightImageView.setOnClickListener {
            val editText = getFocusedEditText()
            if (editText != bindingContent.contentEditText) return@setOnClickListener // don't change gravity for headerEdittext
            
            val startSelection = bindingContent.contentEditText.selectionStart
            val endSelection = bindingContent.contentEditText.selectionEnd
            changeTextGravityUseCase(
                    editText,
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
            clickedActionMenuButton = Constans.ClickedActionButton.NULL
            
        }
        
        bindingContent.actionMenuChangeTextForegroundColor.setOnClickListener {
            clickedActionMenuButton = Constans.ClickedActionButton.FOREGROUND
            
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
            clickedActionMenuButton = Constans.ClickedActionButton.FOREGROUND
            
        }
        bindingContent.actionMenuChangeTextGravity.setOnClickListener {
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
            clickedActionMenuButton = Constans.ClickedActionButton.GRAVITY
            
        }
        bindingContent.actionMenuChangeTextBackgroundColor.setOnClickListener {
            clickedActionMenuButton = Constans.ClickedActionButton.BACKGROUND
            
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
            clickedActionMenuButton = Constans.ClickedActionButton.BACKGROUND
            
        }
        
        bindingContent.actionMenuColorsRemoveImageView.setOnClickListener {
            val selectedEditText: EditText = getFocusedEditText() ?: return@setOnClickListener
            val selectionStart = selectedEditText.selectionStart
            val selectionEnd = selectedEditText.selectionEnd
            when (clickedActionMenuButton) {
                Constans.ClickedActionButton.NULL ->
                    throw IllegalArgumentException("Constans.ClickedActionButton cannot be null when color buttons were clicked.")
                Constans.ClickedActionButton.FOREGROUND ->
                    changeTextForegroundColorUseCase(
                            selectedEditText,
                            null,
                            startPosition = selectionStart,
                            endPosition = selectionEnd)
                Constans.ClickedActionButton.BACKGROUND ->
                    changeTextBackgroundColorUseCase(
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
            clickedActionMenuButton = Constans.ClickedActionButton.NULL
            
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
            clickedActionMenuButton = Constans.ClickedActionButton.NULL
            
        }
        
        bindingContent.actionMenuButtonAddImage.setOnClickListener {
            if (getFocusedEditText() == bindingContent.contentEditText) {
                
                ImagePicker.Builder(activity = this)
                    .createIntent { resultIntent ->
                        pickImageToAddResult.launch(resultIntent)
                        
                    }
            }
            
        }
        
        bindingContent.actionMenuButtonBold.setOnClickListener {
            val editText = getFocusedEditText() ?: return@setOnClickListener
            
            makeTextBoldUseCase(editText = editText)
        }
        bindingContent.actionMenuButtonItalic.setOnClickListener {
            val editText = getFocusedEditText() ?: return@setOnClickListener
            
            makeTextItalicUseCase(editText = editText)
        }
        
        
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
                val et = getFocusedEditText()
                if (et == null) finish()
                else {
                    // clear focus and close keyboard
                    et.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(et.windowToken, 0)
                }
                
            }
            R.id.menu_note_hamburger -> {
                if (binding.drawerMenu.isDrawerOpen(Gravity.RIGHT)) {
                    binding.drawerMenu.closeDrawer(Gravity.RIGHT)
                } else {
                    binding.drawerMenu.openDrawer(Gravity.RIGHT)
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
        const val intentNotePosition = "notePositionInAdapter"
        
    }
}
    