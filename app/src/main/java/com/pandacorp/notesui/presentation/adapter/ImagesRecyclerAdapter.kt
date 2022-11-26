package com.pandacorp.notesui.presentation.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pandacorp.notesui.R

class ImagesRecyclerAdapter(
    private var context: Context,
    private var drawablesList: MutableList<Drawable>
) : RecyclerView.Adapter<ImagesRecyclerAdapter.ViewHolder>() {
    
    private val TAG = "ImagesRecyclerAdapter"
    
    private var onClickListener: OnClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false)
        return ViewHolder(itemView)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder: ")
        val drawable = drawablesList[position]
        holder.imageView.setImageDrawable(drawable)
        holder.itemView.setOnClickListener {
            onClickListener?.onItemClick(holder.imageView, drawable, position)
            onClickListener?.onItemLongClick(holder.imageView, drawable, position)
        }
        
    }
    
    override fun getItemCount(): Int {
        return drawablesList.size
    }
    
    fun setList(drawablesList: MutableList<Drawable>) {
        this.drawablesList = drawablesList
        notifyDataSetChanged()
    }
    
    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.onClickListener = onClickListener
    }
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imageItemImageView)
    }
    
    interface OnClickListener {
        fun onItemClick(view: View?, drawable: Drawable, position: Int)
        fun onItemLongClick(view: View?, drawable: Drawable, position: Int)
    }
    
}
