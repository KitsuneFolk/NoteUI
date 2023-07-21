package com.pandacorp.noteui.presentation.ui.adapter.notes

import android.graphics.Color
import android.util.SparseBooleanArray
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pandacorp.noteui.app.databinding.ItemNoteBinding
import com.pandacorp.noteui.domain.model.NoteItem
import com.pandacorp.noteui.presentation.utils.helpers.Utils
import com.pandacorp.noteui.presentation.utils.helpers.setSpannableFromJson

class NotesAdapter : ListAdapter<NoteItem, NotesAdapter.ViewHolder>(DiffCallback()) {
    var isSelectionEnabled = true

    private var onNoteItemClickListener: OnNoteItemClickListener? = null

    private var selectedNotes = SparseBooleanArray()

    interface OnNoteItemClickListener {
        fun onClick(noteItem: NoteItem, position: Int)
        fun onLongClick(noteItem: NoteItem, position: Int)
    }

    class DiffCallback : DiffUtil.ItemCallback<NoteItem>() {
        override fun areItemsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteItem, newItem: NoteItem): Boolean =
            oldItem == newItem
    }

    inner class ViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(noteItem: NoteItem) {
            binding.title.setSpannableFromJson(noteItem.title)
            binding.content.setSpannableFromJson(noteItem.content)

            binding.cardView.apply {
                setOnClickListener {
                    onNoteItemClickListener?.onClick(noteItem, bindingAdapterPosition)
                }
                setOnLongClickListener {
                    onNoteItemClickListener?.onLongClick(noteItem, bindingAdapterPosition)
                    true
                }
            }
            Utils.changeNoteBackground(noteItem.background, binding.backgroundImageView, isAdapter = true)
            if (isSelectionEnabled)
                selectNote(binding, selectedNotes.get(bindingAdapterPosition, false))
        }
    }

    fun setOnClickListener(onNoteItemClickListener: OnNoteItemClickListener) {
        this.onNoteItemClickListener = onNoteItemClickListener
    }

    fun selectList(newList: SparseBooleanArray) {
        repeat(currentList.size) { adapterPosition ->
            if (selectedNotes.get(adapterPosition, false) != newList.get(adapterPosition, false)) {
                notifyItemChanged(adapterPosition)
            }
        }
        selectedNotes = newList
    }

    override fun submitList(list: List<NoteItem>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemNoteBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    private fun selectNote(binding: ItemNoteBinding, isSelect: Boolean) {
        val tv = TypedValue()
        binding.root.context.theme.resolveAttribute(android.R.attr.colorAccent, tv, true)
        val selectionColor = tv.data
        binding.cardView.strokeColor = if (isSelect) selectionColor else Color.WHITE
    }
}