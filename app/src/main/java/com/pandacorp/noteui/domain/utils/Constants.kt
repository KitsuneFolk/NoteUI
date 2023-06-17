package com.pandacorp.noteui.domain.utils

object Constants {
    const val text = "text"

    object ForegroundSpans {
        const val key = "foregroundSpans"
        const val color = "foregroundColor"
        const val start = "foregroundStart"
        const val end = "foregroundEnd"
    }

    object BackgroundSpans {
        const val key = "backgroundSpans"
        const val color = "backgroundColor"
        const val start = "backgroundStart"
        const val end = "backgroundEnd"
    }

    object AlignmentSpans {
        const val key = "alignmentSpans"
        const val gravity = "alignment"
        const val start = "alignmentStart"
        const val end = "alignmentEnd"
    }

    object ImageSpans {
        const val key = "imageSpans"
        const val imgId = "?"
        const val uri = "uri"
        const val start = "imageStart"
        const val end = "imageEnd"
    }

    object BoldSpans {
        const val key = "boldSpans"
        const val start = "boldStart"
        const val end = "boldEnd"
    }

    object ItalicSpans {
        const val key = "italicSpans"
        const val start = "italicStart"
        const val end = "italicEnd"
    }

    object Type {
        const val TEXT_VIEW = 0
        const val EDIT_TEXT = 1
    }
}
