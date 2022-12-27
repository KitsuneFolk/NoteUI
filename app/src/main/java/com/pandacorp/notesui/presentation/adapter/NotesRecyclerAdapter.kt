package com.pandacorp.notesui.presentation.adapter

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.util.size
import androidx.recyclerview.widget.RecyclerView
import com.pandacorp.domain.models.NoteItem
import com.pandacorp.domain.usecases.utils.JsonToSpannableUseCase
import com.pandacorp.notesui.R
import com.pandacorp.notesui.utils.ThemeHandler
import com.pandacorp.notesui.utils.Utils
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotesRecyclerAdapter(
    private val context: Context,
    private var itemsList: MutableList<NoteItem>
) : RecyclerView.Adapter<NotesRecyclerAdapter.ViewHolder>(), KoinComponent {
    
    private val jsonToSpannableUseCase: JsonToSpannableUseCase by inject()
    
    private var onNoteItemClickListener: OnNoteItemClickListener? = null
    private var onNoteItemLongClickListener: OnNoteItemLongClickListener? = null
    
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
        val noteItem = itemsList[position]
        
        holder.header.text = jsonToSpannableUseCase(noteItem.header)
        
        holder.content.text = jsonToSpannableUseCase(noteItem.content)
        
        // Remove background to avoid bug when after removing note, a new one had background as removed one.
        holder.backgroundImageView.setImageDrawable(null)
        
        changeNoteBackground(noteItem.background, holder.backgroundImageView)
        
        holder.cardView.isActivated = selectedItemsList.get(position, false)
    
        holder.cardView.setOnClickListener { v ->
            onNoteItemClickListener?.onClick(v, noteItem, position)
        }
        holder.cardView.setOnLongClickListener { v ->
            onNoteItemLongClickListener?.onLongClick(v, noteItem, position) ?:
            return@setOnLongClickListener false
            true
        }
        
        toggleCheckedIcon(holder, position)
        
    }
    
    private fun changeNoteBackground(
        background: String, noteBackgroundImageView: ImageView
    ) {
        
        try {
            // note.background is an image drawable from Utils.backgroundImages
            val drawableResId = Utils.backgroundImages[background.toInt()]
            val drawable = ContextCompat.getDrawable(context, drawableResId)
            noteBackgroundImageView.setImageDrawable(drawable)
        } catch (e: ArrayIndexOutOfBoundsException) {
            // note.background is a color.
            val colorBackground = ThemeHandler(context).getColorBackground()
            noteBackgroundImageView.background = ColorDrawable(colorBackground)
        } catch (e: NumberFormatException) {
            // note.background is a image from storage (uri)
            noteBackgroundImageView.setImageURI(Uri.parse(background))
            
        }
        
    }
    
    //Selection methods
    private fun toggleCheckedIcon(holder: ViewHolder, position: Int) {
        val note = itemsList[position]
        val selectionColor = ContextCompat.getColor(context, R.color.note_selection_color)
        val colorPrimary = ContextCompat.getColor(context, ThemeHandler(context).getColorPrimary())
        if (selectedItemsList.get(position, false)) {
            holder.checkedImage.visibility = View.VISIBLE // show check icon
            
            holder.cardView.setCardBackgroundColor(selectionColor) // change background
            holder.backgroundImageView.setColorFilter(selectionColor) // add color filter if there is an image
            
        } else {
            holder.checkedImage.visibility = View.INVISIBLE // hide check icon
            
            holder.cardView.setCardBackgroundColor(colorPrimary) // change background
            holder.backgroundImageView.clearColorFilter() // remove color filter
        }
        if (currentSelectedIndex == position) resetCurrentIndex()
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
        for (i in 0 until selectedItemsList.size()) {
            items.add(selectedItemsList.keyAt(i))
        }
        return items
    }
    
    fun selectAllItems() {
        if (selectedItemsList.size == itemsList.size) {
            //Unselect all
            selectedItemsList.clear()
        } else {
            //Select all
            selectedItemsList.clear()
            for (i in 0 until itemsList.size) {
                selectedItemsList.put(i, true)
            }
        }
        notifyDataSetChanged()
        
        
    }
    
    fun setList(itemsList: MutableList<NoteItem>) {
        this.itemsList = itemsList
        notifyDataSetChanged()
    }
    
    fun setOnClickListener(onNoteItemClickListener: OnNoteItemClickListener) {
        this.onNoteItemClickListener = onNoteItemClickListener
    }
    fun setOnLongClickListener(onNoteItemLongClickListener: OnNoteItemLongClickListener) {
        this.onNoteItemLongClickListener = onNoteItemLongClickListener
    }
    
    interface OnNoteItemClickListener {
        fun onClick(view: View?, noteItem: NoteItem, position: Int)
    }
    interface OnNoteItemLongClickListener {
        fun onLongClick(view: View?, noteItem: NoteItem, position: Int)
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val header = itemView.findViewById<TextView>(R.id.note_item_header_textView)
        val content = itemView.findViewById<TextView>(R.id.note_item_content_textview)
        val backgroundImageView =
            itemView.findViewById<ImageView>(R.id.note_item_background_imageview)
        val checkedImage = itemView.findViewById<ImageView>(R.id.note_item_select_check_image)
        
        val cardView = itemView.findViewById<CardView>(R.id.note_item_cardView)
        
    }
    companion object {
        const val TAG = "NotesRecyclerAdapter"
    
    }
    
}