package com.pandacorp.domain.usecases.utils.text

import android.graphics.Color
import android.text.Spannable
import android.text.style.BackgroundColorSpan
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.core.text.toSpannable

class ChangeTextBackgroundColorUseCase {
    /**
     * This method changes selected text background color of contentEditText
     */
    operator fun invoke(
        editText: EditText, @ColorInt backgroundColor: Int?, startPosition: Int, endPosition: Int
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
}