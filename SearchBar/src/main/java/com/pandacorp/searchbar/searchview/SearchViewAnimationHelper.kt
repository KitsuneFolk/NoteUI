package com.pandacorp.searchbar.searchview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.core.view.ViewCompat
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.internal.ClippableRoundedCornerLayout
import com.google.android.material.internal.FadeThroughDrawable
import com.google.android.material.internal.MultiViewUpdateListener
import com.google.android.material.internal.RectEvaluator
import com.google.android.material.internal.ReversableAnimatedValueInterpolator
import com.google.android.material.internal.ToolbarUtils
import com.google.android.material.internal.TouchObserverFrameLayout
import com.google.android.material.internal.ViewUtils
import com.pandacorp.searchbar.SearchBar

/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Helper class for [SearchView] animations.
 */
@SuppressLint("RestrictedApi")
internal class SearchViewAnimationHelper(private val searchView: SearchView) {
    private var showDurationMs: Long = 300
    private var showContentScaleDurationMs = showDurationMs
    private var hideDurationMs: Long = 250
    private var hideContentScaleDurationMs = hideDurationMs
    private val scrim: View = searchView.scrim
    private val rootView: ClippableRoundedCornerLayout = searchView.rootView
    private val headerContainer = searchView.headerContainer
    private val toolbarContainer = searchView.toolbarContainer
    private val toolbar: Toolbar
    private val searchPrefix: TextView
    private val editText: EditText
    private val clearButton: ImageButton
    private val divider: View
    private val contentContainer: TouchObserverFrameLayout
    private var searchBar: SearchBar? = null

    init {
        toolbar = searchView.toolbar
        searchPrefix = searchView.searchPrefix
        editText = searchView.editText
        clearButton = searchView.clearButton
        divider = searchView.divider
        contentContainer = searchView.contentContainer
    }

    fun setAnimationDuration(showDurationMs: Long, hideDurationMs: Long) {
        this.showDurationMs = showDurationMs
        showContentScaleDurationMs = showDurationMs
        this.hideDurationMs = hideDurationMs
        hideContentScaleDurationMs = hideDurationMs
    }

    fun setSearchBar(searchBar: SearchBar?) {
        this.searchBar = searchBar
    }

    fun show() {
        if (searchBar != null) {
            startShowAnimationExpand()
        } else {
            startShowAnimationTranslate()
        }
    }

    fun hide() {
        if (searchBar != null) {
            startHideAnimationCollapse()
        } else {
            startHideAnimationTranslate()
        }
    }

