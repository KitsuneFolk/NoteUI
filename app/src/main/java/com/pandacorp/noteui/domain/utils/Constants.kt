package com.pandacorp.noteui.domain.utils

object Constants {
    const val TEXT = "text"

    object ForegroundSpans {
        const val KEY = "foregroundSpans"
        const val COLOR = "foregroundColor"
        const val START = "foregroundStart"
        const val END = "foregroundEnd"
    }

    object BackgroundSpans {
        const val KEY = "backgroundSpans"
        const val COLOR = "backgroundColor"
        const val START = "backgroundStart"
        const val END = "backgroundEnd"
    }

    object AlignmentSpans {
        const val KEY = "alignmentSpans"
        const val GRAVITY = "alignment"
        const val START = "alignmentStart"
        const val END = "alignmentEnd"
    }

    object ImageSpans {
        const val KEY = "imageSpans"
        const val IMG_ID = "?"
        const val URI = "uri"
        const val START = "imageStart"
        const val END = "imageEnd"
    }

    object BoldSpans {
        const val KEY = "boldSpans"
        const val START = "boldStart"
        const val END = "boldEnd"
    }

    object ItalicSpans {
        const val KEY = "italicSpans"
        const val START = "italicStart"
        const val END = "italicEnd"
    }

    object UnderlineSpans {
        const val KEY = "underlineSpans"
        const val START = "underlineStart"
        const val END = "underlineEnd"
    }

    object Type {
        const val TEXT_VIEW = 0
        const val EDIT_TEXT = 1
    }
}