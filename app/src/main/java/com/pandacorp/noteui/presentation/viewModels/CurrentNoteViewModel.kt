package com.pandacorp.noteui.presentation.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.domain.usecase.note.UpdateNoteUseCase
import com.pandacorp.noteui.presentation.utils.helpers.Constants
import com.pandacorp.noteui.presentation.utils.views.UndoRedoHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CurrentNoteViewModel(private val updateNoteUseCase: UpdateNoteUseCase) : ViewModel() {
    private val _note = MutableLiveData<NoteItem>()
    val note: MutableLiveData<NoteItem> = _note

    var clickedActionMenuButton = MutableLiveData(Constants.ClickedActionButton.NULL)

    val titleEditHistory = MutableLiveData(UndoRedoHelper.EditHistory())
    val contentEditHistory = MutableLiveData(UndoRedoHelper.EditHistory())

    fun updateNote(noteItem: NoteItem) {
        _note.value = noteItem
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                updateNoteUseCase(noteItem)
            }
        }
    }

    fun setNote(noteItem: NoteItem) {
        viewModelScope.launch {
            _note.postValue(noteItem)
        }
    }
}
