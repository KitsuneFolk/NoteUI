package com.pandacorp.domain.usecases.notes

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.pandacorp.domain.models.NoteItem

class SetNoteBackgroundUseCase(private val context: Context) {
    operator fun invoke(note: NoteItem, backgroundImages: List<Int>, noteBackgroundImageView: ImageView) {
        try {
            // If note.background is image drawable from Utils.backgroundImages
            val drawableResId = backgroundImages[note.background.toInt()]
            val drawable = ContextCompat.getDrawable(context, drawableResId)
            noteBackgroundImageView.setImageDrawable(drawable)
        } catch (e: Exception) {
            // In case if note.background is a chosen from storage image or color.
            noteBackgroundImageView.setImageURI(Uri.parse(note.background))
            
        }
    }
    
}