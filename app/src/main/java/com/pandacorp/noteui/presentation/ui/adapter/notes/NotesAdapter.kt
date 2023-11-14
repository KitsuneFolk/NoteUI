package com.pandacorp.noteui.presentation.ui.adapter.notes

import android.os.Bundle
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
        fun onClick(
            noteItem: NoteItem,
            position: Int
        )

        fun onLongClick(
            noteItem: NoteItem,
            position: Int
        )
    }

    class DiffCallback : DiffUtil.ItemCallback<NoteItem>() {
        override fun areItemsTheSame(
            oldItem: NoteItem,
            newItem: NoteItem
        ): Boolean = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: NoteItem,
            newItem: NoteItem
        ): Boolean = oldItem == newItem

        override fun getChangePayload(
            oldItem: NoteItem,
            newItem: NoteItem
        ): Bundle? {
            val diff = Bundle()
            if (newItem.title != oldItem.title) {
                diff.putString("title", newItem.title)
            }
            if (newItem.content != oldItem.content) {
                diff.putString("content", newItem.content)
            }
            if (newItem.background != oldItem.background) {
                diff.putString("background", newItem.background)
            }
            return if (diff.size() == 0) {
                null
            } else {
                diff
            }
        }
    }

    inner class ViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(noteItem: NoteItem) {
            bindTitle(noteItem.title)
            bindContent(noteItem.content)
            bindBackground(noteItem.background)

            binding.cardView.apply {
                setOnClickListener {
                    onNoteItemClickListener?.onClick(noteItem, bindingAdapterPosition)
                }
                setOnLongClickListener {
                    onNoteItemClickListener?.onLongClick(noteItem, bindingAdapterPosition)
                    true
                }
            }
            select(selectedNotes.get(bindingAdapterPosition, false))
        }

        fun bindTitle(title: String) {
            binding.title.setSpannableFromJson(title)
        }

        fun bindContent(content: String) {
            binding.content.setSpannableFromJson(content)
        }

        fun bindBackground(background: String) {
            Utils.changeNoteBackground(background, binding.backgroundImageView, isAdapter = true)
        }

        fun select(isSelect: Boolean) {
            if (isSelectionEnabled) {
                val tv = TypedValue()
                binding.cardView.strokeColor =
                    if (isSelect) {
                        binding.root.context.theme.resolveAttribute(android.R.attr.colorAccent, tv, true)
                        tv.data
                    } else {
                        binding.root.context.theme.resolveAttribute(android.R.attr.textColor, tv, true)
                        tv.data
                    }
            }
        }
    }

    fun setOnClickListener(onNoteItemClickListener: OnNoteItemClickListener) {
        this.onNoteItemClickListener = onNoteItemClickListener
    }

    fun selectList(newList: SparseBooleanArray) {
        repeat(currentList.size) { adapterPosition ->
            if (selectedNotes.get(adapterPosition, false) != newList.get(adapterPosition, false)) {
                notifyItemChanged(
                    adapterPosition,
                    Bundle().apply {
                        putBoolean("selection", newList[adapterPosition])
                    },
                )
            }
        }
        selectedNotes = newList
    }

    override fun submitList(list: List<NoteItem>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(ItemNoteBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(currentList[position])
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        } else {
            val bundle = payloads[0] as Bundle
            for (key in bundle.keySet()) {
                when (key) {
                    "title" -> {
                        holder.bindTitle(bundle.getString("title") ?: continue)
                    }

                    "content" -> {
                        holder.bindContent(bundle.getString("content") ?: continue)
                    }

                    "background" -> {
                        holder.bindBackground(bundle.getString("background") ?: continue)
                    }

                    "selection" -> {
                        holder.select(bundle.getBoolean("selection"))
                    }
                }
            }
        }
    }
}