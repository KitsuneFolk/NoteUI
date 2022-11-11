package com.pandacorp.notesui.presentation.adapter

import android.content.Context
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.util.size
import androidx.recyclerview.widget.RecyclerView
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.notesui.R
import com.pandacorp.notesui.utils.Utils


class NotesRecyclerAdapter(
    private var context: Context,
    private var itemsList: MutableList<NoteItem>
) : RecyclerView.Adapter<NotesRecyclerAdapter.ViewHolder>() {
    private val TAG = "NotesRecyclerAdapter"
    
    private var onClickListener: OnClickListener? = null
    
    private var selectedItemsList = SparseBooleanArray()
    private var currentSelectedIndex = -1
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.note_item, parent, false)
        return ViewHolder(itemView)
    }
    
    override fun getItemCount() = itemsList.size
    
    fun getItem(position: Int): NoteItem = itemsList[position]
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemsList[position]
    
        holder.header.text = Utils.jsonToSpannable(item.header)
        
        holder.content.text = Utils.jsonToSpannable(item.content)
        Log.d(TAG, "onBindViewHolder: header.text = ${holder.header.text}")
        Log.d(TAG, "onBindViewHolder: content.text = ${holder.content.text}")
        
        holder.cardView.isActivated = selectedItemsList.get(position, false)
        
        holder.cardView.setOnClickListener(View.OnClickListener { v ->
            if (onClickListener == null) return@OnClickListener
            onClickListener!!.onItemClick(v, item, position)
        })
        
        holder.cardView.setOnLongClickListener(OnLongClickListener { v ->
            if (onClickListener == null) return@OnLongClickListener false
            onClickListener!!.onItemLongClick(v, item, position)
            true
        })
        
        toggleCheckedIcon(holder, position)
        
    }
    
    //Selection methods
    private fun toggleCheckedIcon(holder: ViewHolder, position: Int) {
        if (selectedItemsList.get(position, false)) {
            holder.checkedImage.visibility = View.VISIBLE
            if (currentSelectedIndex == position) resetCurrentIndex()
        } else {
            holder.checkedImage.visibility = View.INVISIBLE
            if (currentSelectedIndex == position) resetCurrentIndex()
        }
    }
    private fun resetCurrentIndex() {
        currentSelectedIndex = -1
    }
    fun toggleSelection(pos: Int) {
        currentSelectedIndex = pos
        if (selectedItemsList.get(pos, false)) {
            selectedItemsList.delete(pos)
        } else {
            selectedItemsList.put(pos, true)
        }
        notifyItemChanged(pos)
    }
    fun clearSelections() {
        selectedItemsList.clear()
        notifyDataSetChanged()
    }
    fun getSelectedItemCount(): Int {
        return selectedItemsList.size()
    }
    fun getSelectedItems(): List<Int> {
        val items: MutableList<Int> = ArrayList(selectedItemsList.size())
        Log.d(TAG, "getSelectedItems: selectedItemsList.size = ${selectedItemsList.size()}")
        for (i in 0 until selectedItemsList.size()) {
            items.add(selectedItemsList.keyAt(i))
        }
        return items
    }
    fun selectAllItems() {
        Log.d(TAG, "selectAllItems: start selectedItemsList.size = ${selectedItemsList.size}")
        if (selectedItemsList.size == itemsList.size) {
            //Unselect all
            Log.d(TAG, "selectAllItems: Unselect all")
            selectedItemsList.clear()
        } else {
            //Select all
            Log.d(TAG, "selectAllItems: Select all")
            selectedItemsList.clear()
            for (i in 0 until itemsList.size) {
                selectedItemsList.put(i, true)
            }
        }
        notifyDataSetChanged()
        Log.d(TAG, "selectAllItems: end selectedItemsList.size = ${selectedItemsList.size}")
        
        
    }
    
    fun filterList(filterList: ArrayList<NoteItem>) {
        itemsList = filterList
        notifyDataSetChanged()
    }
    
    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }
    
    fun setList(itemsList: MutableList<NoteItem>) {
        this.itemsList = itemsList
        notifyDataSetChanged()
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val TAG = "ViewHolder"
        val header = itemView.findViewById<TextView>(R.id.list_item_header_textView)
        val content = itemView.findViewById<TextView>(R.id.list_item_content_textview)
        val checkedImage = itemView.findViewById<ImageView>(R.id.list_item_select_check_image)
        
        val cardView = itemView.findViewById<CardView>(R.id.list_item_cardView)
        
    }
    
    interface OnClickListener {
        fun onItemClick(view: View?, item: NoteItem, position: Int)
        fun onItemLongClick(view: View?, item: NoteItem, position: Int)
    }
    
}