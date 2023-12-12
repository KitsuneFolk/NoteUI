package com.pandacorp.noteui.presentation.utils

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * An adapter designed for RecyclerView that allows you to combine it with another adapter to display scrollable views
 * above the main adapter's content.
 */

class ViewAdapter(private val view: View) : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(view)

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }
}