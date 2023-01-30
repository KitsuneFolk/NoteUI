package com.pandacorp.domain.usecases.utils.text

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.widget.EditText
import com.pandacorp.domain.usecases.utils.Constans

/**
 * This function inserts images at position inside edittext
 */
class InsertImageInEditTextUseCase(val context: Context) {
    operator fun invoke(editText: EditText, uri: Uri) {
        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        
        val inputStream = context.contentResolver.openInputStream(uri)
        val drawable = Drawable.createFromStream(inputStream, uri.toString())
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        
        val imageSpan = ImageSpan(drawable, uri.toString(), ImageSpan.ALIGN_BASELINE)
        val builder = SpannableStringBuilder(Constans.imgId)
        builder.setSpan(imageSpan, 0, Constans.imgId.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        editText.text.replace(selectionStart, selectionEnd, builder)
        
    }
    
}