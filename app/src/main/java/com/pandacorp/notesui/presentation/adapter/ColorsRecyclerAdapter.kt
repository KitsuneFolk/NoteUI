package com.pandacorp.notesui.presentation.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.notesui.R

class ColorsRecyclerAdapter(
    private var context: Context,
    private var itemsList: MutableList<ColorItem>
) : RecyclerView.Adapter<ColorsRecyclerAdapter.ViewHolder>() {
    
    private val TAG = "ColorsRecyclerAdapter"
    
    private var onColorItemClickListener: OnColorItemClickListener? = null
    private var onColorItemLongClickListener: OnColorItemLongClickListener? = null
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.color_item, parent, false)
        return ViewHolder(itemView)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: position = $position")
        val colorItem = itemsList[position]
        holder.imageView.setImageResource(0) // remove background to avoid bug with recycling
        when (colorItem.type){
            ColorItem.COLOR -> {
                holder.imageView.background = ColorDrawable(colorItem.color)
    
            }
            ColorItem.ADD -> {
                Log.d(TAG, "onBindViewHolder: add ")
                holder.imageView.setImageResource(R.drawable.ic_add_baseline)
    
            }
        }
        holder.imageView.setOnClickListener { v ->
            onColorItemClickListener?.onClick(v, colorItem, position)
        }
        holder.imageView.setOnLongClickListener { v ->
            onColorItemLongClickListener?.onLongClick(v, colorItem, position) ?:
            return@setOnLongClickListener false
            true
        }
    
    
    }
    
    override fun getItemViewType(position: Int): Int {
        return position
    }
    
    override fun getItemCount(): Int {
        return itemsList.size
    }
    
    fun setList(itemsList: MutableList<ColorItem>) {
        this.itemsList = itemsList
        notifyDataSetChanged()
    }
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.colorItemImageView)!!
        
    }
    
    fun setOnClickListener(onColorItemClickListener: OnColorItemClickListener) {
        this.onColorItemClickListener = onColorItemClickListener
    }
    fun setOnLongClickListener(onColorItemLongClickListener: OnColorItemLongClickListener) {
        this.onColorItemLongClickListener = onColorItemLongClickListener
    }
    interface OnColorItemClickListener {
        fun onClick(view: View?, colorItem: ColorItem, position: Int)
    }
    interface OnColorItemLongClickListener {
        fun onLongClick(view: View?, colorItem: ColorItem, position: Int)
    }
    
}
