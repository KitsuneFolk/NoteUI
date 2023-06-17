package com.pandacorp.noteui.data.mapper

import com.pandacorp.noteui.data.model.ColorDataItem
import com.pandacorp.noteui.domain.model.ColorItem

class ColorMapper {
    fun toColorItem(colorDataItem: ColorDataItem): ColorItem =
        ColorItem(colorDataItem.id, colorDataItem.color)

    fun toColorDataItem(colorItem: ColorItem): ColorDataItem =
        ColorDataItem(colorItem.id, colorItem.color)
}