package com.pandacorp.domain.usecases.utils

import android.text.Layout
import android.text.Spannable
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SpannableToJsonUseCase {
    @Throws(JSONException::class)
    operator fun invoke(spannable: Spannable): String {
        val json = JSONObject()
        json.put(Strings.textText, spannable.toString())
        
        val foregroundSpansJsonArray = JSONArray()
        val foregroundSpans = spannable.getSpans(
                0, spannable.length,
                ForegroundColorSpan::class.java)
        for (foregroundSpan in foregroundSpans) {
            val foregroundColor = foregroundSpan.foregroundColor
            val foregroundStart = spannable.getSpanStart(foregroundSpan)
            val foregroundEnd = spannable.getSpanEnd(foregroundSpan)
            val foregroundSpansJO = JSONObject()
            foregroundSpansJO.put(Strings.foregroundColorText, foregroundColor)
            foregroundSpansJO.put(Strings.foregroundStartText, foregroundStart)
            foregroundSpansJO.put(Strings.foregroundEndText, foregroundEnd)
            foregroundSpansJsonArray.put(foregroundSpansJO)
        }
        json.put(Strings.foregroundSpansText, foregroundSpansJsonArray)
        
        val backgroundSpansJsonArray = JSONArray()
        val backgroundSpans = spannable.getSpans(
                0, spannable.length,
                BackgroundColorSpan::class.java)
        for (backgroundSpan in backgroundSpans) {
            val backgroundColor = backgroundSpan.backgroundColor
            val backgroundStart = spannable.getSpanStart(backgroundSpan)
            val backgroundEnd = spannable.getSpanEnd(backgroundSpan)
            val backgroundSpansJO = JSONObject()
            backgroundSpansJO.put(Strings.backgroundColorText, backgroundColor)
            backgroundSpansJO.put(Strings.backgroundStartText, backgroundStart)
            backgroundSpansJO.put(Strings.backgroundEndText, backgroundEnd)
            backgroundSpansJsonArray.put(backgroundSpansJO)
        }
        json.put(Strings.backgroundSpansText, backgroundSpansJsonArray)
        
        val alignmentSpansJsonArray = JSONArray()
        val alignmentSpans = spannable.getSpans(
                0, spannable.length,
                AlignmentSpan::class.java)
        for (alignmentSpan in alignmentSpans) {
            val alignmentInt = when (alignmentSpan.alignment) {
                Layout.Alignment.ALIGN_NORMAL -> Gravity.START
                Layout.Alignment.ALIGN_CENTER -> Gravity.CENTER
                Layout.Alignment.ALIGN_OPPOSITE -> Gravity.END
            }
            val alignmentStart = spannable.getSpanStart(alignmentSpan)
            val alignmentEnd = spannable.getSpanEnd(alignmentSpan)
            val alignmentSpansJO = JSONObject()
            alignmentSpansJO.put(Strings.alignmentText, alignmentInt)
            alignmentSpansJO.put(Strings.alignmentStartText, alignmentStart)
            alignmentSpansJO.put(Strings.alignmentEndText, alignmentEnd)
            alignmentSpansJsonArray.put(alignmentSpansJO)
        }
        json.put(Strings.alignmentSpansText, alignmentSpansJsonArray)
        
        return json.toString()
    }
    
}