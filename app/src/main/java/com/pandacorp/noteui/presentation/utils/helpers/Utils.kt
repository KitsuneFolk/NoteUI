package com.pandacorp.noteui.presentation.utils.helpers

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.target.Target
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.domain.model.ColorItem

class Utils {
    companion object {
        /**
         * Get a list of images that user can choose to set a note's background
         */
        val backgroundDrawablesList =
            listOf(
                R.drawable.image_night_city,
                R.drawable.image_city,
                R.drawable.image_moon,
                R.drawable.image_night_sky,
                R.drawable.image_nature,
                R.drawable.image_mountain,
                R.drawable.image_colors,
                R.drawable.image_speed,
            )

        /**
         * Get a list of default colors that will be shown on the database creation
         */
        fun getDefaultColorsList(context: Context): List<ColorItem> =
            mutableListOf(
                ColorItem(id = 1, color = ContextCompat.getColor(context, R.color.light_yellow)),
                ColorItem(id = 2, color = ContextCompat.getColor(context, R.color.light_green)),
                ColorItem(id = 3, color = ContextCompat.getColor(context, R.color.light_lime)),
                ColorItem(id = 4, color = ContextCompat.getColor(context, R.color.light_blue)),
                ColorItem(id = 5, color = ContextCompat.getColor(context, R.color.light_pink)),
                ColorItem(id = 6, color = ContextCompat.getColor(context, R.color.light_purple)),
                ColorItem(id = 7, color = ContextCompat.getColor(context, R.color.light_red)),
            )

        /**
         * Changes the note background in adapter and NoteScreen
         * @param background encoded string background, stored in db
         * @param imageView image view, where need to change the background
         * @param isAdapter if true then set colorPrimary as background if it is a color, else colorBackground for NoteScreen
         * @param isUseGlide if false don't use glide to change background instantly for NoteScreen
         */
        fun changeNoteBackground(
            background: String,
            imageView: ImageView,
            isAdapter: Boolean = false,
            isUseGlide: Boolean = true,
        ) {
            try {
                // note.background is an image drawable from Utils
                val drawable =
                    ContextCompat.getDrawable(imageView.context, backgroundDrawablesList[background.toInt()])

                if (!isUseGlide) {
                    imageView.setImageDrawable(drawable)
                    return
                }

                Glide.with(imageView.context)
                    .load(drawable)
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) // Needed for correct work in adapters
                    .into(imageView)
            } catch (e: ArrayIndexOutOfBoundsException) {
                // note.background is a color.
                val typedValue = TypedValue()
                if (isAdapter) {
                    imageView.context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
                } else {
                    imageView.context.theme.resolveAttribute(android.R.attr.colorBackground, typedValue, true)
                }
                val color = typedValue.data

                if (!isUseGlide) {
                    imageView.setImageDrawable(ColorDrawable(color))
                    return
                }

                Glide.with(imageView.context)
                    .load(ColorDrawable(color))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) // Needed for correct work in adapters
                    .into(imageView)
            } catch (e: NumberFormatException) {
                // note.background is an image from storage (uri)
                if (!isUseGlide) {
                    imageView.setImageURI(Uri.parse(background))
                    return
                }

                Glide.with(imageView.context)
                    .load(Uri.parse(background))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL) // Needed for correct work in adapters
                    .into(imageView)
            }
        }

        /**
         * Starts a sliding animation for 2 views
         */
        fun animateViewSliding(
            showingView: View,
            hidingView: View,
        ) {
            val parent = showingView.parent as ViewGroup
            val showingAnimation =
                Slide(Gravity.BOTTOM).apply {
                    duration = Constants.HIDE_DURATION
                    addTarget(showingView)
                }
            val hidingAnimation =
                Slide(Gravity.BOTTOM).apply {
                    duration = Constants.SHOW_DURATION
                    addTarget(hidingView)
                }.addListener(
                    object : Transition.TransitionListener {
                        override fun onTransitionEnd(transition: Transition) {
                            TransitionManager.beginDelayedTransition(parent, showingAnimation)
                            showingView.visibility = View.VISIBLE
                        }

                        override fun onTransitionCancel(transition: Transition) {}

                        override fun onTransitionStart(transition: Transition) {}

                        override fun onTransitionPause(transition: Transition) {}

                        override fun onTransitionResume(transition: Transition) {}
                    },
                )
            TransitionManager.beginDelayedTransition(parent, hidingAnimation)

            hidingView.visibility = View.GONE
        }

        /**
         * Sets the system window insets behavior for the fragment's window.
         *
         * @param root The Root view of the layout
         * @param fitsSystemWindows True to fit system windows, false otherwise.
         */
        fun Fragment.setDecorFitsSystemWindows(
            root: View,
            fitsSystemWindows: Boolean
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requireActivity().window.setDecorFitsSystemWindows(fitsSystemWindows)

                val lp = root.layoutParams as MarginLayoutParams
                if (fitsSystemWindows) {
                    lp.topMargin = 0
                    lp.bottomMargin = 0
                } else {
                    // Set margins manually
                    lp.topMargin =
                        requireActivity().window.decorView.rootWindowInsets
                            .getInsets(WindowInsetsCompat.Type.statusBars()).top
                    lp.bottomMargin =
                        requireActivity().window.decorView.rootWindowInsets
                            .getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                }
                root.layoutParams = lp
            }
        }

        /**
         * Crops the provided Drawable to show only its bottom part, excluding the top part if the
         * orientation is horizontal (landscape mode).
         *
         * @param drawable The input Drawable to be cropped. It should not be null, and it should not be
         *                 an instance of ColorDrawable.
         * @param resources The Resources object used to create a new BitmapDrawable from the cropped image.
         * @return A new Drawable that represents the cropped image. If the orientation is horizontal, the
         *         returned Drawable will only contain the bottom part of the input Drawable. If the
         *         orientation is portrait, the input Drawable is returned unmodified.
         */
        fun cropImage(drawable: Drawable?, resources: Resources): Drawable? {
            if (drawable == null || drawable is ColorDrawable) return drawable
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                return drawable
            }
            val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            val width = bitmap.width
            val height = bitmap.height

            val squareSize = width.coerceAtMost(height)
            val x = (width - squareSize)
            val y = (height - squareSize)

            val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, squareSize, squareSize)
            return BitmapDrawable(resources, croppedBitmap)
        }
    }
}