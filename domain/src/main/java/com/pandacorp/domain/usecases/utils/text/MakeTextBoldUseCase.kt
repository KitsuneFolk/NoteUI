package com.pandacorp.domain.usecases.utils.text

import android.graphics.Typeface
import android.text.Spannable
import android.text.style.StyleSpan
import android.widget.EditText
import androidx.core.text.toSpannable

/**
 * This function inserts bold span at selected edittext positions, if there is already spans it removes and adds nothing, else adds a span
 * @param editText an edittext where you need to insert bold span;
 */
class MakeTextBoldUseCase {
    operator fun invoke(editText: EditText) {
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
    
}