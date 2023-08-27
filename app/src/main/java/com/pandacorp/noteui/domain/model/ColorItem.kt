package com.pandacorp.noteui.domain.model

data class ColorItem(
    var id: Long = 0,
    var color: Int
) {
    companion object Type {
        // Add button to add more colors
        const val ADD = -1

        // Key to indicate that Item is a color for dialogs
        const val COLOR = 0
    }
}