package com.pandacorp.notesui.utils

class Constans {
    // Object to watch what NoteActivity bottom_action_menu button was clicked,
    // foreground text color or background, or button clicked state is null.
    object ClickedActionButton {
        const val KEY = "ClickedActionButton"
        const val NULL = 0
        const val FOREGROUND = 1
        const val BACKGROUND = 2
        const val GRAVITY = 3
    }
    
    object PreferencesKeys {
        const val languagesKey = "Languages"
        const val themesKey = "Themes"
        const val isShowAddNoteFABTextKey = "isShowAddNoteFABText"
        const val isHideActionBarOnScrollKey = "hide_actionbar_while_scrolling"
        const val disableDrawerAnimationKey = "sideMenuDisableAnimation"
        const val disableDrawerAnimationDV = "10 000"
        const val contentTextSizeKey = "ContentTextSize"
        const val headerTextSizeKey = "HeaderTextSize"
        const val contentTextSizeDV = "18"
        const val headerTextSizeDV = "20"
        const val versionKey = "Version"
        
        const val preferenceBundleKey = "preferenceBundleKey"
        
    }
    object Bundles {
        const val noteHeaderText = "noteHeaderText"
        const val noteContentText = "noteContentText"
        const val noteBackground = "noteBackground"
        const val noteIsShowTransparentActionBar = "noteIsShowTransparentActionBar"
    
    }
    
    companion object {
        // bundle key if value is stored in isolation and no need to create other keys
        const val valueKey = "valueKey"
    }
}