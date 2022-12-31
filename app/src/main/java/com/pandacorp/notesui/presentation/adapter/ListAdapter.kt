package com.pandacorp.notesui.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.pandacorp.notesui.R
import com.pandacorp.notesui.presentation.activities.MainActivity
import com.pandacorp.notesui.presentation.settings.ListItem
import com.pandacorp.notesui.utils.Constans

class ListAdapter(
    context: Context, languagesList: MutableList<ListItem>, private val preferenceKey: String
) : ArrayAdapter<ListItem>(context, 0, languagesList) {
    private val TAG = MainActivity.TAG
    
    private var onListItemClickListener: OnListItemClickListener? = null
    private var onListItemLongClickListener: OnListItemLongClickListener? = null
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        }
        val listItem = getItem(position)!!
        
        val layout = view!!.findViewById<ConstraintLayout>(R.id.ListItemLayout)
        val textView = view.findViewById<TextView>(R.id.ListItemTextView)
        val cardView = view.findViewById<CardView>(R.id.ListItemCardView)
        val imageView = view.findViewById<ImageView>(R.id.ListItemImageView)
        
        layout.setOnClickListener { v ->
            onListItemClickListener?.onClick(v, listItem, position)
        }
        layout.setOnLongClickListener { v ->
            onListItemLongClickListener?.onLongClick(v, listItem, position) ?:
            return@setOnLongClickListener false
            true
        }
        
        textView.text = listItem.title
        imageView.setImageDrawable(listItem.drawable)
        // make imageview rounded if key == theme
        if (preferenceKey == Constans.PreferencesKeys.themesKey){
            cardView.radius  = 80f
        }
        return view
    }
    
    fun setOnClickListener(onListItemClickListener: OnListItemClickListener) {
        this.onListItemClickListener = onListItemClickListener
    }
    fun setOnLongClickListener(onListItemLongClickListener: OnListItemLongClickListener) {
        this.onListItemLongClickListener = onListItemLongClickListener
    }
    
    interface OnListItemClickListener {
        fun onClick(view: View?, listItem: ListItem, position: Int)
    }
    interface OnListItemLongClickListener {
        fun onLongClick(view: View?, listItem: ListItem, position: Int)
    }
}
    
