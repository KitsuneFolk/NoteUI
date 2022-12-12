package com.pandacorp.notesui.controllers

import android.content.Context
import android.graphics.Color
import android.text.Layout
import android.text.Spannable
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.annotation.GravityInt
import androidx.appcompat.app.AlertDialog
import androidx.core.text.toSpannable
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.notesui.R
import com.pandacorp.notesui.databinding.ActivityNoteBinding
import com.pandacorp.notesui.presentation.NoteActivity
import com.pandacorp.notesui.presentation.adapter.ColorsRecyclerAdapter
import com.pandacorp.notesui.viewModels.NoteViewModel
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

class InitActionBottomMenuController {
    private val TAG = NoteActivity.TAG
    
    private lateinit var context: Context
    private lateinit var activity: NoteActivity
    private lateinit var note: NoteItem
    private lateinit var noteBinding: ActivityNoteBinding
    private lateinit var vm: NoteViewModel
    
    private lateinit var colorsRecyclerAdapter: ColorsRecyclerAdapter
    
    // Enum class to watch what menu button was clicked,
    // foreground text color or background, or button clicked state is null.
    private enum class ClickedActionButtonState {
        NULL,
        FOREGROUND_COLOR,
        BACKGROUND_COLOR
    }
    
    private var clickedActionButtonState: ClickedActionButtonState = ClickedActionButtonState.NULL
    
    operator fun invoke(
        context: Context,
        activity: NoteActivity,
        note: NoteItem,
        noteBinding: ActivityNoteBinding,
        vm: NoteViewModel
    ) {
        this.context = context
        this.activity = activity
        this.note = note
        this.noteBinding = noteBinding
        this.vm = vm
        
        initViews()
    }
    
    private fun initViews() {
        initColorsRecyclerView()
        initActionBottomMenu()
        
    }
    
