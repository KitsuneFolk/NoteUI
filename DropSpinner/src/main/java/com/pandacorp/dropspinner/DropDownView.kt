package com.pandacorp.dropspinner

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.pandacorp.animatedtextview.AnimatedTextView
import net.cachapa.expandablelayout.ExpandableLayout

class DropDownView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MaterialCardView(context, attrs, defStyleAttr), View.OnClickListener {
    fun interface ItemClickListener {
        fun onItemClick(
            position: Int,
            item: DropDownItem
        )
    }

    private var listener: ItemClickListener? = null
    private lateinit var items: List<DropDownItem>
    private lateinit var listViewAdapter: ListViewAdapter
    private val expandableLayout: ExpandableLayout
    private val listView: ListView
    private val imageArrow: AppCompatImageView
    private val label: TextView
    private val value: AnimatedTextView

    init {
        // Add body layout
        val dropDownBody =
            LayoutInflater.from(context).inflate(
                R.layout.dropsy_layout_drop_down,
                this,
                false,
            ) as LinearLayout
        addView(dropDownBody)
        expandableLayout = findViewById(R.id.expandableLayout)
        listView = findViewById(R.id.listView)
        imageArrow = findViewById(R.id.img_arrow)
        label = findViewById(R.id.txt_drop_drown_label)
        value = findViewById(R.id.txt_drop_drown_value)

        // Get attrs
        val dropsyAttrs =
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.DropDownView,
                0,
                0,
            )
        initData(dropsyAttrs)
        setStyles(dropsyAttrs)
        dropsyAttrs.recycle()

        setOnClickListener(this)
    }

    fun setItemClickListener(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onClick(v: View?) {
        if (expandableLayout.isExpanded) {
            hideListView()
        } else {
            showListView()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return Bundle().apply {
            putParcelable("superState", super.onSaveInstanceState())
            putInt("selectedPosition", listViewAdapter.selectedIndex)
            putBoolean("isExpanded", expandableLayout.isExpanded)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null) {
            super.onRestoreInstanceState(null)
            return
        }
        val viewState = (state as Bundle)
        if (viewState.getBoolean("isExpanded")) {
            showListView(false)
        } else {
            hideListView(false)
        }
        val position = viewState.getInt("selectedPosition")
        val item = items[position]
        value.setText(item.text, withAnimation = false, moveDown = false)
        listViewAdapter.setSelection(position, item)
        super.onRestoreInstanceState(state.getParcelableExtraSupport("superState", Parcelable::class.java))
    }

    private fun showListView(withAnimation: Boolean = true) {
        post {
            animateExpand(withAnimation)
        }
    }

    private fun hideListView(withAnimation: Boolean = true) {
        postDelayed({
            animateCollapse(withAnimation)
        }, if (withAnimation) 150 else 0)
    }

    private fun setStyles(dropsyAttrs: TypedArray) {
        val resources = context.resources
        val dropsyLabelColor =
            dropsyAttrs.getColor(
                R.styleable.DropDownView_dropsyLabelColor,
                ContextCompat.getColor(context, R.color.dropsy_text_color_secondary),
            )
        val dropsyValueColor =
            dropsyAttrs.getColor(
                R.styleable.DropDownView_dropsyValueColor,
                Color.BLACK,
            )
        val dropsyElevation =
            dropsyAttrs.getDimension(R.styleable.DropDownView_dropsyElevation, 0.0f)
        val dropsySelector =
            dropsyAttrs.getColor(R.styleable.DropDownView_dropsySelector, Color.BLACK)
        val dropsyBorderSelector =
            dropsyAttrs.getColorStateList(R.styleable.DropDownView_dropsyBorderSelector)

        // Arrow styling
        imageArrow.imageTintList =
            dropsyAttrs.getColorStateList(R.styleable.DropDownView_dropsySelector)

        // Text styling
        label.setTextColor(dropsyLabelColor)
        value.setTextColor(dropsyValueColor)
        listViewAdapter.setTextColor(dropsyValueColor)
        listViewAdapter.setArrowColor(dropsySelector)

        // Card styling
        val padding = resources.getDimension(R.dimen.dropsy_dropdown_padding).toInt()
        setContentPadding(padding, padding, padding, padding)
        radius = resources.getDimension(R.dimen.dropsy_dropdown_corner_radius)
        strokeWidth = resources.getDimension(R.dimen.dropsy_dropdown_stroke_width).toInt()
        strokeColor = dropsySelector

        if (dropsyBorderSelector == null) {
            strokeColor = Color.WHITE
        } else {
            setStrokeColor(dropsyBorderSelector)
        }

        elevation = dropsyElevation
    }

    private fun initData(dropsyAttrs: TypedArray) {
        val dropsyLabel = dropsyAttrs.getString(R.styleable.DropDownView_dropsyLabel)
        items = dropsyAttrs.getTextArray(R.styleable.DropDownView_dropsyItems)?.map {
            DropDownItem(it.toString())
        } ?: listOf()

        label.text = dropsyLabel
        if (dropsyLabel.isNullOrBlank()) {
            label.visibility = View.GONE
        }
        if (items.isNotEmpty()) {
            value.setText(items[0].text, withAnimation = false, moveDown = false)
        }

        listViewAdapter = ListViewAdapter(context, items)
        listViewAdapter.setSelection(0, items[0])
        listView.adapter = listViewAdapter
        listView.setHeightBasedOnChildren() // Set height programmatically because it's not correctly measured in ExpandableLayout
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = items[position]
            value.setText(item.text)
            listViewAdapter.setSelection(position, item)
            listener?.onItemClick(position, item)
            hideListView()
        }
    }

    private fun animateExpand(withAnimation: Boolean) {
        val rotate =
            RotateAnimation(
                360f,
                180f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
            )
        rotate.duration = if (withAnimation) 300 else 0
        rotate.fillAfter = true
        imageArrow.startAnimation(rotate)
        expandableLayout.setExpanded(true, withAnimation)
    }

    private fun animateCollapse(withAnimation: Boolean) {
        val rotate =
            RotateAnimation(
                180f,
                360f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
            )
        rotate.duration = if (withAnimation) 300 else 0
        rotate.fillAfter = true
        imageArrow.startAnimation(rotate)
        expandableLayout.setExpanded(false, withAnimation)
    }
}