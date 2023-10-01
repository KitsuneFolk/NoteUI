package com.pandacorp.noteui.domain.usecase.text

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
import com.pandacorp.noteui.domain.utils.Constants
import com.pandacorp.noteui.domain.utils.Constants.ImageSpans.IMG_ID
import com.pandacorp.noteui.presentation.utils.CustomUnderlineSpan
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class JsonToSpannableUseCase(private val context: Context) {
    @Throws(JSONException::class)
    operator fun invoke(
        type: Int,
        jsonString: String
    ): Spannable? {
        val json: JSONObject?
        try {
            // Check if jsonString is empty
            json = JSONObject(jsonString)
        } catch (jsonException: JSONException) {
            return null
        }
        val spannableString = SpannableString(json.getString(Constants.TEXT))
        val builder = SpannableStringBuilder(spannableString)
        try {
            val foregroundSpansJsonArray = json.getJSONArray(Constants.ForegroundSpans.KEY)
            addForegroundSpans(foregroundSpansJsonArray, builder)
        } catch (e: Exception) {
            // foregroundSpansJsonArray == null
        }

        try {
            val backgroundSpansJsonArray = json.getJSONArray(Constants.BackgroundSpans.KEY)
            addBackgroundSpans(backgroundSpansJsonArray, builder)
        } catch (e: Exception) {
            // backgroundSpansJsonArray == null
        }
        try {
            val alignmentSpansJsonArray = json.getJSONArray(Constants.AlignmentSpans.KEY)
            addAlignmentSpans(alignmentSpansJsonArray, builder)
        } catch (e: Exception) {
            // alignmentSpansJsonArray == null
        }
        try {
            val imagesSpansJsonArray = json.getJSONArray(Constants.ImageSpans.KEY)
            addImagesSpans(type, imagesSpansJsonArray, builder)
        } catch (e: Exception) {
            // imagesSpansJsonArray == null
        }
        try {
            val boldSpansJsonArray = json.getJSONArray(Constants.BoldSpans.KEY)
            addBoldSpans(boldSpansJsonArray, builder)
        } catch (e: Exception) {
            // boldSpansJsonArray == null
        }
        try {
            val italicSpansJsonArray = json.getJSONArray(Constants.ItalicSpans.KEY)
            addItalicSpans(italicSpansJsonArray, builder)
        } catch (e: Exception) {
            // italicSpansJsonArray == null
        }
        try {
            val underlineSpansJsonArray = json.getJSONArray(Constants.UnderlineSpans.KEY)
            addUnderlineSpans(underlineSpansJsonArray, builder)
        } catch (e: Exception) {
            // underlineSpansJsonArray == null
        }
        return builder
    }

    private fun addForegroundSpans(
        jsonArray: JSONArray,
        spannableString: Spannable
    ) {
        for (i in 0 until jsonArray.length()) {
            val foregroundSpan = jsonArray.getJSONObject(i)
            val foregroundColor = foregroundSpan.getInt(Constants.ForegroundSpans.COLOR)
            val foregroundStart = foregroundSpan.getInt(Constants.ForegroundSpans.START)
            val foregroundEnd = foregroundSpan.getInt(Constants.ForegroundSpans.END)
            spannableString.setSpan(
                ForegroundColorSpan(foregroundColor),
                foregroundStart,
                foregroundEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun addBackgroundSpans(
        jsonArray: JSONArray,
        spannableString: Spannable
    ) {
        for (i in 0 until jsonArray.length()) {
            val backgroundSpan = jsonArray.getJSONObject(i)
            val backgroundColor = backgroundSpan.getInt(Constants.BackgroundSpans.COLOR)
            val backgroundStart = backgroundSpan.getInt(Constants.BackgroundSpans.START)
            val backgroundEnd = backgroundSpan.getInt(Constants.BackgroundSpans.END)
            spannableString.setSpan(
                BackgroundColorSpan(backgroundColor),
                backgroundStart,
                backgroundEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun addAlignmentSpans(
        jsonArray: JSONArray,
        spannableString: Spannable
    ) {
        for (i in 0 until jsonArray.length()) {
            val alignmentSpan = jsonArray.getJSONObject(i)
            val alignment =
                when (alignmentSpan.getInt(Constants.AlignmentSpans.GRAVITY)) {
                    Gravity.START -> Layout.Alignment.ALIGN_NORMAL
                    Gravity.CENTER -> Layout.Alignment.ALIGN_CENTER
                    Gravity.END -> Layout.Alignment.ALIGN_OPPOSITE
                    else -> {
                        Layout.Alignment.ALIGN_NORMAL
                    }
                }
            val alignmentStart = alignmentSpan.getInt(Constants.AlignmentSpans.START)
            val alignmentEnd = alignmentSpan.getInt(Constants.AlignmentSpans.END)
            spannableString.setSpan(
                AlignmentSpan.Standard(alignment),
                alignmentStart,
                alignmentEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
            )
        }
    }

    private fun addImagesSpans(
        type: Int,
        jsonArray: JSONArray,
        text: SpannableStringBuilder
    ) {
        for (i in 0 until jsonArray.length()) {
            val imageJson = jsonArray.getJSONObject(i)
            val stringUri = imageJson.getString(Constants.ImageSpans.URI)

            val inputStream = context.contentResolver.openInputStream(Uri.parse(stringUri))
            val drawable = Drawable.createFromStream(inputStream, stringUri.toString())!!

            when (type) {
                Constants.Type.EDIT_TEXT ->
                    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)

                Constants.Type.TEXT_VIEW -> {
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

            val start = imageJson.getInt(Constants.ImageSpans.START)
            val end = imageJson.getInt(Constants.ImageSpans.END)
            val imageSpan = ImageSpan(drawable, stringUri, ImageSpan.ALIGN_BOTTOM)
            val builder = SpannableStringBuilder(IMG_ID)
            builder.setSpan(imageSpan, 0, IMG_ID.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            text.replace(start, end, builder)
        }
    }

    private fun addBoldSpans(
        jsonArray: JSONArray,
        spannableString: Spannable
    ) {
        for (i in 0 until jsonArray.length()) {
            val boldSpan = jsonArray.getJSONObject(i)
            val start = boldSpan.getInt(Constants.BoldSpans.START)
            val end = boldSpan.getInt(Constants.BoldSpans.END)
            spannableString.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun addItalicSpans(
        jsonArray: JSONArray,
        spannableString: Spannable
    ) {
        for (i in 0 until jsonArray.length()) {
            val boldSpan = jsonArray.getJSONObject(i)
            val start = boldSpan.getInt(Constants.ItalicSpans.START)
            val end = boldSpan.getInt(Constants.ItalicSpans.END)
            spannableString.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun addUnderlineSpans(
        jsonArray: JSONArray,
        spannableString: Spannable
    ) {
        for (i in 0 until jsonArray.length()) {
            val span = jsonArray.getJSONObject(i)
            val start = span.getInt(Constants.UnderlineSpans.START)
            val end = span.getInt(Constants.UnderlineSpans.END)
            spannableString.setSpan(CustomUnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}