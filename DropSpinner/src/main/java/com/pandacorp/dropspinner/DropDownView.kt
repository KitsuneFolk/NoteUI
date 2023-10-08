package com.pandacorp.dropspinner

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.card.MaterialCardView
import com.skydoves.powermenu.CustomPowerMenu
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnDismissedListener
import com.skydoves.powermenu.OnMenuItemClickListener

class DropDownView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : MaterialCardView(context, attrs, defStyleAttr),
    OnMenuItemClickListener<DropDownItem>, OnDismissedListener, View.OnClickListener {

    fun interface ItemClickListener {
        fun onItemClick(position: Int, item: DropDownItem)
    }

    private var listener: ItemClickListener? = null
    private lateinit var dropDownItems: List<DropDownItem>
    private val dropDownAdapter = DropDownAdapter()
    private val dropDownPopup: CustomPowerMenu<DropDownItem?, DropDownAdapter?> by lazy {
        val popup: CustomPowerMenu<DropDownItem?, DropDownAdapter?> =
            CustomPowerMenu.Builder(context, dropDownAdapter)
                .setWidth(width)
                .addItemList(dropDownItems)
                .setMenuRadius(radius)
                .setPadding(resources.getDimension(R.dimen.dropsy_white_space_margin).toInt())
                .setShowBackground(false)
                .setOnDismissListener(this)
                .setDismissIfShowAgain(true)
                .setFocusable(true)
                .setOnMenuItemClickListener(this)
                .setAnimation(MenuAnimation.DROP_DOWN)
                .setLifecycleOwner(context as LifecycleOwner)
                .build()
        if (dropDownItems.isNotEmpty())
            dropDownAdapter.setSelection(0, dropDownItems[0])
        popup
    }
    private val imageArrow: AppCompatImageView
    private val label: CustomTextView
    private val value: CustomTextView

    init {
        // Add body layout
        val dropDownBody = LayoutInflater.from(context).inflate(
            R.layout.dropsy_layout_drop_down,
            this,
            false
        ) as LinearLayout
        addView(dropDownBody)
        imageArrow = findViewById(R.id.img_arrow)
        label = findViewById(R.id.txt_drop_drown_label)
        value = findViewById(R.id.txt_drop_drown_value)

        // get attrs
        val dropsyAttrs = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DropDownView,
            0, 0
        )
        setStyles(dropsyAttrs)
        initData(dropsyAttrs)
        dropsyAttrs.recycle()

        setOnClickListener(this)
    }

    fun setItemClickListener(listener: ItemClickListener) {
        this.listener = listener
    }

    override fun onClick(v: View?) {
        if (isSelected)
            hideDropdown()
        else
            showDropdown()
    }

    override fun onItemClick(position: Int, item: DropDownItem) {
        value.text = item.text
        dropDownAdapter.setSelection(position, item)
        listener?.onItemClick(position, item)
        hideDropdown()
    }

    override fun onDismissed() {
        isSelected = false
        animateCollapse()
    }

    private fun showDropdown() {
        post {
            isSelected = true
            dropDownPopup.showAsDropDown(this)
            animateExpand()
        }
    }

    private fun hideDropdown() {
        postDelayed({
            dropDownPopup.dismiss()
        }, 150)
    }

    private fun setStyles(dropsyAttrs: TypedArray) {
        val resources = context.resources
        val dropsyLabelColor =
            dropsyAttrs.getColor(
                R.styleable.DropDownView_dropsyLabelColor,
                ContextCompat.getColor(context, R.color.dropsy_text_color_secondary)
            )
        val dropsyValueColor =
            dropsyAttrs.getColor(
                R.styleable.DropDownView_dropsyValueColor,
                ContextCompat.getColor(context, R.color.dropsy_text_color)
            )
        val dropsyElevation =
            dropsyAttrs.getDimension(R.styleable.DropDownView_dropsyElevation, 0.0f)
        val dropsySelector =
            dropsyAttrs.getColor(R.styleable.DropDownView_dropsySelector, Color.BLACK)
        val dropsyBorderSelector =
            dropsyAttrs.getColorStateList(R.styleable.DropDownView_dropsyBorderSelector)

        // arrow styling
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            imageArrow.imageTintList =
                dropsyAttrs.getColorStateList(R.styleable.DropDownView_dropsySelector)
        } else {
            imageArrow.setColorFilter(dropsySelector, android.graphics.PorterDuff.Mode.SRC_IN)
        }

        // text styling
        label.setTextColor(dropsyLabelColor)
        value.setTextColor(dropsyValueColor)
        dropDownAdapter.setTextColor(dropsyValueColor)

        // card styling
        val padding = resources.getDimension(R.dimen.dropsy_dropdown_padding).toInt()
        setContentPadding(padding, padding, padding, padding)
        radius = resources.getDimension(R.dimen.dropsy_dropdown_corner_radius)
        strokeWidth = resources.getDimension(R.dimen.dropsy_dropdown_stroke_width).toInt()
        strokeColor = dropsySelector

        if (dropsyBorderSelector == null)
            setStrokeColor(
                ContextCompat.getColorStateList(context, R.color.dropsy_selector)
            )
        else
            setStrokeColor(dropsyBorderSelector)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
            elevation = dropsyElevation
    }

    private fun initData(dropsyAttrs: TypedArray) {
        val dropsyLabel = dropsyAttrs.getString(R.styleable.DropDownView_dropsyLabel)
        dropDownItems = dropsyAttrs.getTextArray(R.styleable.DropDownView_dropsyItems)?.map {
            DropDownItem(it.toString())
        } ?: listOf()

        label.text = dropsyLabel
        if (dropsyLabel.isNullOrBlank())
            label.visibility = View.GONE
        if (dropDownItems.isNotEmpty())
            value.text = dropDownItems[0].text
    }

    private fun animateExpand() {
        val rotate = RotateAnimation(
            360f,
            180f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 300
        rotate.fillAfter = true
        imageArrow.startAnimation(rotate)
    }

    private fun animateCollapse() {
        val rotate = RotateAnimation(
            180f,
            360f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        rotate.duration = 300
        rotate.fillAfter = true
        imageArrow.startAnimation(rotate)
    }
}