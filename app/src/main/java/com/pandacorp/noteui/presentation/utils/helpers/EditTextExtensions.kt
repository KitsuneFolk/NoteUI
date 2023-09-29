package com.pandacorp.noteui.presentation.utils.helpers

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Layout
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StyleSpan
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.text.toSpannable
import com.pandacorp.noteui.domain.usecase.text.JsonToSpannableUseCase
import com.pandacorp.noteui.domain.usecase.text.SpannableToJsonUseCase
import com.pandacorp.noteui.domain.utils.Constants
import com.pandacorp.noteui.domain.utils.Constants.Type.EDIT_TEXT
import com.pandacorp.noteui.domain.utils.Constants.Type.TEXT_VIEW

fun TextView.setSpannableFromJson(jsonString: String) {
    text = JsonToSpannableUseCase(context)(TEXT_VIEW, jsonString)
}

fun EditText.setSpannableFromJson(jsonString: String) {
    setText(JsonToSpannableUseCase(context)(EDIT_TEXT, jsonString))
}

fun EditText.getJson(): String = SpannableToJsonUseCase()(text)

fun EditText.getSelectedLineStart(): Int {
    var selectedLineStart = -1
    if (selectionStart != -1) {
        val firstSelectedLine = layout.getLineForOffset(selectionStart)
        selectedLineStart = layout.getLineStart(firstSelectedLine)
    }
    return selectedLineStart
}

fun EditText.getSelectedLineEnd(): Int {
    var selectedLineEnd = -1
    if (selectionStart != -1) {
        val lastSelectedLine = layout.getLineForOffset(selectionEnd)
        selectedLineEnd = layout.getLineVisibleEnd(lastSelectedLine)
    }
    return selectedLineEnd
}

fun EditText.changeTextForegroundColor(
    @ColorInt foregroundColor: Int? = null
) {
    val spannable = text.toSpannable()

    val spans: Array<ForegroundColorSpan> =
        spannable.getSpans(
            selectionStart,
            selectionEnd,
            ForegroundColorSpan::class.java,
        )
    spans.forEach {
        val selectedSpanStart = spannable.getSpanStart(it)
        val selectedSpanEnd = spannable.getSpanEnd(it)
        if (selectedSpanStart >= selectionStart && selectedSpanEnd <= selectionEnd) spannable.removeSpan(it)
    }

    spannable.setSpan(
        ForegroundColorSpan((foregroundColor ?: currentTextColor)),
        selectionStart,
        selectionEnd,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )

    val savedSelectionStart = selectionStart
    val savedSelectionEnd = selectionEnd
    text = SpannableStringBuilder(spannable)
    setSelection(savedSelectionStart, savedSelectionEnd)
}

fun EditText.changeTextBackgroundColor(
    @ColorInt backgroundColor: Int? = null
) {
    val spannable = text.toSpannable()

    val spans: Array<BackgroundColorSpan> =
        spannable.getSpans(
            selectionStart,
            selectionEnd,
            BackgroundColorSpan::class.java,
        )
    spans.forEach {
        val selectedSpanStart = spannable.getSpanStart(it)
        val selectedSpanEnd = spannable.getSpanEnd(it)
        if (selectedSpanStart >= selectionStart && selectedSpanEnd <= selectionEnd) {
            spannable.removeSpan(it)
        }
    }

    spannable.setSpan(
        BackgroundColorSpan((backgroundColor ?: Color.TRANSPARENT)),
        selectionStart,
        selectionEnd,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )

    val savedSelectionStart = selectionStart
    val savedSelectionEnd = selectionEnd
    text = SpannableStringBuilder(spannable)
    setSelection(savedSelectionStart, savedSelectionEnd)
}

