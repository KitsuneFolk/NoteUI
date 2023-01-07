package com.pandacorp.domain.usecases.utils

import android.text.Layout
import android.text.Spannable
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.view.Gravity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SpannableToJsonUseCase {
    private val TAG = "NoteActivity"
    
    @Throws(JSONException::class)
    operator fun invoke(spannable: Spannable): String {
        val json = JSONObject()
        json.put(Constans.textText, spannable.toString())
        
        // foreground spans
        val foregroundSpans = spannable.getSpans(
                0, spannable.length,
                ForegroundColorSpan::class.java)
        addForegroundSpans(spannable, json, foregroundSpans)
        
        // background spans
        val backgroundSpans = spannable.getSpans(
                0, spannable.length,
                BackgroundColorSpan::class.java)
        addBackgroundSpans(spannable, json, backgroundSpans)
        
        // alignment spans
        val alignmentSpans = spannable.getSpans(
                0, spannable.length,
                AlignmentSpan::class.java)
        addAlignmentSpan(spannable, json, alignmentSpans)
        
        // images spans
        val imagesSpans = spannable.getSpans(
                0, spannable.length,
                ImageSpan::class.java)
        addImageSpans(spannable, json, imagesSpans)
        
        return json.toString()
    }
    
    private fun addForegroundSpans(
        spannable: Spannable,
        json: JSONObject,
        spans: Array<ForegroundColorSpan>
    ) {
        val jsonArray = JSONArray()
        
        for (span in spans) {
            val foregroundColor = span.foregroundColor
            val foregroundStart = spannable.getSpanStart(span)
            val foregroundEnd = spannable.getSpanEnd(span)
            val spansJO = JSONObject()
            spansJO.put(Constans.foregroundColor, foregroundColor)
            spansJO.put(Constans.foregroundStart, foregroundStart)
            spansJO.put(Constans.foregroundEnd, foregroundEnd)
            jsonArray.put(spansJO)
        }
        json.put(Constans.foregroundSpans, jsonArray)
    }
    
    private fun addBackgroundSpans(
        spannable: Spannable,
        json: JSONObject,
        spans: Array<BackgroundColorSpan>
    ) {
        val jsonArray = JSONArray()
        
        for (span in spans) {
            val backgroundColor = span.backgroundColor
            val backgroundStart = spannable.getSpanStart(span)
            val backgroundEnd = spannable.getSpanEnd(span)
            val spansJO = JSONObject()
            spansJO.put(Constans.backgroundColor, backgroundColor)
            spansJO.put(Constans.backgroundStart, backgroundStart)
            spansJO.put(Constans.backgroundEnd, backgroundEnd)
            jsonArray.put(spansJO)
        }
        json.put(Constans.backgroundSpans, jsonArray)
    }
    
    private fun addAlignmentSpan(
        spannable: Spannable,
        json: JSONObject,
        spans: Array<AlignmentSpan>
    ) {
        val jsonArray = JSONArray()
        
        for (span in spans) {
            val alignmentInt = when (span.alignment) {
                Layout.Alignment.ALIGN_NORMAL -> Gravity.START
                Layout.Alignment.ALIGN_CENTER -> Gravity.CENTER
                Layout.Alignment.ALIGN_OPPOSITE -> Gravity.END
            }
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            val spansJO = JSONObject()
            spansJO.put(Constans.alignmentGravity, alignmentInt)
            spansJO.put(Constans.alignmentStart, start)
            spansJO.put(Constans.alignmentEnd, end)
            jsonArray.put(spansJO)
        }
        json.put(Constans.alignmentSpans, jsonArray)
    }
    
    private fun addImageSpans(spannable: Spannable, json: JSONObject, spans: Array<ImageSpan>) {
        val jsonArray = JSONArray()

        for (span in spans) {
            val start = spannable.getSpanStart(span)
            val end = spannable.getSpanEnd(span)
            val spansJO = JSONObject()
            val stringUri = span.source
            spansJO.put(Constans.uri, stringUri)
            spansJO.put(Constans.imageStart, start)
            spansJO.put(Constans.imageEnd, end)
            jsonArray.put(spansJO)
        }
        json.put(Constans.imageSpans, jsonArray)
    }
    
}