    private fun initColorsRecyclerView() {
        colorsRecyclerAdapter = ColorsRecyclerAdapter(context, mutableListOf())
        colorsRecyclerAdapter.setOnClickListener(object : ColorsRecyclerAdapter.OnClickListener {
            override fun onItemClick(view: View?, colorItem: ColorItem, position: Int) {
                val startPosition =
                    noteBinding.contentActivityInclude.contentEditText.selectionStart
                val endPosition = noteBinding.contentActivityInclude.contentEditText.selectionEnd
                if (colorItem.type == ColorItem.ADD) {
                    //Add button clicked
                    ColorPickerDialog.Builder(context)
                        .setTitle(activity.resources.getString(R.string.alert_dialog_add_color))
                        .setPreferenceName("MyColorPickerDialog")
                        .setPositiveButton(
                                R.string.select,
                                ColorEnvelopeListener { envelope, fromUser ->
                                    val newColorItem = ColorItem(color = envelope.color)
                                    vm.addColor(newColorItem)
                                    Log.d(TAG, "onItemClick: color = ${envelope.hexCode}")
                                    
                                })
                        .setNegativeButton(
                                context.getString(android.R.string.cancel)
                        ) { dialogInterface, i -> dialogInterface.dismiss() }
                        .attachAlphaSlideBar(true)
                        .attachBrightnessSlideBar(true)
                        .setBottomSpace(12) // set a bottom space between the last slideBar and buttons.
                        .show()
                    noteBinding.contentActivityInclude.contentEditText.setSelection(
                            startPosition,
                            endPosition)
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
                noteBinding.contentActivityInclude.contentEditText.setSelection(
                        startPosition,
                        endPosition)
                
            }
            
            override fun onItemLongClick(view: View?, colorItem: ColorItem, position: Int) {
                if (colorItem.type == ColorItem.ADD) {
                    AlertDialog.Builder(context, R.style.MaterialAlertDialog)
                        .setTitle(R.string.confirm_colors_reset)
                        .setPositiveButton(R.string.reset) { dialog, which ->
                            vm.resetColors(context)
                            colorsRecyclerAdapter.notifyDataSetChanged()
                            
                            
                        }
                        .setNegativeButton(activity.getString(android.R.string.cancel)) { dialog, which ->
                            dialog.dismiss()
                            
                        }
                        .show()
                        .window!!.decorView.setBackgroundResource(R.drawable.alert_dialog_background)
                    
                } else {
                    
                    AlertDialog.Builder(context, R.style.MaterialAlertDialog)
                        .setTitle(R.string.confirm_color_remove)
                        .setPositiveButton(R.string.remove) { dialog, which ->
                            vm.removeColor(colorItem)
                            colorsRecyclerAdapter.notifyDataSetChanged()
                            
                            
                        }
                        .setNegativeButton(activity.getString(android.R.string.cancel)) { dialog, which ->
                            dialog.dismiss()
                            
                        }
                        .show()
                        .window!!.decorView.setBackgroundResource(R.drawable.alert_dialog_background)
                    
                    
                }
            }
        })
        noteBinding.contentActivityInclude.actionMenuColorsRecyclerView.adapter =
            colorsRecyclerAdapter
        vm.colorsList.observe(activity) {
            colorsRecyclerAdapter.setList(it)
        }
        
    }
    
    private fun initActionBottomMenu() {
        
        noteBinding.contentActivityInclude.actionMenuGravityLeftImageView.setOnClickListener {
            val startSelection = noteBinding.contentActivityInclude.contentEditText.selectionStart
            val endSelection = noteBinding.contentActivityInclude.contentEditText.selectionEnd
            changeTextGravity(
                    Gravity.LEFT,
                    noteBinding.contentActivityInclude.contentEditText.selectionStart,
                    noteBinding.contentActivityInclude.contentEditText.selectionEnd)
            noteBinding.contentActivityInclude.contentEditText.setSelection(
                    startSelection,
                    endSelection)
        }
        noteBinding.contentActivityInclude.actionMenuGravityCenterImageView.setOnClickListener {
            val startSelection = noteBinding.contentActivityInclude.contentEditText.selectionStart
            val endSelection = noteBinding.contentActivityInclude.contentEditText.selectionEnd
            changeTextGravity(
                    Gravity.CENTER,
                    noteBinding.contentActivityInclude.contentEditText.selectionStart,
                    noteBinding.contentActivityInclude.contentEditText.selectionEnd)
            noteBinding.contentActivityInclude.contentEditText.setSelection(
                    startSelection,
                    endSelection)
            
        }
        noteBinding.contentActivityInclude.actionMenuGravityRightImageView.setOnClickListener {
            val startSelection = noteBinding.contentActivityInclude.contentEditText.selectionStart
            val endSelection = noteBinding.contentActivityInclude.contentEditText.selectionEnd
            changeTextGravity(
                    Gravity.RIGHT,
                    noteBinding.contentActivityInclude.contentEditText.selectionStart,
                    noteBinding.contentActivityInclude.contentEditText.selectionEnd)
            noteBinding.contentActivityInclude.contentEditText.setSelection(
                    startSelection,
                    endSelection)
            
        }
        noteBinding.contentActivityInclude.actionMenuGravityCloseImageView.setOnClickListener {
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuGravityLinearLayout)
            TransitionManager.beginDelayedTransition(
                    noteBinding.contentActivityInclude.actionMenuParentLayout,
                    animation)
            noteBinding.contentActivityInclude.actionMenuGravityLinearLayout.visibility = View.GONE
            noteBinding.contentActivityInclude.actionMenuLinearLayout.visibility = View.VISIBLE
            
        }
        noteBinding.contentActivityInclude.actionMenuButtonChangeTextForegroundColor.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.FOREGROUND_COLOR
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuColorsLinearLayout)
            TransitionManager.beginDelayedTransition(
                    noteBinding.contentActivityInclude.actionMenuParentLayout,
                    animation)
            noteBinding.contentActivityInclude.actionMenuLinearLayout.visibility = View.GONE
            noteBinding.contentActivityInclude.actionMenuColorsLinearLayout.visibility =
                View.VISIBLE
            
        }
        noteBinding.contentActivityInclude.actionMenuButtonChangeTextGravity.setOnClickListener {
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuGravityLinearLayout)
            TransitionManager.beginDelayedTransition(
                    noteBinding.contentActivityInclude.actionMenuParentLayout,
                    animation)
            noteBinding.contentActivityInclude.actionMenuLinearLayout.visibility = View.GONE
            noteBinding.contentActivityInclude.actionMenuGravityLinearLayout.visibility =
                View.VISIBLE
            
        }
        noteBinding.contentActivityInclude.actionMenuButtonChangeTextBackgroundColor.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.BACKGROUND_COLOR
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuColorsLinearLayout)
            animation.addTarget(R.id.actionMenuColorsRecyclerView)
            TransitionManager.beginDelayedTransition(
                    noteBinding.contentActivityInclude.actionMenuParentLayout,
                    animation)
            noteBinding.contentActivityInclude.actionMenuLinearLayout.visibility = View.GONE
            noteBinding.contentActivityInclude.actionMenuColorsLinearLayout.visibility =
                View.VISIBLE
            
            
        }
        
        noteBinding.contentActivityInclude.actionMenuColorsRemoveImageView.setOnClickListener {
            val startSelection = noteBinding.contentActivityInclude.contentEditText.selectionStart
            val endSelection = noteBinding.contentActivityInclude.contentEditText.selectionEnd
            when (clickedActionButtonState) {
                ClickedActionButtonState.NULL ->
                    throw Exception("clickedActionButtonState cannot be null when color buttons were clicked.")
                ClickedActionButtonState.FOREGROUND_COLOR ->
                    changeTextForegroundColor(
                            null,
                            startPosition = startSelection,
                            endPosition = endSelection)
                ClickedActionButtonState.BACKGROUND_COLOR ->
                    changeTextBackgroundColor(
                            null,
                            startPosition = startSelection,
                            endPosition = endSelection)
                
            }
            noteBinding.contentActivityInclude.contentEditText.setSelection(
                    startSelection,
                    endSelection)
            
            
        }
        noteBinding.contentActivityInclude.noteActionColorsCloseImageButton.setOnClickListener {
            clickedActionButtonState = ClickedActionButtonState.NULL
            
            //Slide Animation
            val animation = Slide(Gravity.BOTTOM)
            animation.duration = 400
            animation.addTarget(R.id.actionMenuLinearLayout)
            animation.addTarget(R.id.actionMenuColorsLinearLayout)
            animation.addTarget(R.id.actionMenuColorsRecyclerView)
            TransitionManager.beginDelayedTransition(
                    noteBinding.contentActivityInclude.actionMenuParentLayout,
                    animation)
            noteBinding.contentActivityInclude.actionMenuColorsLinearLayout.visibility = View.GONE
            noteBinding.contentActivityInclude.actionMenuLinearLayout.visibility = View.VISIBLE
            
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
        val resultText: Spannable =
            noteBinding.contentActivityInclude.contentEditText.text?.toSpannable() ?: return
        
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
        
        noteBinding.contentActivityInclude.contentEditText.setText(resultText)
        
    }
    
    /**
     * This method changes selected text background color of contentEditText
     */
    private fun changeTextBackgroundColor(
        @ColorInt backgroundColor: Int?,
        startPosition: Int,
        endPosition: Int
    ) {
        val resultText: Spannable =
            noteBinding.contentActivityInclude.contentEditText.text?.toSpannable() ?: return
        
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
        
        noteBinding.contentActivityInclude.contentEditText.setText(resultText)
        
        
    }
    
    /**
     * This method changes text gravity.
     */
    private fun changeTextGravity(
        @GravityInt gravity: Int,
        selectionStart: Int,
        selectionEnd: Int
    ) {
        val resultText =
            noteBinding.contentActivityInclude.contentEditText.text?.toSpannable() ?: return
        val selectedLinePositions =
            getEditTextSelectedLineTextBySelection(
                    noteBinding.contentActivityInclude.contentEditText,
                    selectionStart, selectionEnd)
        val firstSelectedLineStart = selectedLinePositions.first
        val lastSelectedLineEnd = selectedLinePositions.second
        
        val spans: Array<AlignmentSpan> = resultText.getSpans(
                firstSelectedLineStart, lastSelectedLineEnd,
                AlignmentSpan::class.java)
        repeat(spans.count()) {
            resultText.removeSpan(spans[it])
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
                noteBinding.contentActivityInclude.contentEditText.setText(resultText)
            }
        }
        noteBinding.contentActivityInclude.contentEditText.setText(resultText)
        
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
        Log.d(
                TAG,
                "changeTextGravity: selectionStart = $selectionStart, selectionEnd = $selectionEnd")
        if (selectionStart != -1) {
            val firstSelectedLine = editText.layout.getLineForOffset(selectionStart)
            val lastSelectedLine = editText.layout.getLineForOffset(selectionEnd)
            Log.d(
                    TAG,
                    "getEditTextSelectedLineTextBySelection: firstSelectedLine = $firstSelectedLine, lastSelectedLine = $lastSelectedLine")
            selectedLineStart = editText.layout.getLineStart(firstSelectedLine)
            selectedLineEnd = editText.layout.getLineVisibleEnd(lastSelectedLine)
        }
        Log.d(
                TAG,
                "getEditTextSelectedLineTextBySelection: selectedLineStart = $selectedLineStart, selectedLineEnd = $selectedLineEnd")
        return Pair(selectedLineStart, selectedLineEnd)
    }
}
    
