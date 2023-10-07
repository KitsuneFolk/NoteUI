package com.pandacorp.noteui.presentation.utils.helpers

class Constants {
    // Watch which NoteActivity menu button was clicked
    object ClickedActionButton {
        const val NULL = 0
        const val FOREGROUND = 1
        const val BACKGROUND = 2
        const val GRAVITY = 3
    }

    object Filter {
        const val OLDEST = 0
        const val NEWEST = 1
        const val MOST_TEXT = 2
        const val LEAST_TEXT = 3
    }

    object Preferences {
        object Key {
            const val LANGUAGE = "Languages"
            const val THEME = "Themes"
            const val SHOW_FAB = "isShowFabTextKey"
            const val CONTENT_TEXT_SIZE = "ContentTextSize"
            const val TITLE_TEXT_SIZE = "TitleTextSizeKey"
            const val HIDE_ACTIONBAR_ON_SCROLL = "isHideActionBarOnScrollKey"
            const val DRAWER_ANIMATION = "disableDrawerAnimationKey"
        }

        object DefaultValue {
            const val SHOW_FAB = true
            const val HIDE_ACTIONBAR_ON_SCROLL = true
            const val DRAWER_ANIMATION = 10000
            const val CONTENT_TEXT_SIZE = 18
            const val TITLE_TEXT_SIZE = 20
            const val FILTER = Filter.OLDEST
        }
    }

    object DialogKey {
        const val KEY = "DialogKey"
        const val NULL = 0
        const val COLOR_DIALOG = 1
        const val SHOWED_DIALOG = "SHOWED_DIALOG"
        const val SAVED_VALUE = "SAVED_VALUE"
    }

    companion object {
        const val SNACKBAR_DURATION = 4_000
        const val ANIMATION_DURATION = 400L

        const val SHOW_DURATION = 200L
        const val HIDE_DURATION = 125L
    }
}