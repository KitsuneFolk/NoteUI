package com.pandacorp.noteui.presentation.ui.screen

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.fragula2.navigation.SwipeBackFragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.ScreenNoteBinding
import com.pandacorp.noteui.domain.model.ColorItem
import com.pandacorp.noteui.presentation.ui.adapter.notes.ColorsAdapter
import com.pandacorp.noteui.presentation.ui.adapter.notes.ImagesAdapter
import com.pandacorp.noteui.presentation.utils.dialog.CustomBottomSheetDialog
import com.pandacorp.noteui.presentation.utils.dialog.colorpicker.DialogColorPicker
import com.pandacorp.noteui.presentation.utils.helpers.Constants
import com.pandacorp.noteui.presentation.utils.helpers.PreferenceHandler
import com.pandacorp.noteui.presentation.utils.helpers.Utils
import com.pandacorp.noteui.presentation.utils.helpers.changeTextBackgroundColor
import com.pandacorp.noteui.presentation.utils.helpers.changeTextForegroundColor
import com.pandacorp.noteui.presentation.utils.helpers.changeTextGravity
import com.pandacorp.noteui.presentation.utils.helpers.getJson
import com.pandacorp.noteui.presentation.utils.helpers.hideToolbarWhileScrolling
import com.pandacorp.noteui.presentation.utils.helpers.insertImage
import com.pandacorp.noteui.presentation.utils.helpers.makeTextBold
import com.pandacorp.noteui.presentation.utils.helpers.makeTextItalic
import com.pandacorp.noteui.presentation.utils.helpers.makeTextUnderline
import com.pandacorp.noteui.presentation.utils.helpers.setDecorFitsSystemWindows
import com.pandacorp.noteui.presentation.utils.helpers.setSpannableFromJson
import com.pandacorp.noteui.presentation.utils.helpers.setTransparent
import com.pandacorp.noteui.presentation.utils.helpers.sp
import com.pandacorp.noteui.presentation.utils.views.UndoRedoHelper
import com.pandacorp.noteui.presentation.viewModels.ColorViewModel
import com.pandacorp.noteui.presentation.viewModels.CurrentNoteViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class NoteScreen : Fragment() {
    private var _binding: ScreenNoteBinding? = null
    private val binding get() = _binding!!

    private val navController by lazy { findNavController() }
    private val swipeBackFragment by lazy { requireParentFragment() as SwipeBackFragment }

    private val currentNoteViewModel: CurrentNoteViewModel by activityViewModel()
    private val colorsViewModel: ColorViewModel by viewModel()

    private val colorsAdapter by lazy {
        ColorsAdapter().apply {
            setOnClickListener { colorItem ->
                if (colorItem.color == ColorItem.ADD) {
                    addColorDialog.show()
                    return@setOnClickListener
                }
                val editText = getFocusedEditText() ?: return@setOnClickListener

                when (currentNoteViewModel.clickedActionMenuButton.value) {
                    Constants.ClickedActionButton.FOREGROUND ->
                        editText.changeTextForegroundColor(colorItem.color)

                    Constants.ClickedActionButton.BACKGROUND ->
                        editText.changeTextBackgroundColor(colorItem.color)

                    Constants.ClickedActionButton.NULL -> return@setOnClickListener
                }
            }

            setOnLongClickListener { colorItem ->
                if (colorItem.color == ColorItem.ADD) {
                    resetColorsDialog.apply {
                        setOnRemoveClickListener {
                            colorsViewModel.removeColor(colorItem)
                            dismiss()
                        }
                        show()
                    }
                } else {
                    colorClickDialog.apply {
                        setOnRemoveClickListener {
                            colorsViewModel.removeColor(colorItem)
                            dismiss()
                        }
                        show()
                    }
                }
            }

            lifecycleScope.launch {
                colorsViewModel.colorsList.collect {
                    submitList(it)
                }
            }
        }
    }
    private val imagesAdapter by lazy {
        ImagesAdapter().apply {
            setOnClickListener { drawable, position ->
                binding.noteBackgroundImageView.setImageDrawable(drawable)
                currentNoteViewModel.note.value!!.background = position.toString()
            }
            val imagesList =
                mutableListOf<Drawable>().apply {
                    Utils.backgroundDrawablesList.forEach {
                        add(ContextCompat.getDrawable(requireContext(), it)!!)
                    }
                }
            submitList(imagesList)
        }
    }

    private val addColorDialog by lazy {
        DialogColorPicker(requireActivity()).apply {
            setOnColorSelect { envelope, _ ->
                colorsViewModel.addColor(ColorItem(color = envelope.color))
            }
        }
    }
    private val resetColorsDialog by lazy {
        CustomBottomSheetDialog(requireContext(), ColorItem.ADD).apply {
            setOnResetClickListener {
                colorsViewModel.resetColors()
                dismiss()
            }
            setOnRemoveAllClickListener {
                colorsViewModel.removeAllColors()
                dismiss()
            }
        }
    }
    private val colorClickDialog by lazy {
        CustomBottomSheetDialog(requireContext(), ColorItem.COLOR)
    }

    private val isHideToolbarWhileScrolling by lazy {
        sp.getBoolean(
            Constants.Preferences.Key.HIDE_ACTIONBAR_ON_SCROLL,
            Constants.Preferences.DefaultValue.HIDE_ACTIONBAR_ON_SCROLL
        )
    }

    private lateinit var undoRedoTitleEditTextHelper: UndoRedoHelper
    private lateinit var undoRedoContentEditTextHelper: UndoRedoHelper

    private val pickNoteBackgroundImageResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = it.data!!.data

                binding.noteBackgroundImageView.setImageURI(imageUri)
                currentNoteViewModel.note.value!!.background = imageUri.toString()
            }
        }

    private val pickImageToAddResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) {
                val imageUri = it.data!!.data
                val editText: EditText =
                    getFocusedEditText()!!
                // Get a drawable from uri
                imageUri?.also {
                    editText.insertImage(imageUri)
                }
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.drawerMenu.isDrawerOpen(GravityCompat.END)) {
                        binding.drawerMenu.closeDrawer(GravityCompat.END)
                    } else {
                        navController.popBackStack()
                    }
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ScreenNoteBinding.inflate(layoutInflater.cloneInContext(requireContext()))

        initViews()

        return binding.root
    }

    override fun onPause() {
        val noteItem = currentNoteViewModel.note.value!!
        noteItem.title = binding.titleEditText.getJson()
        noteItem.content = binding.contentEditText.getJson()
        currentNoteViewModel.updateNote(noteItem)
        super.onPause()
        setDecorFitsSystemWindows(binding.root, true)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val dialogKey =
            when {
                addColorDialog.isShowing -> Constants.DialogKey.COLOR_DIALOG
                else -> Constants.DialogKey.NULL
            }
        outState.apply {
            putInt(Constants.DialogKey.KEY, dialogKey)
        }
        currentNoteViewModel.titleEditHistory.value = undoRedoTitleEditTextHelper.editHistory
        currentNoteViewModel.contentEditHistory.value = undoRedoContentEditTextHelper.editHistory
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.apply {
            when (currentNoteViewModel.clickedActionMenuButton.value) {
                Constants.ClickedActionButton.NULL -> {}

                Constants.ClickedActionButton.FOREGROUND -> {
                    binding.buttonsRoot.visibility = View.GONE
                    binding.colorsRoot.visibility = View.VISIBLE
                }

                Constants.ClickedActionButton.BACKGROUND -> {
                    binding.buttonsRoot.visibility = View.GONE
                    binding.colorsRoot.visibility = View.VISIBLE
                }

                Constants.ClickedActionButton.GRAVITY -> {
                    binding.buttonsRoot.visibility = View.GONE
                    binding.gravityRoot.visibility = View.VISIBLE
                }
            }
            // Block swiping, because android doesn't call onDrawerOpened after rotation
            if (binding.drawerMenu.isDrawerOpen(GravityCompat.END)) swipeBackFragment.setScrollingEnabled(false)
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

    override fun getContext(): Context {
        val oldContext = super.getContext()
        return ContextThemeWrapper(oldContext, PreferenceHandler.getThemeRes(oldContext!!))
    }

    private fun initViews() {
        Utils.changeNoteBackground(
            currentNoteViewModel.note.value!!.background,
            binding.noteBackgroundImageView,
            isUseGlide = false
        )

        val isHorizontal = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isHorizontal) {
                // Don't resize in the landscape orientation, due to small screen size
                requireActivity().window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                )
            } else {
                ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
                    val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                    val layoutParams = binding.actionMenuRoot.layoutParams as MarginLayoutParams
                    if (imeHeight == 0) {
                        layoutParams.bottomMargin = 0
                    } else {
                        // The inset manually set in Fragment.setDecorFitsSystemWindows
                        val bottomInset =
                            requireActivity().window.decorView.rootWindowInsets
                                .getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                        layoutParams.bottomMargin = imeHeight - bottomInset
                    }
                    binding.actionMenuRoot.layoutParams = layoutParams
                    ViewCompat.onApplyWindowInsets(v, insets)
                }
            }
        } else {
            if (isHorizontal) {
                // Don't resize in the landscape orientation, due to small screen size
                requireActivity().window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                )
            } else {
                @Suppress("DEPRECATION")
                requireActivity().window?.setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                )
            }
        }

        addStopSwipeOnTouch(binding.titleEditText)
        addStopSwipeOnTouch(binding.contentEditText)
        addStopSwipeOnTouch(binding.actionMenuRoot)
        addStopSwipeOnTouch(binding.actionMenuScrollView)
        addStopSwipeOnTouch(binding.gravityRoot)
        addStopSwipeOnTouch(binding.colorsRoot)

        val showTransparent = currentNoteViewModel.note.value!!.isShowTransparentActionBar
        binding.toolbar.setTransparent(showTransparent)
        binding.toolbar.title = null
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener {
            val editText = getFocusedEditText()
            if (editText == null) {
                navController.popBackStack()
            } else {
                // Clear focus and close the keyboard
                editText.clearFocus()
                val imm =
                    requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editText.windowToken, 0)
            }
        }
        binding.toolbar.hideToolbarWhileScrolling(isHideToolbarWhileScrolling)
        binding.toolbar.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_note, menu)
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    when (menuItem.itemId) {
                        R.id.menu_note_hamburger -> {
                            binding.drawerMenu.apply {
                                if (isDrawerOpen(GravityCompat.END)) {
                                    closeDrawer(GravityCompat.END)
                                } else {
                                    openDrawer(GravityCompat.END)
                                }
                            }
                        }

                        R.id.menu_note_extended_undo -> {
                            if (binding.titleEditText.hasFocus()) {
                                if (undoRedoTitleEditTextHelper.canUndo) {
                                    undoRedoTitleEditTextHelper.undo()
                                }
                            }

                            if (binding.contentEditText.hasFocus()) {
                                if (undoRedoContentEditTextHelper.canUndo) {
                                    undoRedoContentEditTextHelper.undo()
                                }
                            }
                        }

                        R.id.menu_note_extended_redo -> {
                            if (binding.titleEditText.hasFocus()) {
                                if (undoRedoTitleEditTextHelper.canRedo) {
                                    undoRedoTitleEditTextHelper.redo()
                                }
                            }
                            if (binding.contentEditText.hasFocus()) {
                                if (undoRedoContentEditTextHelper.canRedo) {
                                    undoRedoContentEditTextHelper.redo()
                                }
                            }
                        }
                    }
                    return true
                }
            },
            viewLifecycleOwner
        )
        binding.titleEditText.setOnFocusChangeListener { _, hasFocus ->
            if (isHideToolbarWhileScrolling) {
                binding.toolbar.hideToolbarWhileScrolling(!hasFocus)
            }
            binding.toolbar.menu.clear()
            if (hasFocus) {
                binding.toolbar.inflateMenu(R.menu.menu_note_extended)
                setDecorFitsSystemWindows(binding.root, false) // Needed to elevate the action menu on API 30+
            } else {
                binding.toolbar.inflateMenu(R.menu.menu_note)
                setDecorFitsSystemWindows(binding.root, true)
            }
        }
        binding.contentEditText.setOnFocusChangeListener { _, hasFocus ->
            if (isHideToolbarWhileScrolling) {
                binding.toolbar.hideToolbarWhileScrolling(!hasFocus)
            }
            binding.toolbar.menu.clear()
            if (hasFocus) {
                binding.toolbar.inflateMenu(R.menu.menu_note_extended)
                setDecorFitsSystemWindows(binding.root, false) // Needed to elevate the action menu on API 30+
            } else {
                binding.toolbar.inflateMenu(R.menu.menu_note)
                setDecorFitsSystemWindows(binding.root, true)
            }
        }

        binding.contentEditText.textSize =
            sp.getInt(
                Constants.Preferences.Key.CONTENT_TEXT_SIZE,
                Constants.Preferences.DefaultValue.CONTENT_TEXT_SIZE
            ).toFloat()
        binding.titleEditText.textSize =
            sp.getInt(
                Constants.Preferences.Key.TITLE_TEXT_SIZE,
                Constants.Preferences.DefaultValue.TITLE_TEXT_SIZE
            ).toFloat()

        binding.titleEditText.setSpannableFromJson(currentNoteViewModel.note.value!!.title)
        binding.contentEditText.setSpannableFromJson(currentNoteViewModel.note.value!!.content)

        undoRedoTitleEditTextHelper =
            UndoRedoHelper(binding.titleEditText, currentNoteViewModel.titleEditHistory.value!!)
        undoRedoContentEditTextHelper =
            UndoRedoHelper(binding.contentEditText, currentNoteViewModel.contentEditHistory.value!!)

        initDrawerMenu()

        initActionBottomMenu()
    }

    private fun initDrawerMenu() {
        binding.imageRecyclerView.adapter = imagesAdapter

        binding.motionDrawerLayout.let {
            it.attachEditText(
                binding.contentEditText,
                sp.getInt(
                    Constants.Preferences.Key.DRAWER_ANIMATION,
                    Constants.Preferences.DefaultValue.DRAWER_ANIMATION
                )
            )
            it.post { // The animation doesn't work outside of post
                it.loadLayoutDescription(R.xml.drawer_layout_motion_scene) // Set programmatically to remove lags
            }
        }

        binding.drawerMenu.addDrawerListener(
            object : DrawerListener {
                override fun onDrawerOpened(drawerView: View) {
                    swipeBackFragment.setScrollingEnabled(false)
                }

                override fun onDrawerClosed(drawerView: View) {
                    swipeBackFragment.setScrollingEnabled(true)
                }

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                }

                override fun onDrawerStateChanged(newState: Int) {
                    if (newState == DrawerLayout.STATE_DRAGGING) {
                        swipeBackFragment.setScrollingEnabled(false)
                    }
                }
            }
        )

        binding.expandChangeBackgroundButton.apply {
            fun rotateIcon(fromDegrees: Float, toDegrees: Float, duration: Long = 400) {
                val rotate =
                    RotateAnimation(
                        fromDegrees,
                        toDegrees,
                        Animation.RELATIVE_TO_SELF,
                        0.5f,
                        Animation.RELATIVE_TO_SELF,
                        0.5f
                    )
                rotate.duration = duration
                rotate.fillAfter = true
                binding.changeBackgroundButtonImageView.startAnimation(rotate)
            }

            // Restore icon rotation
            binding.changeBackgroundExpandableLayout.post { // Use post, because onRestoreInstanceState is not called otherwise
                if (binding.changeBackgroundExpandableLayout.isExpanded) {
                    rotateIcon(360f, 180f, 0)
                }
            }

            setOnClickListener {
                if (binding.changeBackgroundExpandableLayout.isExpanded) {
                    rotateIcon(180f, 360f)
                    binding.changeBackgroundExpandableLayout.collapse()
                } else {
                    rotateIcon(360f, 180f)
                    binding.changeBackgroundExpandableLayout.expand()
                }
            }
        }

        binding.drawerMenuSelectImageButton.setOnClickListener {
            val dm = resources.displayMetrics
            val height = dm.heightPixels.toFloat()
            val width = dm.widthPixels.toFloat()

            ImagePicker.with(activity = requireActivity())
                .crop(width, height)
                .createIntent {
                    pickNoteBackgroundImageResult.launch(it)
                }
        }
        binding.drawerMenuResetButton.setOnClickListener {
            val tv = TypedValue()
            requireContext().theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
            val colorBackground = tv.data
            binding.noteBackgroundImageView.setImageDrawable(
                ColorDrawable(colorBackground)
            )
            currentNoteViewModel.note.value!!.background = colorBackground.toString()
        }

        binding.transparentActionBarSwitch.apply {
            isChecked = currentNoteViewModel.note.value!!.isShowTransparentActionBar
            setOnCheckedChangeListener { _, isChecked ->
                binding.toolbar.setTransparent(isChecked)

                currentNoteViewModel.note.value!!.isShowTransparentActionBar = isChecked
            }
        }
    }

    private fun initActionBottomMenu() {
        binding.colorsRecyclerView.post {
            binding.colorsRecyclerView.adapter = colorsAdapter
            // Code needed to resolve the bug when RecyclerView is not scrollable inside of ViewPager
            binding.colorsRecyclerView.addOnItemTouchListener(
                object : RecyclerView.OnItemTouchListener {
                    override fun onInterceptTouchEvent(view: RecyclerView, event: MotionEvent): Boolean {
                        when (event.action) {
                            MotionEvent.ACTION_DOWN ->
                                binding.colorsRecyclerView.parent?.requestDisallowInterceptTouchEvent(true)
                        }
                        return false
                    }

                    override fun onTouchEvent(view: RecyclerView, event: MotionEvent) {
                    }

                    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
                }
            )
        }

        binding.gravityStartButton.setOnClickListener {
            val editText = getFocusedEditText()
            // Don't change the gravity for titleEditText
            if (editText != binding.contentEditText) return@setOnClickListener
            editText.changeTextGravity(Gravity.START)
        }
        binding.gravityCenterButton.setOnClickListener {
            val editText = getFocusedEditText()
            // Don't change gravity for titleEditText
            if (editText != binding.contentEditText) return@setOnClickListener
            editText.changeTextGravity(Gravity.CENTER)
        }
        binding.gravityEndButton.setOnClickListener {
            val editText = getFocusedEditText()
            // Don't change gravity for titleEditText
            if (editText != binding.contentEditText) return@setOnClickListener
            editText.changeTextGravity(Gravity.END)
        }

        binding.gravityCloseButton.setOnClickListener {
            Utils.animateViewSliding(binding.buttonsRoot, binding.gravityRoot)
            currentNoteViewModel.clickedActionMenuButton.value = Constants.ClickedActionButton.NULL
        }

        binding.changeTextForegroundColor.setOnClickListener {
            Utils.animateViewSliding(binding.colorsRoot, binding.buttonsRoot)
            currentNoteViewModel.clickedActionMenuButton.value = Constants.ClickedActionButton.FOREGROUND
        }
        binding.changeTextGravity.setOnClickListener {
            Utils.animateViewSliding(binding.gravityRoot, binding.buttonsRoot)
            currentNoteViewModel.clickedActionMenuButton.value = Constants.ClickedActionButton.GRAVITY
        }
        binding.changeTextBackgroundColor.setOnClickListener {
            Utils.animateViewSliding(binding.colorsRoot, binding.buttonsRoot)
            currentNoteViewModel.clickedActionMenuButton.value = Constants.ClickedActionButton.BACKGROUND
        }

        binding.colorsClearButton.setOnClickListener {
            val editText = getFocusedEditText() ?: return@setOnClickListener
            when (currentNoteViewModel.clickedActionMenuButton.value) {
                Constants.ClickedActionButton.FOREGROUND ->
                    editText.changeTextForegroundColor()

                Constants.ClickedActionButton.BACKGROUND ->
                    editText.changeTextBackgroundColor()

                Constants.ClickedActionButton.NULL ->
                    throw IllegalArgumentException("Value cannot be null when the color buttons were clicked.")
            }
        }
        binding.colorsClose.setOnClickListener {
            Utils.animateViewSliding(binding.buttonsRoot, binding.colorsRoot)
            currentNoteViewModel.clickedActionMenuButton.value = Constants.ClickedActionButton.NULL
        }

        binding.addImage.setOnClickListener {
            if (getFocusedEditText() == binding.contentEditText) {
                ImagePicker.Builder(activity = requireActivity()).createIntent { resultIntent ->
                    pickImageToAddResult.launch(resultIntent)
                }
            }
        }

        binding.actionMenuButtonBold.setOnClickListener {
            val editText = getFocusedEditText() ?: return@setOnClickListener
            editText.makeTextBold()
        }
        binding.actionMenuButtonItalic.setOnClickListener {
            val editText = getFocusedEditText() ?: return@setOnClickListener
            editText.makeTextItalic()
        }
        binding.actionMenuButtonUnderline.setOnClickListener {
            val editText = getFocusedEditText() ?: return@setOnClickListener
            editText.makeTextUnderline()
        }
    }

    private fun getFocusedEditText(): EditText? {
        return if (binding.titleEditText.isFocused) {
            binding.titleEditText
        } else if (binding.contentEditText.isFocused) {
            binding.contentEditText
        } else {
            null
        }
    }

    private fun addStopSwipeOnTouch(view: View) {
        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN ->
                    swipeBackFragment.setScrollingEnabled(false)

                MotionEvent.ACTION_UP -> {
                    v.performClick()
                    swipeBackFragment.setScrollingEnabled(true)
                }

                MotionEvent.ACTION_CANCEL ->
                    swipeBackFragment.setScrollingEnabled(true)
            }
            return@setOnTouchListener false
        }
    }
}