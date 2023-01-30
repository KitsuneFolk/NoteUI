package com.pandacorp.domain.usecases.utils.text

import android.graphics.Color
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.core.text.toSpannable

/**
 * This method changes selected text foreground color of contentEditText
 */
class ChangeTextForegroundColorUseCase {
    operator fun invoke(
        editText: EditText, @ColorInt foregroundColor: Int?, startPosition: Int, endPosition: Int
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
}