package com.pandacorp.noteui.presentation.utils.helpers

class Constants {
    // Watch which NoteActivity menu button was clicked
    object ClickedActionButton {
        const val NULL = 0
        const val FOREGROUND = 1
        const val BACKGROUND = 2
        const val GRAVITY = 3
    }

    object Preferences {
        const val languagesKey = "Languages"
        const val themesKey = "Themes"
        const val isShowFabTextKey = "isShowFabTextKey"
        const val isShowFabTextDefaultValue = true
        const val isHideActionBarOnScrollKey = "isHideActionBarOnScrollKey"
        const val isHideActionBarOnScrollDefaultValue = true
        const val disableDrawerAnimationKey = "disableDrawerAnimationKey"
        const val disableDrawerAnimationDefaultValue = 10000
        const val contentTextSizeKey = "ContentTextSize"
        const val titleTextSizeKey = "TitleTextSizeKey"
        const val contentTextSizeDefaultValue = 18
        const val titleTextSizeDefaultValue = 20

        const val SHOWED_DIALOG = "preferenceBundleKey"
        const val SAVED_VALUE = "SAVED_VALUE"
    }

    object DialogKey {
        const val KEY = "DialogKey"
        const val NULL = 0
        const val COLOR_DIALOG = 1
    }

    companion object {
        const val SNACKBAR_DURATION = 4_000
        const val ANIMATION_DURATION = 400L

        const val SHOW_DURATION = 200L
        const val HIDE_DURATION = 125L
    }
}