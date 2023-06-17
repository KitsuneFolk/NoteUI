package com.pandacorp.noteui.presentation.utils.views

import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.widget.TextView
import java.util.LinkedList

/*
 * THIS CLASS IS PROVIDED TO THE PUBLIC DOMAIN FOR FREE WITHOUT ANY
 * RESTRICTIONS OR ANY WARRANTY.
 */ /**
 * A generic undo/redo implementation for TextViews.
 */
class UndoRedoHelper(
    private val mTextView: TextView, val editHistory: EditHistory = EditHistory()
) {
    private var mIsUndoOrRedo = false

    private val mChangeListener: EditTextChangeListener

    init {
        mChangeListener = EditTextChangeListener()
        mTextView.post {
            /* Use .post {} to resolve the bug when onTextChanged called after rotation immediately,
             so call addTextChangedListener after text was restored */
            mTextView.addTextChangedListener(mChangeListener)
        }
    }

    fun disconnect() {
        mTextView.removeTextChangedListener(mChangeListener)
    }

    /**
     * Set the maximum history size. If size is negative, then history size is
     * only limited by the device memory.
     */
    fun setMaxHistorySize(maxHistorySize: Int) {
        editHistory.setMaxHistorySize(maxHistorySize)
    }

    fun clearHistory() {
        editHistory.clear()
    }

    val canUndo: Boolean
        get() {
            return editHistory.mmPosition > 0
        }

    fun undo() {
        val edit: EditItem = editHistory.getPrevious() ?: return
        val text = mTextView.editableText
        val start = edit.mmStart
        val end = start + if (edit.mmAfter != null) edit.mmAfter.length else 0
        mIsUndoOrRedo = true
        text.replace(start, end, edit.mmBefore)
        mIsUndoOrRedo = false

        // This will get rid of underlines inserted when editor tries to come
        // up with a suggestion.
        for (o in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(o)
        }
        Selection.setSelection(text, if (edit.mmBefore == null) start else start + edit.mmBefore.length)
    }

    val canRedo: Boolean
        get() {
            return editHistory.mmPosition < editHistory.mmHistory.size
        }

    fun redo() {
        val edit = editHistory.getNext() ?: return
        val text = mTextView.editableText
        val start = edit.mmStart
        val end = start + if (edit.mmBefore != null) edit.mmBefore.length else 0
        mIsUndoOrRedo = true
        text.replace(start, end, edit.mmAfter)
        mIsUndoOrRedo = false

        // This will get rid of underlines inserted when editor tries to come
        // up with a suggestion.
        for (o in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(o)
        }
        Selection.setSelection(text, if (edit.mmAfter == null) start else start + edit.mmAfter.length)
    }

    private val currentHistorySize: Int
        get() = editHistory.mmHistory.size


    class EditHistory {
        val mmHistory = LinkedList<EditItem>()

        var mmPosition = 0

        private var mmMaxHistorySize = -1

        fun clear() {
            mmPosition = 0
            mmHistory.clear()
        }

        fun add(item: EditItem) {
            while (mmHistory.size > mmPosition) {
                mmHistory.removeLast()
            }
            mmHistory.add(item)
            mmPosition++
            if (mmMaxHistorySize >= 0) {
                trimHistory()
            }
        }

        fun setMaxHistorySize(maxHistorySize: Int) {
            mmMaxHistorySize = maxHistorySize
            if (mmMaxHistorySize >= 0) {
                trimHistory()
            }
        }

        private fun trimHistory() {
            while (mmHistory.size > mmMaxHistorySize) {
                mmHistory.removeFirst()
                mmPosition--
            }
            if (mmPosition < 0) {
                mmPosition = 0
            }
        }

        fun getPrevious(): EditItem? {
            if (mmPosition == 0) {
                return null
            }
            mmPosition--
            return mmHistory[mmPosition]
        }

        fun getNext(): EditItem? {
            if (mmPosition >= mmHistory.size) {
                return null
            }
            val item = mmHistory[mmPosition]
            mmPosition++
            return item
        }
    }

    inner class EditItem(val mmStart: Int, val mmBefore: CharSequence?, val mmAfter: CharSequence?)

    inner class EditTextChangeListener : TextWatcher {
        private var mBeforeChange: CharSequence? = null
        private var mAfterChange: CharSequence? = null

        override fun beforeTextChanged(
            s: CharSequence, start: Int, count: Int,
            after: Int
        ) {
            if (mIsUndoOrRedo) return

            mBeforeChange = s.subSequence(start, start + count)
        }

        override fun onTextChanged(
            s: CharSequence, start: Int, before: Int,
            count: Int
        ) {
            if (mIsUndoOrRedo) return
            mAfterChange = s.subSequence(start, start + count)
            editHistory.add(EditItem(start, mBeforeChange, mAfterChange))
        }

        override fun afterTextChanged(s: Editable) {}
    }
}