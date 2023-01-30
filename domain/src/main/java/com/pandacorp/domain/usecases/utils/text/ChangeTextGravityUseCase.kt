package com.pandacorp.domain.usecases.utils.text

import android.text.Layout
import android.text.Spannable
import android.text.style.AlignmentSpan
import android.view.Gravity
import android.widget.EditText
import androidx.annotation.GravityInt
import androidx.core.text.toSpannable

/**
 * This method changes text gravity.
 */
class ChangeTextGravityUseCase(val getEditTextSelectedLinesUseCase: GetEditTextSelectedLinesUseCase) {
    operator fun invoke(
        editText: EditText, @GravityInt gravity: Int, selectionStart: Int, selectionEnd: Int
    ) {
        val resultText = editText.text?.toSpannable() ?: return
        val selectedLinePositions =
            getEditTextSelectedLinesUseCase(editText, selectionStart, selectionEnd)
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
            }
        }
        editText.setText(resultText)
        
    }
}