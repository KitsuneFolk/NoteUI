package com.pandacorp.noteui.domain.model

data class NoteItem(
    var id: Long = 0,
    var title: String = "",
    var content: String = "",
    var background: String = "",
    var isShowTransparentActionBar: Boolean = false
)