package com.pandacorp.dropspinner

class DropDownItem(var text: String, var checked: Boolean = false) {
    fun toggleState() {
        checked = !checked
    }
}