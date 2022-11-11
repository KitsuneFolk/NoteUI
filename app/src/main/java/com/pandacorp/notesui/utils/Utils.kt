package com.pandacorp.notesui.utils

import android.text.Layout.Alignment
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class Utils {
   
    
    //This class is needed for coroutines logs work on Xiaomi devices.
    companion object {
        fun setupExceptionHandler() {
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                throw(throwable)
                
            }
        }
        
        
        private const val textText = "text"
        
        private const val foregroundSpansText = "foregroundSpans"
        private const val foregroundColorText = "foregroundColor"
        private const val foregroundStartText = "foregroundStart"
        private const val foregroundEndText = "foregroundEnd"
        
        private const val backgroundSpansText = "backgroundSpans"
        private const val backgroundColorText = "backgroundColor"
        private const val backgroundStartText = "backgroundStart"
        private const val backgroundEndText = "backgroundEnd"
        
        private const val alignmentSpansText = "alignmentSpans"
        private const val alignmentText = "alignment"
        private const val alignmentStartText = "alignmentStart"
        private const val alignmentEndText = "alignmentEnd"
        
        @Throws(JSONException::class)
        fun spannableToJson(spannable: Spannable): String {
            val json = JSONObject()
            json.put(textText, spannable.toString())
            
            val foregroundSpansJsonArray = JSONArray()
            val foregroundSpans = spannable.getSpans(
                    0, spannable.length,
                    ForegroundColorSpan::class.java)
            for (foregroundSpan in foregroundSpans) {
                val foregroundColor = foregroundSpan.foregroundColor
                val foregroundStart = spannable.getSpanStart(foregroundSpan)
                val foregroundEnd = spannable.getSpanEnd(foregroundSpan)
                val foregroundSpansJO = JSONObject()
                foregroundSpansJO.put(foregroundColorText, foregroundColor)
                foregroundSpansJO.put(foregroundStartText, foregroundStart)
                foregroundSpansJO.put(foregroundEndText, foregroundEnd)
                foregroundSpansJsonArray.put(foregroundSpansJO)
            }
            json.put(foregroundSpansText, foregroundSpansJsonArray)
            
            val backgroundSpansJsonArray = JSONArray()
            val backgroundSpans = spannable.getSpans(
                    0, spannable.length,
                    BackgroundColorSpan::class.java)
            for (backgroundSpan in backgroundSpans) {
                val backgroundColor = backgroundSpan.backgroundColor
                val backgroundStart = spannable.getSpanStart(backgroundSpan)
                val backgroundEnd = spannable.getSpanEnd(backgroundSpan)
                val backgroundSpansJO = JSONObject()
                backgroundSpansJO.put(backgroundColorText, backgroundColor)
                backgroundSpansJO.put(backgroundStartText, backgroundStart)
                backgroundSpansJO.put(backgroundEndText, backgroundEnd)
                backgroundSpansJsonArray.put(backgroundSpansJO)
            }
            json.put(backgroundSpansText, backgroundSpansJsonArray)
    
            val alignmentSpansJsonArray = JSONArray()
            val alignmentSpans = spannable.getSpans(
                    0, spannable.length,
                    AlignmentSpan::class.java)
            for (alignmentSpan in alignmentSpans) {
                val alignmentInt = when (alignmentSpan.alignment){
                    Alignment.ALIGN_NORMAL -> Gravity.START
                    Alignment.ALIGN_CENTER -> Gravity.CENTER
                    Alignment.ALIGN_OPPOSITE -> Gravity.END
                }
                val alignmentStart = spannable.getSpanStart(alignmentSpan)
                val alignmentEnd = spannable.getSpanEnd(alignmentSpan)
                val alignmentSpansJO = JSONObject()
                alignmentSpansJO.put(alignmentText, alignmentInt)
                alignmentSpansJO.put(alignmentStartText, alignmentStart)
                alignmentSpansJO.put(alignmentEndText, alignmentEnd)
                alignmentSpansJsonArray.put(alignmentSpansJO)
            }
            json.put(alignmentSpansText, alignmentSpansJsonArray)
    
            return json.toString()
        }
        
        @Throws(JSONException::class)
        fun jsonToSpannable(jsonString: String): Spannable? {
            val json: JSONObject?
            try {
                //Check if jsonString is empty
                json = JSONObject(jsonString)
                
            } catch (jsonException: JSONException) {
                Log.d("NoteActivity", "jsonToSpannable: ${jsonException.message}")
                return null
            }
            val spannableString = SpannableString(json.getString(textText))
            try {
                //Check if foregroundSpansJsonArray != null
                val foregroundSpansJsonArray = json.getJSONArray(foregroundSpansText)
                for (i in 0 until foregroundSpansJsonArray.length()) {
                    val foregroundSpan = foregroundSpansJsonArray.getJSONObject(i)
                    val foregroundColor = foregroundSpan.getInt(foregroundColorText)
                    val foregroundStart = foregroundSpan.getInt(foregroundStartText)
                    val foregroundEnd = foregroundSpan.getInt(foregroundEndText)
                    spannableString.setSpan(
                            ForegroundColorSpan(foregroundColor),
                            foregroundStart,
                            foregroundEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } catch (e: Exception) {
                Log.d("NoteActivity", "jsonToSpannable: ${e.message}")
            }
    
            try {
                //Check if backgroundSpansJsonArray != null
                val backgroundSpansJsonArray = json.getJSONArray(backgroundSpansText)
                for (i in 0 until backgroundSpansJsonArray.length()) {
                    val backgroundSpan = backgroundSpansJsonArray.getJSONObject(i)
                    val backgroundColor = backgroundSpan.getInt(backgroundColorText)
                    val backgroundStart = backgroundSpan.getInt(backgroundStartText)
                    val backgroundEnd = backgroundSpan.getInt(backgroundEndText)
                    spannableString.setSpan(
                            BackgroundColorSpan(backgroundColor),
                            backgroundStart,
                            backgroundEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } catch (e: Exception) {
                Log.d("NoteActivity", "jsonToSpannable: ${e.message}")
    
            }
            try {
                //Check if alignmentSpansJsonArray != null
                val alignmentSpansJsonArray = json.getJSONArray(alignmentSpansText)
                for (i in 0 until alignmentSpansJsonArray.length()) {
                    val alignmentSpan = alignmentSpansJsonArray.getJSONObject(i)
                    val alignment = when (alignmentSpan.getInt(alignmentText)) {
                        Gravity.START -> Alignment.ALIGN_NORMAL
                        Gravity.CENTER -> Alignment.ALIGN_CENTER
                        Gravity.END -> Alignment.ALIGN_OPPOSITE
                        else -> {Alignment.ALIGN_NORMAL}
                    }
                    val alignmentStart = alignmentSpan.getInt(alignmentStartText)
                    val alignmentEnd = alignmentSpan.getInt(alignmentEndText)
                    spannableString.setSpan(
                            AlignmentSpan.Standard(alignment),
                            alignmentStart,
                            alignmentEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } catch (e: Exception) {
                Log.d("NoteActivity", "jsonToSpannable: ${e.message}")
    
            }
    
            return spannableString
        }
    }
    
    
}