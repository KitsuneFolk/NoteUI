package com.pandacorp.domain.usecases.utils.text

import android.widget.EditText

/**
 * @return pair of int with start text of line and.
 */
class GetEditTextSelectedLinesUseCase {
    operator fun invoke(
        editText: EditText, selectionStart: Int, selectionEnd: Int
    ): Pair<Int, Int> {
        var selectedLineStart = -1
        var selectedLineEnd = -1
        if (selectionStart != -1) {
            val firstSelectedLine = editText.layout.getLineForOffset(selectionStart)
            val lastSelectedLine = editText.layout.getLineForOffset(selectionEnd)
            selectedLineStart = editText.layout.getLineStart(firstSelectedLine)
            selectedLineEnd = editText.layout.getLineVisibleEnd(lastSelectedLine)
        }
        return Pair(selectedLineStart, selectedLineEnd)
    }
}