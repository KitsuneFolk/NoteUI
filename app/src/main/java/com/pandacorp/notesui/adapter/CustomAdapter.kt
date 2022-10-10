package com.pandacorp.notesui.adapter

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pandacorp.notesui.DBHelper
import com.pandacorp.notesui.R


class CustomAdapter(
    private var context: Context,
    private var notesList: MutableList<ListItem>,
) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {
    private val TAG = "CustomAdapter"
    private lateinit var db: DBHelper
    private lateinit var wdb: SQLiteDatabase
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //Creating DBHelper object
        db = DBHelper(parent.context, null)
        
        wdb = db.writableDatabase
        
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(itemView)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notesList[position]
        
        holder.header.text = note.header
        holder.content.text = note.content
        
    }
    
    override fun getItemCount() = notesList.size
    
    fun removeItem(position: Int) {
        notesList.removeAt(position)
        
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, notesList.size)
        
    }
    
    fun filterList(filterList: ArrayList<ListItem>) {
        Log.d(TAG, "filterList: ") //-
        notesList = filterList
        notifyDataSetChanged()
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val TAG = "ViewHolder"
        val header = itemView.findViewById<TextView>(R.id.list_item_header_textView)
        val content = itemView.findViewById<TextView>(R.id.list_item_content_textview)
        
    }
    
}