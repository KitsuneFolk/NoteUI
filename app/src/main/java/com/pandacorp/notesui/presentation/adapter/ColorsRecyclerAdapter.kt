package com.pandacorp.notesui.presentation.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.notesui.R

class ColorsRecyclerAdapter(
    private var context: Context,
    private var itemsList: MutableList<ColorItem>
) : RecyclerView.Adapter<ColorsRecyclerAdapter.ViewHolder>() {
    
    private val TAG = "ColorsRecyclerAdapter"
    
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.color_item, parent, false)
        return ViewHolder(itemView)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val colorItem = itemsList[position]
        //If colorItem.color == Color then set color background, else drawable.
        when (colorItem.type){
            ColorItem.COLOR -> {
                holder.imageView.background = ColorDrawable(colorItem.color)
    
            }
            ColorItem.ADD -> {
                holder.imageView.setImageDrawable(ContextCompat.getDrawable(context, colorItem.color))
    
            }
        }
        holder.imageView.setOnClickListener(View.OnClickListener { v ->
            if (onClickListener == null) return@OnClickListener
            onClickListener!!.onItemClick(v, colorItem, position)
        })
        holder.imageView.setOnLongClickListener(View.OnLongClickListener { v ->
            if (onClickListener == null) return@OnLongClickListener false
            onClickListener!!.onItemLongClick(v, colorItem, position)
            true
        })
    
    
    }
    
    override fun getItemCount(): Int {
        return itemsList.size
    }
    
    fun setList(itemsList: MutableList<ColorItem>) {
        this.itemsList = itemsList
        notifyDataSetChanged()
    }
    
    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.colorItemImageView)!!
        
    }
    
    interface OnClickListener {
        fun onItemClick(view: View?, colorItem: ColorItem, position: Int)
        fun onItemLongClick(view: View?, colorItem: ColorItem, position: Int)
    }
    
}
