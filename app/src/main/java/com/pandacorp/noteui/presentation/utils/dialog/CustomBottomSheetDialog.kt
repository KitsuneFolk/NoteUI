package com.pandacorp.noteui.presentation.utils.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.pandacorp.noteui.app.databinding.DialogBotomsheetBinding
import com.pandacorp.noteui.domain.model.ColorItem

class CustomBottomSheetDialog(context: Context, private val dialogKey: Int) :
    BottomSheetDialog(context) {
    private var _binding: DialogBotomsheetBinding? = null
    private val binding get() = _binding!!

    private var onResetClickListener: View.OnClickListener? = null
    private var onRemoveAllClickListener: View.OnClickListener? = null
    private var onRemoveClickListener: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DialogBotomsheetBinding.inflate(layoutInflater)

        when (dialogKey) {
            ColorItem.ADD -> {
                binding.colorLayout.visibility = View.GONE
                binding.addButtonLayout.visibility = View.VISIBLE
                binding.reset.setOnClickListener {
                    onResetClickListener?.onClick(it)
                }
                binding.removeAll.setOnClickListener {
                    onRemoveAllClickListener?.onClick(it)
                }
            }

            ColorItem.COLOR -> {
                binding.colorLayout.visibility = View.VISIBLE
                binding.addButtonLayout.visibility = View.GONE
                binding.remove.setOnClickListener {
                    onRemoveClickListener?.onClick(it)
                }
            }

            else -> throw IllegalArgumentException("dialogKey = $dialogKey")
        }
        setContentView(binding.root)
    }

    override fun show() {
        super.show()
        // Expand the dialog in the landscape mode
        val view = findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        view!!.post {
            val behavior = BottomSheetBehavior.from(view)
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED)
        }
    }

    fun setOnResetClickListener(onClickListener: View.OnClickListener) {
        onResetClickListener = onClickListener
    }

    fun setOnRemoveClickListener(onClickListener: View.OnClickListener) {
        onRemoveClickListener = onClickListener
    }

    fun setOnRemoveAllClickListener(onClickListener: View.OnClickListener) {
        onRemoveAllClickListener = onClickListener
    }
}