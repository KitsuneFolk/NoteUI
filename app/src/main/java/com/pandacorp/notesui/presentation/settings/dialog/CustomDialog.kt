package com.pandacorp.notesui.presentation.settings.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.DialogFragment

abstract class CustomDialog: DialogFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireDialog().window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // remove default background so that dialog can be rounded
        requireDialog().window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND) // remove shadow
        super.onViewCreated(view, savedInstanceState)
        
    }
}