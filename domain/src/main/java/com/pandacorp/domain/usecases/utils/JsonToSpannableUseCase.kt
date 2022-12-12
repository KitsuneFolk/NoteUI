package com.pandacorp.domain.usecases.utils

import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import org.json.JSONException
import org.json.JSONObject

class JsonToSpannableUseCase {
    private val TAG = "NoteActivity"
    @Throws(JSONException::class)
    operator fun invoke(jsonString: String): Spannable? {
        val json: JSONObject?
        try {
            //Check if jsonString is empty
            json = JSONObject(jsonString)
            
        } catch (jsonException: JSONException) {
            return null
        }
        val spannableString = SpannableString(json.getString(Strings.textText))
        try {
            //Check if foregroundSpansJsonArray != null
            val foregroundSpansJsonArray = json.getJSONArray(Strings.foregroundSpansText)
            for (i in 0 until foregroundSpansJsonArray.length()) {
                val foregroundSpan = foregroundSpansJsonArray.getJSONObject(i)
                val foregroundColor = foregroundSpan.getInt(Strings.foregroundColorText)
                val foregroundStart = foregroundSpan.getInt(Strings.foregroundStartText)
                val foregroundEnd = foregroundSpan.getInt(Strings.foregroundEndText)
                spannableString.setSpan(
                        ForegroundColorSpan(foregroundColor),
                        foregroundStart,
                        foregroundEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            // Log.d(TAG, "invoke: foregrounds = $foregroundSpansJsonArray'")
        } catch (e: Exception) {
        }
        
        try {
            //Check if backgroundSpansJsonArray != null
            val backgroundSpansJsonArray = json.getJSONArray(Strings.backgroundSpansText)
            for (i in 0 until backgroundSpansJsonArray.length()) {
                val backgroundSpan = backgroundSpansJsonArray.getJSONObject(i)
                val backgroundColor = backgroundSpan.getInt(Strings.backgroundColorText)
                val backgroundStart = backgroundSpan.getInt(Strings.backgroundStartText)
                val backgroundEnd = backgroundSpan.getInt(Strings.backgroundEndText)
                spannableString.setSpan(
                        BackgroundColorSpan(backgroundColor),
                        backgroundStart,
                        backgroundEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            // Log.d(TAG, "invoke: backgrounds = $backgroundSpansJsonArray")
        } catch (e: Exception) {
        
        }
        try {
            //Check if alignmentSpansJsonArray != null
            val alignmentSpansJsonArray = json.getJSONArray(Strings.alignmentSpansText)
            for (i in 0 until alignmentSpansJsonArray.length()) {
                val alignmentSpan = alignmentSpansJsonArray.getJSONObject(i)
                val alignment = when (alignmentSpan.getInt(Strings.alignmentText)) {
                    Gravity.START -> Layout.Alignment.ALIGN_NORMAL
                    Gravity.CENTER -> Layout.Alignment.ALIGN_CENTER
                    Gravity.END -> Layout.Alignment.ALIGN_OPPOSITE
                    else -> {
                        Layout.Alignment.ALIGN_NORMAL
                    }
                }
                val alignmentStart = alignmentSpan.getInt(Strings.alignmentStartText)
                val alignmentEnd = alignmentSpan.getInt(Strings.alignmentEndText)
                spannableString.setSpan(
                        AlignmentSpan.Standard(alignment),
                        alignmentStart,
                        alignmentEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            // Log.d(TAG, "invoke: alignments = $alignmentSpansJsonArray")
        } catch (e: Exception) {
        
        }
        return spannableString
    }
}