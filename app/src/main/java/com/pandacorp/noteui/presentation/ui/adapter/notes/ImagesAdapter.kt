package com.pandacorp.noteui.presentation.ui.adapter.notes

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pandacorp.noteui.app.databinding.ItemImageBinding

class ImagesAdapter : ListAdapter<Drawable, ImagesAdapter.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<Drawable>() {
        override fun areItemsTheSame(oldItem: Drawable, newItem: Drawable) =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: Drawable, newItem: Drawable) =
            oldItem.toString() == newItem.toString()
    }

    private var onImageItemClickListener: OnImageItemClickListener? = null

    inner class ViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(drawable: Drawable) {
            Glide.with(binding.root.context)
                .load(drawable)
                .into(binding.imageView)
            binding.root.setOnClickListener {
                onImageItemClickListener?.onClick(drawable, adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemImageBinding.inflate(inflater, parent, false))
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }


    override fun submitList(list: List<Drawable>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    fun setOnClickListener(onImageItemClickListener: OnImageItemClickListener) {
        this.onImageItemClickListener = onImageItemClickListener
    }

    fun interface OnImageItemClickListener {
        fun onClick(drawable: Drawable, position: Int)
    }
}