    private fun startShowAnimationExpand() {
        if (searchView.isAdjustNothingSoftInputMode) {
            searchView.requestFocusAndShowKeyboardIfNeeded()
        }
        searchView.setTransitionState(SearchView.TransitionState.SHOWING)
        editText.setText(searchBar!!.text)
        editText.setSelection(editText.text.length)
        rootView.visibility = View.INVISIBLE
        rootView.post {
            val animatorSet = getExpandCollapseAnimatorSet(true)
            animatorSet.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        rootView.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        if (!searchView.isAdjustNothingSoftInputMode) {
                            searchView.requestFocusAndShowKeyboardIfNeeded()
                        }
                        searchView.setTransitionState(SearchView.TransitionState.SHOWN)
                    }
                }
            )
            animatorSet.start()
        }
    }

    private fun startHideAnimationCollapse() {
        if (searchView.isAdjustNothingSoftInputMode) {
            searchView.clearFocusAndHideKeyboard()
        }
        val animatorSet = getExpandCollapseAnimatorSet(false)
        animatorSet.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    searchView.setTransitionState(SearchView.TransitionState.HIDING)
                }

                override fun onAnimationEnd(animation: Animator) {
                    rootView.visibility = View.GONE
                    if (!searchView.isAdjustNothingSoftInputMode) {
                        searchView.clearFocusAndHideKeyboard()
                    }
                    searchView.setTransitionState(SearchView.TransitionState.HIDDEN)
                }
            }
        )
        animatorSet.start()
    }

    private fun startShowAnimationTranslate() {
        if (searchView.isAdjustNothingSoftInputMode) {
            searchView.postDelayed(
                { searchView.requestFocusAndShowKeyboardIfNeeded() },
                SHOW_TRANSLATE_KEYBOARD_START_DELAY_MS
            )
        }
        rootView.visibility = View.INVISIBLE
        rootView.post {
            rootView.translationY = rootView.height.toFloat()
            val animatorSet = getTranslateAnimatorSet(true)
            animatorSet.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        rootView.visibility = View.VISIBLE
                        searchView.setTransitionState(SearchView.TransitionState.SHOWING)
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        if (!searchView.isAdjustNothingSoftInputMode) {
                            searchView.requestFocusAndShowKeyboardIfNeeded()
                        }
                        searchView.setTransitionState(SearchView.TransitionState.SHOWN)
                    }
                }
            )
            animatorSet.start()
        }
    }

    private fun startHideAnimationTranslate() {
        if (searchView.isAdjustNothingSoftInputMode) {
            searchView.clearFocusAndHideKeyboard()
        }
        val animatorSet = getTranslateAnimatorSet(false)
        animatorSet.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    searchView.setTransitionState(SearchView.TransitionState.HIDING)
                }

                override fun onAnimationEnd(animation: Animator) {
                    rootView.visibility = View.GONE
                    if (!searchView.isAdjustNothingSoftInputMode) {
                        searchView.clearFocusAndHideKeyboard()
                    }
                    searchView.setTransitionState(SearchView.TransitionState.HIDDEN)
                }
            }
        )
        animatorSet.start()
    }

    private fun getTranslateAnimatorSet(show: Boolean): AnimatorSet {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translationYAnimator)
        addBackButtonProgressAnimatorIfNeeded(animatorSet)
        animatorSet.interpolator =
            ReversableAnimatedValueInterpolator.of(
                show,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            )
        animatorSet.duration =
            if (show) SHOW_TRANSLATE_DURATION_MS else HIDE_TRANSLATE_DURATION_MS
        return animatorSet
    }

    private val translationYAnimator: Animator
        get() {
            val animator = ValueAnimator.ofFloat(rootView.height.toFloat(), 0f)
            animator.addUpdateListener(MultiViewUpdateListener.translationYListener(rootView))
            return animator
        }

    private fun getExpandCollapseAnimatorSet(show: Boolean): AnimatorSet {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            getScrimAlphaAnimator(show),
            getRootViewAnimator(show),
            getClearButtonAnimator(show),
            getContentAnimator(show),
            getButtonsAnimator(show),
            getHeaderContainerAnimator(show),
            getActionMenuViewsAlphaAnimator(show),
            getEditTextAnimator(show),
            getSearchPrefixAnimator(show)
        )
        animatorSet.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator) {
                    setActionMenuViewAlphaIfNeeded((if (show) 0 else 1).toFloat())
                }

                override fun onAnimationEnd(animation: Animator) {
                    setActionMenuViewAlphaIfNeeded((if (show) 1 else 0).toFloat())
                    if (show) {
                        // After expanding, we should reset the clip bounds so it can react to screen or
                        // layout changes. Otherwise it will result in wrong clipping on the layout.
                        rootView.resetClipBoundsAndCornerRadius()
                    }
                }
            }
        )
        return animatorSet
    }

    private fun setActionMenuViewAlphaIfNeeded(alpha: Float) {
        if (searchView.isMenuItemsAnimated) {
            val actionMenuView = ToolbarUtils.getActionMenuView(toolbar)
            if (actionMenuView != null) {
                actionMenuView.alpha = alpha
            }
        }
    }

    private fun getScrimAlphaAnimator(show: Boolean): Animator {
        val interpolator =
            if (show) AnimationUtils.LINEAR_INTERPOLATOR else AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = if (show) showDurationMs else hideDurationMs
        animator.interpolator = ReversableAnimatedValueInterpolator.of(show, interpolator)
        animator.addUpdateListener(MultiViewUpdateListener.alphaListener(scrim))
        return animator
    }

    private fun getRootViewAnimator(show: Boolean): Animator {
        val toClipBounds =
            ViewUtils.calculateRectFromBounds(
                searchView
            )
        val fromClipBounds = calculateFromClipBounds()
        val clipBounds = Rect(fromClipBounds)
        val initialCornerRadius = searchBar!!.cornerSize
        val animator = ValueAnimator.ofObject(RectEvaluator(clipBounds), fromClipBounds, toClipBounds)
        animator.addUpdateListener { valueAnimator: ValueAnimator ->
            val cornerRadius = initialCornerRadius * (1 - valueAnimator.animatedFraction)
            rootView.updateClipBoundsAndCornerRadius(clipBounds, cornerRadius)
        }
        animator.duration = if (show) showDurationMs else hideDurationMs
        animator.interpolator =
            ReversableAnimatedValueInterpolator.of(show, AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
        return animator
    }

    private fun calculateFromClipBounds(): Rect {
        val searchBarAbsolutePosition = IntArray(2)
        searchBar!!.getLocationOnScreen(searchBarAbsolutePosition)
        val searchBarAbsoluteLeft = searchBarAbsolutePosition[0]
        val searchBarAbsoluteTop = searchBarAbsolutePosition[1]

        // Use rootView to handle potential fitsSystemWindows padding applied to parent searchView.
        val searchViewAbsolutePosition = IntArray(2)
        rootView.getLocationOnScreen(searchViewAbsolutePosition)
        val searchViewAbsoluteLeft = searchViewAbsolutePosition[0]
        val searchViewAbsoluteTop = searchViewAbsolutePosition[1]
        val fromLeft = searchBarAbsoluteLeft - searchViewAbsoluteLeft
        val fromTop = searchBarAbsoluteTop - searchViewAbsoluteTop
        val fromRight = fromLeft + searchBar!!.width
        val fromBottom = fromTop + searchBar!!.height
        return Rect(fromLeft, fromTop, fromRight, fromBottom)
    }

    private fun getClearButtonAnimator(show: Boolean): AnimatorSet {
        val animatorSet = AnimatorSet()
        animatorSet.duration =
            if (show) showDurationMs else hideDurationMs
        animatorSet.interpolator =
            ReversableAnimatedValueInterpolator.of(
                show,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            )
        if (searchView.isAnimatedNavigationIcon) {
            val drawable = clearButton.drawable
            addFadeThroughDrawableAnimatorIfNeeded(animatorSet, drawable)
            animatorSet.playTogether(getTranslationAnimator(show, false, clearButton))
        }
        return animatorSet
    }

    private fun getButtonsAnimator(show: Boolean): Animator {
        val animatorSet = AnimatorSet()
        addBackButtonTranslationAnimatorIfNeeded(animatorSet)
        addBackButtonProgressAnimatorIfNeeded(animatorSet)
        addActionMenuViewAnimatorIfNeeded(animatorSet)
        animatorSet.duration = if (show) showDurationMs else hideDurationMs
        animatorSet.interpolator =
            ReversableAnimatedValueInterpolator.of(
                show,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            )
        return animatorSet
    }

    private fun addBackButtonTranslationAnimatorIfNeeded(animatorSet: AnimatorSet) {
        val backButton = ToolbarUtils.getNavigationIconButton(toolbar) ?: return
        val backButtonAnimatorX = ValueAnimator.ofFloat(getFromTranslationXStart(backButton).toFloat(), 0f)
        backButtonAnimatorX.addUpdateListener(MultiViewUpdateListener.translationXListener(backButton))
        val backButtonAnimatorY = ValueAnimator.ofFloat(fromTranslationY.toFloat(), 0f)
        backButtonAnimatorY.addUpdateListener(MultiViewUpdateListener.translationYListener(backButton))
        animatorSet.playTogether(backButtonAnimatorX, backButtonAnimatorY)
    }

    private fun addBackButtonProgressAnimatorIfNeeded(animatorSet: AnimatorSet) {
        val backButton = ToolbarUtils.getNavigationIconButton(toolbar) ?: return
        val drawable = DrawableCompat.unwrap<Drawable>(backButton.drawable)
        if (searchView.isAnimatedNavigationIcon) {
            addDrawerArrowDrawableAnimatorIfNeeded(animatorSet, drawable)
            addFadeThroughDrawableAnimatorIfNeeded(animatorSet, drawable)
        } else {
            setFullDrawableProgressIfNeeded(drawable)
        }
    }

    private fun addDrawerArrowDrawableAnimatorIfNeeded(animatorSet: AnimatorSet, drawable: Drawable) {
        if (drawable is DrawerArrowDrawable) {
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.addUpdateListener { animation: ValueAnimator -> drawable.progress = animation.animatedFraction }
            animatorSet.playTogether(animator)
        }
    }

    private fun addFadeThroughDrawableAnimatorIfNeeded(animatorSet: AnimatorSet, drawable: Drawable) {
        if (drawable is FadeThroughDrawable) {
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animator.addUpdateListener { animation: ValueAnimator ->
                drawable.setProgress(animation.animatedFraction)
            }
            animatorSet.playTogether(animator)
        }
    }

    private fun setFullDrawableProgressIfNeeded(drawable: Drawable) {
        if (drawable is DrawerArrowDrawable) {
            drawable.progress = 1f
        }
        if (drawable is FadeThroughDrawable) {
            drawable.setProgress(1f)
        }
    }

    private fun addActionMenuViewAnimatorIfNeeded(animatorSet: AnimatorSet) {
        val actionMenuView = ToolbarUtils.getActionMenuView(toolbar) ?: return
        val actionMenuViewAnimatorX = ValueAnimator.ofFloat(getFromTranslationXEnd(actionMenuView).toFloat(), 0f)
        actionMenuViewAnimatorX.addUpdateListener(
            MultiViewUpdateListener.translationXListener(actionMenuView)
        )
        val actionMenuViewAnimatorY = ValueAnimator.ofFloat(fromTranslationY.toFloat(), 0f)
        actionMenuViewAnimatorY.addUpdateListener(
            MultiViewUpdateListener.translationYListener(actionMenuView)
        )
        animatorSet.playTogether(actionMenuViewAnimatorX, actionMenuViewAnimatorY)
    }

    private fun getHeaderContainerAnimator(show: Boolean): Animator {
        return getTranslationAnimator(show, false, headerContainer)
    }

    private fun getActionMenuViewsAlphaAnimator(show: Boolean): Animator {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = if (show) showDurationMs else hideDurationMs
        animator.interpolator =
            ReversableAnimatedValueInterpolator.of(
                show,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            )
        return animator
    }

    private fun getSearchPrefixAnimator(show: Boolean): Animator {
        return getTranslationAnimator(show, true, searchPrefix)
    }

    private fun getEditTextAnimator(show: Boolean): Animator {
        return getTranslationAnimator(show, true, editText)
    }

    private fun getContentAnimator(show: Boolean): Animator {
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(getContentScaleAnimator(show))
        return animatorSet
    }

    private fun getContentScaleAnimator(show: Boolean): Animator {
        val animatorScale = ValueAnimator.ofFloat(CONTENT_FROM_SCALE, 1f)
        animatorScale.duration = if (show) showContentScaleDurationMs else hideContentScaleDurationMs
        animatorScale.interpolator =
            ReversableAnimatedValueInterpolator.of(
                show,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            )
        animatorScale.addUpdateListener(MultiViewUpdateListener.scaleListener(contentContainer))
        return animatorScale
    }

    private fun getTranslationAnimator(show: Boolean, anchoredToStart: Boolean, view: View): Animator {
        val startX = if (anchoredToStart) getFromTranslationXStart(view) else getFromTranslationXEnd(view)
        val animatorX = ValueAnimator.ofFloat(startX.toFloat(), 0f)
        animatorX.addUpdateListener(MultiViewUpdateListener.translationXListener(view))
        val animatorY = ValueAnimator.ofFloat(fromTranslationY.toFloat(), 0f)
        animatorY.addUpdateListener(MultiViewUpdateListener.translationYListener(view))
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(animatorX, animatorY)
        animatorSet.duration = if (show) showDurationMs else hideDurationMs
        animatorSet.interpolator =
            ReversableAnimatedValueInterpolator.of(
                show,
                AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR
            )
        return animatorSet
    }

    private fun getFromTranslationXStart(view: View): Int {
        val marginStart = MarginLayoutParamsCompat.getMarginStart((view.layoutParams as ViewGroup.MarginLayoutParams))
        val paddingStart = ViewCompat.getPaddingStart(searchBar!!)
        return if (ViewUtils.isLayoutRtl(
                searchBar
            )
        ) {
            searchBar!!.width - searchBar!!.right + marginStart - paddingStart
        } else {
            searchBar!!.left - marginStart + paddingStart
        }
    }

    private fun getFromTranslationXEnd(view: View): Int {
        val marginEnd = MarginLayoutParamsCompat.getMarginEnd((view.layoutParams as ViewGroup.MarginLayoutParams))
        return if (ViewUtils.isLayoutRtl(
                searchBar
            )
        ) {
            searchBar!!.left - marginEnd
        } else {
            searchBar!!.right - searchView.width + marginEnd
        }
    }

    private val fromTranslationY: Int
        get() {
            val toolbarMiddleY = (toolbarContainer.top + toolbarContainer.bottom) / 2
            val searchBarMiddleY = (searchBar!!.top + searchBar!!.bottom) / 2
            return searchBarMiddleY - toolbarMiddleY
        }

    companion object {
        // Constants for hide collapse animation.
        private const val CONTENT_FROM_SCALE = 0.95f

        // Constants for show translate animation.
        private const val SHOW_TRANSLATE_DURATION_MS: Long = 350
        private const val SHOW_TRANSLATE_KEYBOARD_START_DELAY_MS: Long = 150

        // Constants for hide translate animation.
        private const val HIDE_TRANSLATE_DURATION_MS: Long = 300
    }
}