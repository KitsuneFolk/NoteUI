package com.pandacorp.dropspinner

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

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

            txtLabel?.text = item.text
            textColor?.let { txtLabel?.setTextColor(it) }

            if (item.checked) {
                txtLabel?.applyCustomFont(context, "roboto_bold")
            } else {
                txtLabel?.applyCustomFont(context, "roboto_regular")
            }

            return view!!
        }
    }