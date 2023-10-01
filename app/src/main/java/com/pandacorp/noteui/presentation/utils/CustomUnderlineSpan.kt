package com.pandacorp.noteui.presentation.utils

import android.text.style.UnderlineSpan

/**
 * Custom span that extends UnderlineSpan. Can't use UnderlineSpan directly because we don't need to save the spans set by keyboard
 */
class CustomUnderlineSpan : UnderlineSpan()