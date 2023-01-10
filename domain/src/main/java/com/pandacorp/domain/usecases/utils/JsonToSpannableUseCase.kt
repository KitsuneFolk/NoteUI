package com.pandacorp.domain.usecases.utils

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.*
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.pandacorp.domain.usecases.utils.Constans.imgId
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class JsonToSpannableUseCase(private val context: Context) {
    private val TAG = "NoteActivity"
    
    @Throws(JSONException::class)
    operator fun invoke(view: View, jsonString: String): Spannable? {
        val json: JSONObject?
        try {
            //Check if jsonString is empty
            json = JSONObject(jsonString)
            
        } catch (jsonException: JSONException) {
            return null
        }
        val spannableString = SpannableString(json.getString(Constans.textText))
        val builder = SpannableStringBuilder(spannableString)
        try {
            val foregroundSpansJsonArray = json.getJSONArray(Constans.foregroundSpans)
            addForegroundSpans(foregroundSpansJsonArray, builder)
        } catch (e: Exception) {
            // foregroundSpansJsonArray == null
        }
        
        try {
            val backgroundSpansJsonArray = json.getJSONArray(Constans.backgroundSpans)
            addBackgroundSpans(backgroundSpansJsonArray, builder)
        } catch (e: Exception) {
            // backgroundSpansJsonArray == null
        }
        try {
            val alignmentSpansJsonArray = json.getJSONArray(Constans.alignmentSpans)
            addAlignmentSpans(alignmentSpansJsonArray, builder)
        } catch (e: Exception) {
            // alignmentSpansJsonArray == null
        }
        try {
            val imagesSpansJsonArray = json.getJSONArray(Constans.imageSpans)
            addImagesSpans(view, imagesSpansJsonArray, builder)
            
        } catch (e: Exception) {
            // imagesSpansJsonArray == null
        }
        try {
            val boldSpansJsonArray = json.getJSONArray(Constans.boldSpans)
            addBoldSpans(boldSpansJsonArray, builder)
            
        } catch (e: Exception) {
            // boldSpansJsonArray == null
        }
        try {
            val italicSpansJsonArray = json.getJSONArray(Constans.italicSpans)
            addItalicSpans(italicSpansJsonArray, builder)
            
        } catch (e: Exception) {
            // italicSpansJsonArray == null
        }
        return builder
    }
    
    private fun addForegroundSpans(jsonArray: JSONArray, spannableString: Spannable) {
        for (i in 0 until jsonArray.length()) {
            val foregroundSpan = jsonArray.getJSONObject(i)
            val foregroundColor = foregroundSpan.getInt(Constans.foregroundColor)
            val foregroundStart = foregroundSpan.getInt(Constans.foregroundStart)
            val foregroundEnd = foregroundSpan.getInt(Constans.foregroundEnd)
            spannableString.setSpan(
                    ForegroundColorSpan(foregroundColor),
                    foregroundStart,
                    foregroundEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    
    private fun addBackgroundSpans(jsonArray: JSONArray, spannableString: Spannable) {
        for (i in 0 until jsonArray.length()) {
            val backgroundSpan = jsonArray.getJSONObject(i)
            val backgroundColor = backgroundSpan.getInt(Constans.backgroundColor)
            val backgroundStart = backgroundSpan.getInt(Constans.backgroundStart)
            val backgroundEnd = backgroundSpan.getInt(Constans.backgroundEnd)
            spannableString.setSpan(
                    BackgroundColorSpan(backgroundColor),
                    backgroundStart,
                    backgroundEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    
    private fun addAlignmentSpans(jsonArray: JSONArray, spannableString: Spannable) {
        for (i in 0 until jsonArray.length()) {
            val alignmentSpan = jsonArray.getJSONObject(i)
            val alignment = when (alignmentSpan.getInt(Constans.alignmentGravity)) {
                Gravity.START -> Layout.Alignment.ALIGN_NORMAL
                Gravity.CENTER -> Layout.Alignment.ALIGN_CENTER
                Gravity.END -> Layout.Alignment.ALIGN_OPPOSITE
                else -> {
                    Layout.Alignment.ALIGN_NORMAL
                }
            }
            val alignmentStart = alignmentSpan.getInt(Constans.alignmentStart)
            val alignmentEnd = alignmentSpan.getInt(Constans.alignmentEnd)
            spannableString.setSpan(
                    AlignmentSpan.Standard(alignment),
                    alignmentStart,
                    alignmentEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
    
    private fun addImagesSpans(
        view: View,
        jsonArray: JSONArray,
        text: SpannableStringBuilder
    ) {
        for (i in 0 until jsonArray.length()) {
            val imageJson = jsonArray.getJSONObject(i)
            val stringUri = imageJson.getString(Constans.uri)
            
            val inputStream = context.contentResolver.openInputStream(Uri.parse(stringUri))
            val drawable = Drawable.createFromStream(inputStream, stringUri.toString())
            
            when (view) {
                // NoteActivity
                is EditText ->
                    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
                
                // NoteRecyclerAdapter
                is TextView -> {
                    val aspectRatio = drawable.intrinsicWidth.toFloat() / drawable.intrinsicHeight
                    
                    var width = drawable.intrinsicWidth
                    var height = drawable.intrinsicHeight
                    val maxWidth = 75
                    val maxHeight = 75
                    
                    if (width > 0 && height > 0) {
                        if (aspectRatio > 1) {
                            // Landscape image
                            if (width > maxWidth) {
                                width = maxWidth
                                height = (width / aspectRatio).toInt()
                            }
                        } else {
                            // Portrait image
                            if (height > maxHeight) {
                                height = maxHeight
                                width = (height * aspectRatio).toInt()
                            }
                        }
                    } else {
                        width = drawable.intrinsicWidth
                        height = drawable.intrinsicHeight
                    }
                    drawable.setBounds(0, 0, width, height)
                }
                
            }
            
            val start = imageJson.getInt(Constans.imageStart)
            val end = imageJson.getInt(Constans.imageEnd)
            val imageSpan = ImageSpan(drawable, stringUri, ImageSpan.ALIGN_BOTTOM)
            val builder = SpannableStringBuilder(imgId)
            builder.setSpan(imageSpan, 0, imgId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            
            text.replace(start, end, builder)
            
        }
    }
    
    private fun addBoldSpans(jsonArray: JSONArray, spannableString: Spannable) {
        for (i in 0 until jsonArray.length()) {
            val boldSpan = jsonArray.getJSONObject(i)
            val start = boldSpan.getInt(Constans.boldStart)
            val end = boldSpan.getInt(Constans.boldEnd)
            spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    
        }
    }
    private fun addItalicSpans(jsonArray: JSONArray, spannableString: Spannable) {
        for (i in 0 until jsonArray.length()) {
            val boldSpan = jsonArray.getJSONObject(i)
            val start = boldSpan.getInt(Constans.italicStart)
            val end = boldSpan.getInt(Constans.italicEnd)
            spannableString.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    
        }
    }
    
}