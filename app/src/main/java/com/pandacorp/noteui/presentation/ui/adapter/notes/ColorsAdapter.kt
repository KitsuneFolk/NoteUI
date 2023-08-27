package com.pandacorp.noteui.presentation.ui.adapter.notes

import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.ItemColorBinding
import com.pandacorp.noteui.domain.model.ColorItem

class ColorsAdapter : ListAdapter<ColorItem, ColorsAdapter.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<ColorItem>() {
        override fun areItemsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean =
            oldItem == newItem
    }

    inner class ViewHolder(private val binding: ItemColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(colorItem: ColorItem) {
            when (colorItem.color) {
                ColorItem.ADD -> binding.imageView.setImageResource(R.drawable.ic_add_baseline)
                else -> binding.imageView.setImageDrawable(ColorDrawable(colorItem.color))
            }
            binding.cardView.setOnClickListener {
                onColorItemClickListener?.onClick(colorItem)
            }
            binding.cardView.setOnLongClickListener {
                onColorItemLongClickListener?.onLongClick(colorItem)
                    ?: return@setOnLongClickListener false
                true
            }
        }
    }

    fun interface OnColorItemClickListener {
        fun onClick(colorItem: ColorItem)
    }
    fun interface OnColorItemLongClickListener {
        fun onLongClick(colorItem: ColorItem)
    }

    private var onColorItemClickListener: OnColorItemClickListener? = null
    private var onColorItemLongClickListener: OnColorItemLongClickListener? = null

    fun setOnClickListener(onColorItemClickListener: OnColorItemClickListener) {
        this.onColorItemClickListener = onColorItemClickListener
    }
    fun setOnLongClickListener(onColorItemLongClickListener: OnColorItemLongClickListener) {
        this.onColorItemLongClickListener = onColorItemLongClickListener
    }

    override fun submitList(list: List<ColorItem>?) {
        val newList = list?.let { ArrayList(it) }
        // Add the Add button at the start of the list
        newList?.add(0, ColorItem(id = -1, color = ColorItem.ADD))
        super.submitList(newList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemColorBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}