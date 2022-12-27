package com.pandacorp.notesui.presentation.adapter

import android.content.Context
import android.graphics.drawable.Drawable
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
    
    private var onImageItemClickListener: OnImageItemClickListener? = null
    private var onImageItemLongClickListener: OnImageItemLongClickListener? = null
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false)
        return ViewHolder(itemView)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val drawable = drawablesList[position]
        holder.imageView.setImageDrawable(drawable)
        holder.itemView.setOnClickListener {
            onImageItemClickListener?.onClick(holder.imageView, drawable, position)
            onImageItemLongClickListener?.onLongClick(holder.imageView, drawable, position)
        }
        
    }
    
    override fun getItemCount(): Int {
        return drawablesList.size
    }
    
    fun setList(drawablesList: MutableList<Drawable>) {
        this.drawablesList = drawablesList
        notifyDataSetChanged()
    }
    
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imageItemImageView)
    }
    
    fun setOnClickListener(onImageItemClickListener: OnImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener
    }
    fun setOnLongClickListener(onImageItemLongClickListener: OnImageItemLongClickListener) {
        this.onImageItemLongClickListener = onImageItemLongClickListener
    }
    
    interface OnImageItemClickListener {
        fun onClick(view: View?, drawable: Drawable, position: Int)
    }
    interface OnImageItemLongClickListener {
        fun onLongClick(view: View?, drawable: Drawable, position: Int)
    }
}
