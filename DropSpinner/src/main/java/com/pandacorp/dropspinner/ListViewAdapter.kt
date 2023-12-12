package com.pandacorp.dropspinner

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView

class ListViewAdapter(context: Context, items: List<DropDownItem>) : ArrayAdapter<DropDownItem>(context, 0, items) {
    var selectedIndex = -1
    private var textColor: Int? = null
    private var arrowColor: Int? = null

    fun setSelection(
        index: Int,
        item: DropDownItem?
    ) {
        if (selectedIndex != -1 && selectedIndex < count) {
            getItem(selectedIndex)?.toggleState()
        }
        item?.toggleState()
        notifyDataSetChanged()
        selectedIndex = index
    }

    internal fun setTextColor(color: Int) {
        this.textColor = color
    }

    internal fun setArrowColor(arrowColor: Int) {
        this.arrowColor = arrowColor
    }

    override fun getView(
        index: Int,
        convertView: View?,
        viewGroup: ViewGroup
    ): View {
        var view: View? = convertView
        val context: Context = viewGroup.context
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.dropsy_item_drop_down, viewGroup, false)
        }
        val item = getItem(index)!!
        val txtLabel: TextView? = view?.findViewById(R.id.txt_label)
        val imgCheck: AppCompatImageView? = view?.findViewById(R.id.img_check)

        txtLabel?.text = item.text
        textColor?.let { txtLabel?.setTextColor(it) }
        arrowColor?.let { imgCheck?.imageTintList = ColorStateList.valueOf(it) }

        if (item.checked) {
            txtLabel?.typeface = Typeface.DEFAULT_BOLD
            imgCheck?.visibility = View.VISIBLE
        } else {
            txtLabel?.typeface = Typeface.DEFAULT
            imgCheck?.visibility = View.GONE
        }

        return view!!
    }
}