package com.pandacorp.notesui.utils.dialog

import android.animation.AnimatorInflater
import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.pandacorp.domain.models.ColorItem
import com.pandacorp.notesui.R
import com.pandacorp.notesui.utils.Constans
import com.pandacorp.notesui.utils.Utils


class CustomBottomSheetDialog : BottomSheetDialogFragment() {
    private val TAG = Utils.TAG
    
    private val LAYOUT_HEIGHT = 200
    
    private var onResetButtonClickListener: View.OnClickListener? = null
    private var onRemoveButtonClickListener: View.OnClickListener? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // This needed to not recreate fragment after screen rotation, so listeners won't be null
        retainInstance = true
        val root = LinearLayout(requireContext())
        val rootLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LAYOUT_HEIGHT, 1f)
        root.layoutParams = rootLp
        root.orientation = LinearLayout.VERTICAL
        
        when (val arg = requireArguments().getInt(Constans.valueKey)) {
            ColorItem.ADD -> callAddDialog(root) // Add color button bottom sheet dialog
            ColorItem.COLOR -> callColorDialog(root) // Color button bottom sheet dialog
            else -> throw IllegalArgumentException("argument = $arg")
        }
        
        return root
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // here we remove callback that removes rounded corners on slide
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        (dialog as BottomSheetDialog).behavior.addBottomSheetCallback(object :
            BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    val newMaterialShapeDrawable = createMaterialShapeDrawable(bottomSheet)
                    
                    bottomSheet.background = newMaterialShapeDrawable
                }
            }
            
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })
        return dialog
    }
    
    private fun callAddDialog(root: LinearLayout) {
        val resetLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LAYOUT_HEIGHT, 1f)
        resetLp.setMargins(5, 5, 5, 0)
         val removeLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LAYOUT_HEIGHT, 1f)
        removeLp.setMargins(5, 5, 5, 10)
    
        // Can't use 1 anim for 2 views
        val anim1 = AnimatorInflater.loadStateListAnimator(
                requireContext(),
                R.animator.increase_size_large_animator)
        val anim2 = AnimatorInflater.loadStateListAnimator(
                requireContext(),
                R.animator.increase_size_large_animator)
        
        // Reset
        val resetTv = TextView(requireContext())
        resetTv.setText(R.string.reset)
        resetTv.gravity = Gravity.CENTER
        resetTv.textSize = 14f
        
        val resetLayout = FrameLayout(requireContext())
        resetLayout.layoutParams = resetLp
        resetLayout.stateListAnimator = anim1
        resetLayout.setOnClickListener {
            onResetButtonClickListener?.onClick(view)
        }
        
        // Remove all
        val removeTv = TextView(requireContext())
        removeTv.setText(R.string.removeAll)
        removeTv.gravity = Gravity.CENTER
        removeTv.textSize = 14f
        
        val removeLayout = FrameLayout(requireContext())
        removeLayout.layoutParams = removeLp
        removeLayout.stateListAnimator = anim2
        removeLayout.setOnClickListener {
            onRemoveButtonClickListener?.onClick(it)
        }
        
        resetLayout.addView(resetTv)
        removeLayout.addView(removeTv)
        
        root.addView(resetLayout, resetLp)
        root.addView(removeLayout, removeLp)
        
    }
    
    private fun callColorDialog(root: LinearLayout) {
        val anim = AnimatorInflater.loadStateListAnimator(
                requireContext(),
                R.animator.increase_size_large_animator)
        val removeLp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LAYOUT_HEIGHT-40, 1f)
        removeLp.setMargins(5, 5, 5, 10)
    
        // Remove color
        val removeTv = TextView(requireContext())
        removeTv.setText(R.string.remove)
        removeTv.gravity = Gravity.CENTER
        removeTv.textSize = 14f
    
        val removeLayout = FrameLayout(requireContext())
        removeLayout.layoutParams = removeLp
        removeLayout.stateListAnimator = anim
        removeLayout.setOnClickListener {
            onRemoveButtonClickListener?.onClick(it)
        }
    
        removeLayout.addView(removeTv)
    
        root.addView(removeLayout, removeLp)
    
    }
    
    private fun createMaterialShapeDrawable(bottomSheet: View): MaterialShapeDrawable {
        //Create a ShapeAppearanceModel with the same shapeAppearanceOverlay used in the style
        val shapeAppearanceModel =
            ShapeAppearanceModel.builder(context, 0, R.style.CustomShapeAppearanceBottomSheetDialog)
                .build()
        
        //Create a new MaterialShapeDrawable (you can't use the original MaterialShapeDrawable in the BottomSheet)
        val currentMaterialShapeDrawable = bottomSheet.background as MaterialShapeDrawable
        val newMaterialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        //Copy the attributes in the new MaterialShapeDrawable
        newMaterialShapeDrawable.initializeElevationOverlay(context)
        newMaterialShapeDrawable.fillColor = currentMaterialShapeDrawable.fillColor
        newMaterialShapeDrawable.tintList = currentMaterialShapeDrawable.tintList
        newMaterialShapeDrawable.elevation = currentMaterialShapeDrawable.elevation
        newMaterialShapeDrawable.strokeWidth = currentMaterialShapeDrawable.strokeWidth
        newMaterialShapeDrawable.strokeColor = currentMaterialShapeDrawable.strokeColor
        return newMaterialShapeDrawable
    }
    
    fun setOnResetButtonClickListener(onClickListener: View.OnClickListener) {
        onResetButtonClickListener = onClickListener
    }
    
    fun setOnRemoveButtonClickListener(onClickListener: View.OnClickListener) {
        onRemoveButtonClickListener = onClickListener
    }
    
    companion object {
        fun newInstance(dialogKey: Int): CustomBottomSheetDialog {
            val args = Bundle()
            args.putInt(Constans.valueKey, dialogKey)
            val dialog = CustomBottomSheetDialog()
            dialog.arguments = args
            return dialog
        }
    }
}