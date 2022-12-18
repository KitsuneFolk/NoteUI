package com.pandacorp.domain.usecases.utils

import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.AppBarLayout

class HideToolbarWhileScrollingUseCase {
    operator fun invoke(toolbar: Toolbar, isHide: Boolean) {
        val p = toolbar.layoutParams as AppBarLayout.LayoutParams
        if (isHide) {
            p.scrollFlags = AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
        } else {
            p.scrollFlags = 0
        }
        toolbar.layoutParams = p
    }
}