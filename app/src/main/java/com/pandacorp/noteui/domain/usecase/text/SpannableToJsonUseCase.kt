package com.pandacorp.noteui.domain.usecase.text

import android.graphics.Typeface
import android.text.Layout
import android.text.Spannable
import android.text.style.*
import android.view.Gravity
import com.pandacorp.noteui.domain.utils.Constants
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SpannableToJsonUseCase {
    @Throws(JSONException::class)
    operator fun invoke(spannable: Spannable): String {
        val json = JSONObject().apply {
            put(Constants.text, spannable.toString())
        }

        // foreground spans
        val foregroundSpans =
            spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
        addForegroundSpans(spannable, json, foregroundSpans)

        // background spans
        val backgroundSpans =
            spannable.getSpans(0, spannable.length, BackgroundColorSpan::class.java)
        addBackgroundSpans(spannable, json, backgroundSpans)

        // alignment spans
        val alignmentSpans = spannable.getSpans(0, spannable.length, AlignmentSpan::class.java)
        addAlignmentSpan(spannable, json, alignmentSpans)

        // images spans
        val imagesSpans = spannable.getSpans(0, spannable.length, ImageSpan::class.java)
        addImageSpans(spannable, json, imagesSpans)

        // bold and italic spans

        val boldSpans: MutableList<StyleSpan> = mutableListOf()
        val italicSpans: MutableList<StyleSpan> = mutableListOf()
        spannable.getSpans(0, spannable.length, StyleSpan::class.java).onEach { styleSpan ->
            if (styleSpan.style == Typeface.BOLD) boldSpans.add(styleSpan)
            if (styleSpan.style == Typeface.ITALIC) italicSpans.add(styleSpan)
        }
        makeBold(spannable, json, boldSpans)
        makeItalic(spannable, json, italicSpans)

        return json.toString()
    }

    private fun addForegroundSpans(
        spannable: Spannable,
        json: JSONObject,
        spans: Array<ForegroundColorSpan>
    ) {
        val jsonArray = JSONArray()

        for (span in spans) {
            val spansJO = JSONObject().apply {
                put(Constants.ForegroundSpans.color, span.foregroundColor)
                put(Constants.ForegroundSpans.start, spannable.getSpanStart(span))
                put(Constants.ForegroundSpans.end, spannable.getSpanEnd(span))
            }
            jsonArray.put(spansJO)
        }
        json.put(Constants.ForegroundSpans.key, jsonArray)
    }

    private fun addBackgroundSpans(
        spannable: Spannable,
        json: JSONObject,
        spans: Array<BackgroundColorSpan>
    ) {
        val jsonArray = JSONArray()

        for (span in spans) {
            val spansJO = JSONObject().apply {
                put(Constants.BackgroundSpans.color, span.backgroundColor)
                put(Constants.BackgroundSpans.start, spannable.getSpanStart(span))
                put(Constants.BackgroundSpans.end, spannable.getSpanEnd(span))
            }
            jsonArray.put(spansJO)
        }
        json.put(Constants.BackgroundSpans.key, jsonArray)
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
                null -> Gravity.START
            }
            val spansJO = JSONObject().apply {
                put(Constants.AlignmentSpans.gravity, alignmentInt)
                put(Constants.AlignmentSpans.start, spannable.getSpanStart(span))
                put(Constants.AlignmentSpans.end, spannable.getSpanEnd(span))
            }

            jsonArray.put(spansJO)
        }
        json.put(Constants.AlignmentSpans.key, jsonArray)
    }

    private fun addImageSpans(spannable: Spannable, json: JSONObject, spans: Array<ImageSpan>) {
        val jsonArray = JSONArray()

        for (span in spans) {
            val stringUri = span.source
            val spansJO = JSONObject().apply {
                put(Constants.ImageSpans.uri, stringUri)
                put(Constants.ImageSpans.start, spannable.getSpanStart(span))
                put(Constants.ImageSpans.end, spannable.getSpanEnd(span))
            }
            jsonArray.put(spansJO)
        }
        json.put(Constants.ImageSpans.key, jsonArray)
    }

    private fun makeBold(spannable: Spannable, json: JSONObject, spans: List<StyleSpan>) {
        val jsonArray = JSONArray()

        for (span in spans) {
            val spansJO = JSONObject().apply {
                put(Constants.BoldSpans.start, spannable.getSpanStart(span))
                put(Constants.BoldSpans.end, spannable.getSpanEnd(span))
            }
            jsonArray.put(spansJO)
        }
        json.put(Constants.BoldSpans.key, jsonArray)
    }

    private fun makeItalic(spannable: Spannable, json: JSONObject, spans: List<StyleSpan>) {
        val jsonArray = JSONArray()
        for (span in spans) {
            val spansJO = JSONObject().apply {
                put(Constants.ItalicSpans.start, spannable.getSpanStart(span))
                put(Constants.ItalicSpans.end, spannable.getSpanEnd(span))
            }
            jsonArray.put(spansJO)
        }
        json.put(Constants.ItalicSpans.key, jsonArray)
    }
}