fun EditText.changeTextGravity(gravity: Int) {
    val selectedLineStart = getSelectedLineStart()
    val selectedLineEnd = getSelectedLineEnd()

    val spannable = text.toSpannable()
    val spans: Array<AlignmentSpan> =
        spannable.getSpans(
            selectedLineStart,
            selectedLineEnd,
            AlignmentSpan::class.java,
        )
    spans.forEach {
        val selectedSpanStart = spannable.getSpanStart(it)
        val selectedSpanEnd = spannable.getSpanEnd(it)
        if (selectedSpanStart >= selectedLineStart && selectedSpanEnd <= selectedLineEnd) spannable.removeSpan(it)
    }

    val alignment =
        when (gravity) {
            Gravity.START -> Layout.Alignment.ALIGN_NORMAL
            Gravity.CENTER -> Layout.Alignment.ALIGN_CENTER
            Gravity.END -> Layout.Alignment.ALIGN_OPPOSITE
            else -> throw IllegalArgumentException("gravity = $gravity")
        }
    spannable.setSpan(
        AlignmentSpan.Standard(alignment),
        selectedLineStart,
        selectedLineEnd,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
    )

    val savedSelectionStart = selectionStart
    val savedSelectionEnd = selectionEnd
    text = SpannableStringBuilder(spannable)
    setSelection(savedSelectionStart, savedSelectionEnd)
}

fun EditText.insertImage(uri: Uri) {
    val inputStream = context.contentResolver.openInputStream(uri)
    val drawable =
        Drawable.createFromStream(inputStream, uri.toString())?.apply {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        } ?: return // In case user chose an invalid image

    val imageSpan = ImageSpan(drawable, uri.toString(), ImageSpan.ALIGN_BASELINE)
    val builder =
        SpannableStringBuilder(Constants.ImageSpans.IMG_ID).apply {
            setSpan(imageSpan, 0, Constants.ImageSpans.IMG_ID.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

    val savedSelectionStart = selectionStart
    text.replace(selectionStart, selectionStart, builder)
    setSelection(savedSelectionStart)
}

fun EditText.makeTextBold() {
    val spannable = text.toSpannable()

    val styleSpans = spannable.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
    val boldSpans: MutableList<StyleSpan> = mutableListOf()

    styleSpans.forEach { span ->
        if (span.style == Typeface.BOLD) boldSpans.add(span)
    }
    boldSpans.forEach { boldSpan ->
        if (spannable.getSpanStart(boldSpan) >= selectionStart && spannable.getSpanEnd(boldSpan) <= selectionEnd) {
            spannable.removeSpan(boldSpan)
        }
    }
    if (boldSpans.isEmpty()) {
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            selectionStart,
            selectionEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    } else {
        // remove the spans, what we do above
    }

    val savedSelectionStart = selectionStart
    val savedSelectionEnd = selectionEnd
    text = SpannableStringBuilder(spannable)
    setSelection(savedSelectionStart, savedSelectionEnd)
}

fun EditText.makeTextItalic() {
    val spannable = text.toSpannable()

    val italicSpans: MutableList<StyleSpan> = mutableListOf()

    spannable.getSpans(selectionStart, selectionEnd, StyleSpan::class.java).forEach { styleSpan ->
        if (styleSpan.style == Typeface.ITALIC) italicSpans.add(styleSpan)
    }

    italicSpans.forEach { italicSpan ->
        val selectedSpanStart = spannable.getSpanStart(italicSpan)
        val selectedSpanEnd = spannable.getSpanEnd(italicSpan)

        if (selectedSpanStart >= selectionStart && selectedSpanEnd <= selectionEnd) {
            spannable.removeSpan(italicSpan)
        }
    }
    if (italicSpans.isEmpty()) {
        spannable.setSpan(
            StyleSpan(Typeface.ITALIC),
            selectionStart,
            selectionEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
    } else {
        // remove the spans, what we do above
    }

    val savedSelectionStart = selectionStart
    val savedSelectionEnd = selectionEnd
    text = SpannableStringBuilder(spannable)
    setSelection(savedSelectionStart, savedSelectionEnd)
}