package com.pandacorp.notesui.utils

class Constans {
    // Object to watch what NoteActivity bottom_action_menu button was clicked,
    // foreground text color or background, or button clicked state is null.
    object ClickedActionButtonState {
        const val NULL = 0
        const val FOREGROUND_COLOR = 1
        const val BACKGROUND_COLOR = 2
    }
    
    object PreferencesKeys {
        const val languagesKey = "Languages"
        const val themesKey = "Themes"
        const val isShowAddNoteFABTextKey = "isShowAddNoteFABText"
        const val isHideActionBarOnScrollKey = "hide_actionbar_while_scrolling"
        const val contentTextSizeKey = "ContentTextSize"
        const val headerTextSizeKey = "HeaderTextSize"
        const val contentTextSizeDefaultValue = "18"
        const val headerTextSizeDefaultValue = "20"
        const val versionKey = "Version"
        
        const val preferenceBundleKey = "preferenceBundleKey"
        
    }
    object Bundles {
        const val noteHeaderText = "noteHeaderText"
        const val noteContentText = "noteContentText"
        const val noteBackground = "noteBackground"
        const val noteIsShowTransparentActionBar = "noteIsShowTransparentActionBar"
    
    }
}