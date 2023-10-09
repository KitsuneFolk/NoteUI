package com.pandacorp.dropspinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.AppCompatImageView

class ListViewAdapter constructor(context: Context, items: List<DropDownItem>) :
    ArrayAdapter<DropDownItem>(context, 0, items) {
        var selectedIndex = -1
        private var textColor: Int? = null

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
            val txtLabel: CustomTextView? = view?.findViewById(R.id.txt_label)
            val imgCheck: AppCompatImageView? = view?.findViewById(R.id.img_check)

            txtLabel?.text = item.text
            textColor?.let { txtLabel?.setTextColor(it) }

            if (item.checked) {
                txtLabel?.applyCustomFont(context, "roboto_bold")
                imgCheck?.visibility = View.VISIBLE
            } else {
                txtLabel?.applyCustomFont(context, "roboto_regular")
                imgCheck?.visibility = View.GONE
            }

            return view!!
        }
    }