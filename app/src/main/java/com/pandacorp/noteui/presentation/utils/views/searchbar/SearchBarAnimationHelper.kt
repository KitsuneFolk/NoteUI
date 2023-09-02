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
package com.pandacorp.noteui.presentation.utils.views.searchbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.ActionMenuView
import androidx.core.view.ViewCompat
import com.google.android.material.animation.AnimatableView
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.internal.ExpandCollapseAnimationHelper
import com.google.android.material.internal.MultiViewUpdateListener
import com.google.android.material.internal.ToolbarUtils
import com.google.android.material.internal.ViewUtils
import com.google.android.material.shape.MaterialShapeDrawable

/** Helper class for [SearchBar] animations.  */
@SuppressLint("RestrictedApi")
internal class SearchBarAnimationHelper {
    private val onLoadAnimationCallbacks: MutableSet<SearchBar.OnLoadAnimationCallback> = LinkedHashSet()
    private val expandAnimationListeners: MutableSet<AnimatorListenerAdapter> = LinkedHashSet()
    private val collapseAnimationListeners: MutableSet<AnimatorListenerAdapter> = LinkedHashSet()
    private var secondaryViewAnimator: Animator? = null
    private var defaultCenterViewAnimator: Animator? = null
    var isExpanding = false
        private set
    var isCollapsing = false
        private set
    var isOnLoadAnimationFadeInEnabled = true
    private var runningExpandOrCollapseAnimator: Animator? = null

    fun startOnLoadAnimation(searchBar: SearchBar) {
        dispatchOnLoadAnimation(SearchBar.OnLoadAnimationCallback::onAnimationStart)
        val textView = searchBar.textView
        val centerView = searchBar.getCenterView()
        val secondaryActionMenuItemView: View? = ToolbarUtils.getSecondaryActionMenuItemView(searchBar)
        val secondaryViewAnimator = getSecondaryViewAnimator(textView, secondaryActionMenuItemView)

        secondaryViewAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                dispatchOnLoadAnimation(SearchBar.OnLoadAnimationCallback::onAnimationEnd)
            }
        })

        this.secondaryViewAnimator = secondaryViewAnimator

        textView.alpha = 0f
        if (secondaryActionMenuItemView != null) {
            secondaryActionMenuItemView.alpha = 0f
        }
        if (centerView is AnimatableView) {
            (centerView as AnimatableView).startAnimation { secondaryViewAnimator.start() }
        } else if (centerView != null) {
            centerView.alpha = 0f
            centerView.visibility = View.VISIBLE
            val defaultCenterViewAnimator = getDefaultCenterViewAnimator(centerView)
            this.defaultCenterViewAnimator = defaultCenterViewAnimator
            defaultCenterViewAnimator.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        centerView.visibility = View.GONE
                        secondaryViewAnimator.start()
                    }
                },
            )
            defaultCenterViewAnimator.start()
        } else {
            secondaryViewAnimator.start()
        }
    }

    fun stopOnLoadAnimation(searchBar: SearchBar) {
        if (secondaryViewAnimator != null) {
            secondaryViewAnimator!!.end()
        }
        if (defaultCenterViewAnimator != null) {
            defaultCenterViewAnimator!!.end()
        }
        val centerView = searchBar.getCenterView()
        if (centerView is AnimatableView) {
            (centerView as AnimatableView).stopAnimation()
        }
        if (centerView != null) {
            centerView.alpha = 0f
        }
    }

    fun addOnLoadAnimationCallback(onLoadAnimationCallback: SearchBar.OnLoadAnimationCallback) {
        onLoadAnimationCallbacks.add(onLoadAnimationCallback)
    }

    fun removeOnLoadAnimationCallback(onLoadAnimationCallback: SearchBar.OnLoadAnimationCallback): Boolean {
        return onLoadAnimationCallbacks.remove(onLoadAnimationCallback)
    }

    private fun dispatchOnLoadAnimation(invocation: (SearchBar.OnLoadAnimationCallback) -> Unit) {
        onLoadAnimationCallbacks.forEach { callback ->
            invocation.invoke(callback)
        }
    }

    private fun getDefaultCenterViewAnimator(centerView: View?): Animator {
        val fadeInAnimator = ValueAnimator.ofFloat(0f, 1f)
        fadeInAnimator.addUpdateListener(MultiViewUpdateListener.alphaListener(centerView))
        fadeInAnimator.interpolator = AnimationUtils.LINEAR_INTERPOLATOR
        fadeInAnimator.duration =
            if (isOnLoadAnimationFadeInEnabled) ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_DURATION_MS else 0
        fadeInAnimator.startDelay =
            if (isOnLoadAnimationFadeInEnabled) ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_IN_START_DELAY_MS else 0
        val fadeOutAnimator = ValueAnimator.ofFloat(1f, 0f)
        fadeOutAnimator.addUpdateListener(MultiViewUpdateListener.alphaListener(centerView))
        fadeOutAnimator.interpolator = AnimationUtils.LINEAR_INTERPOLATOR
        fadeOutAnimator.duration = ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_DURATION_MS
        fadeOutAnimator.startDelay = ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_OUT_START_DELAY_MS
        val animatorSet = AnimatorSet()
        animatorSet.playSequentially(fadeInAnimator, fadeOutAnimator)
        return animatorSet
    }

    private fun getSecondaryViewAnimator(
        textView: TextView,
        secondaryActionMenuItemView: View?,
    ): Animator {
        val animatorSet = AnimatorSet()
        animatorSet.startDelay = ON_LOAD_ANIM_SECONDARY_START_DELAY_MS
        animatorSet.play(getTextViewAnimator(textView))
        if (secondaryActionMenuItemView != null) {
            animatorSet.play(getSecondaryActionMenuItemAnimator(secondaryActionMenuItemView))
        }
        return animatorSet
    }

    private fun getTextViewAnimator(textView: TextView): Animator {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener(MultiViewUpdateListener.alphaListener(textView))
        animator.interpolator = AnimationUtils.LINEAR_INTERPOLATOR
        animator.duration = ON_LOAD_ANIM_SECONDARY_DURATION_MS
        return animator
    }

    private fun getSecondaryActionMenuItemAnimator(secondaryActionMenuItemView: View?): Animator {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener(MultiViewUpdateListener.alphaListener(secondaryActionMenuItemView))
        animator.interpolator = AnimationUtils.LINEAR_INTERPOLATOR
        animator.duration = ON_LOAD_ANIM_SECONDARY_DURATION_MS
        return animator
    }

    fun startExpandAnimation(
        searchBar: SearchBar,
        expandedView: View,
        appBarLayout: AppBarLayout?,
        skipAnimation: Boolean,
    ) {
        // If we are in the middle of an collapse animation we should cancel it before we start the
        // expand.
        if (isCollapsing && runningExpandOrCollapseAnimator != null) {
            runningExpandOrCollapseAnimator!!.cancel()
        }
        isExpanding = true
        expandedView.visibility = View.INVISIBLE
        expandedView.post {
            val fadeAndExpandAnimatorSet = AnimatorSet()
            val fadeOutChildrenAnimator =
                getFadeOutChildrenAnimator(searchBar, expandedView)
            val expandAnimator =
                getExpandAnimator(searchBar, expandedView, appBarLayout)
            fadeAndExpandAnimatorSet.playSequentially(fadeOutChildrenAnimator, expandAnimator)
            fadeAndExpandAnimatorSet.addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        runningExpandOrCollapseAnimator = null
                    }
                },
            )
            for (listener in expandAnimationListeners) {
                fadeAndExpandAnimatorSet.addListener(listener)
            }
            if (skipAnimation) {
                fadeAndExpandAnimatorSet.duration = 0
            }
            fadeAndExpandAnimatorSet.start()
            runningExpandOrCollapseAnimator = fadeAndExpandAnimatorSet
        }
    }

    private fun getExpandAnimator(
        searchBar: SearchBar,
        expandedView: View,
        appBarLayout: AppBarLayout?,
    ): Animator {
        return getExpandCollapseAnimationHelper(searchBar, expandedView, appBarLayout)
            .setDuration(EXPAND_DURATION_MS)
            .addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        searchBar.visibility = View.INVISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        isExpanding = false
                    }
                },
            )
            .expandAnimator
    }

    fun addExpandAnimationListener(listener: AnimatorListenerAdapter) {
        expandAnimationListeners.add(listener)
    }

    fun removeExpandAnimationListener(listener: AnimatorListenerAdapter): Boolean {
        return expandAnimationListeners.remove(listener)
    }

    fun startCollapseAnimation(
        searchBar: SearchBar,
        expandedView: View,
        appBarLayout: AppBarLayout?,
        skipAnimation: Boolean,
    ) {
        // If we are in the middle of an expand animation we should cancel it before we start the
        // collapse.
        if (isExpanding && runningExpandOrCollapseAnimator != null) {
            runningExpandOrCollapseAnimator!!.cancel()
        }
        isCollapsing = true
        val collapseAndFadeAnimatorSet = AnimatorSet()
        val collapseAnimator = getCollapseAnimator(searchBar, expandedView, appBarLayout)
        val fadeInChildrenAnimator = getFadeInChildrenAnimator(searchBar)
        collapseAndFadeAnimatorSet.playSequentially(collapseAnimator, fadeInChildrenAnimator)
        collapseAndFadeAnimatorSet.addListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    runningExpandOrCollapseAnimator = null
                }
            },
        )
        for (listener in collapseAnimationListeners) {
            collapseAndFadeAnimatorSet.addListener(listener)
        }
        if (skipAnimation) {
            collapseAndFadeAnimatorSet.duration = 0
        }
        collapseAndFadeAnimatorSet.start()
        runningExpandOrCollapseAnimator = collapseAndFadeAnimatorSet
    }

    private fun getCollapseAnimator(
        searchBar: SearchBar,
        expandedView: View,
        appBarLayout: AppBarLayout?,
    ): Animator {
        return getExpandCollapseAnimationHelper(searchBar, expandedView, appBarLayout)
            .setDuration(COLLAPSE_DURATION_MS)
            .addListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        searchBar.stopOnLoadAnimation()
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        searchBar.visibility = View.VISIBLE
                        isCollapsing = false
                    }
                },
            )
            .collapseAnimator
    }

    fun addCollapseAnimationListener(listener: AnimatorListenerAdapter) {
        collapseAnimationListeners.add(listener)
    }

    fun removeCollapseAnimationListener(listener: AnimatorListenerAdapter): Boolean {
        return collapseAnimationListeners.remove(listener)
    }

    private fun getExpandCollapseAnimationHelper(
        searchBar: SearchBar,
        expandedView: View,
        appBarLayout: AppBarLayout?,
    ): ExpandCollapseAnimationHelper {
        return ExpandCollapseAnimationHelper(searchBar, expandedView)
            .setAdditionalUpdateListener(
                getExpandedViewBackgroundUpdateListener(searchBar, expandedView),
            )
            .setCollapsedViewOffsetY(appBarLayout?.top ?: 0)
            .addEndAnchoredViews(getEndAnchoredViews(expandedView))
    }

    private fun getExpandedViewBackgroundUpdateListener(
        searchBar: SearchBar,
        expandedView: View,
    ): AnimatorUpdateListener {
        val expandedViewBackground = MaterialShapeDrawable.createWithElevationOverlay(expandedView.context)
        expandedViewBackground.setCornerSize(searchBar.cornerSize)
        expandedViewBackground.elevation = ViewCompat.getElevation(searchBar)
        return AnimatorUpdateListener { valueAnimator: ValueAnimator ->
            expandedViewBackground.interpolation = 1 - valueAnimator.animatedFraction
            ViewCompat.setBackground(expandedView, expandedViewBackground)

            // Ensures that the expanded view is visible, in the case where ActionMode is used.
            expandedView.alpha = 1f
        }
    }

    private fun getFadeOutChildrenAnimator(searchBar: SearchBar, expandedView: View): Animator {
        val children = getFadeChildren(searchBar)
        val animator = ValueAnimator.ofFloat(1f, 0f)
        animator.addUpdateListener(MultiViewUpdateListener.alphaListener(children))
        animator.addUpdateListener {
            // Ensures that the expanded view is not visible while the children are fading out, in
            // the case where ActionMode is used.
            expandedView.alpha = 0f
        }
        animator.duration = EXPAND_FADE_OUT_CHILDREN_DURATION_MS
        animator.interpolator = AnimationUtils.LINEAR_INTERPOLATOR
        return animator
    }

    private fun getFadeInChildrenAnimator(searchBar: SearchBar): Animator {
        val children = getFadeChildren(searchBar)
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.addUpdateListener(MultiViewUpdateListener.alphaListener(children))
        animator.duration = COLLAPSE_FADE_IN_CHILDREN_DURATION_MS
        animator.interpolator = AnimationUtils.LINEAR_INTERPOLATOR
        return animator
    }

    private fun getFadeChildren(searchBar: SearchBar): List<View?> {
        val children = ViewUtils.getChildren(searchBar)
        if (searchBar.getCenterView() != null) {
            children.remove(searchBar.getCenterView())
        }
        return children
    }

    private fun getEndAnchoredViews(expandedView: View): List<View> {
        val isRtl = ViewUtils.isLayoutRtl(expandedView)
        val endAnchoredViews: MutableList<View> = ArrayList()
        if (expandedView is ViewGroup) {
            for (i in 0 until expandedView.childCount) {
                val child = expandedView.getChildAt(i)
                if (!isRtl && child is ActionMenuView || isRtl && child !is ActionMenuView) {
                    endAnchoredViews.add(child)
                }
            }
        }
        return endAnchoredViews
    }

    companion object {
        // On load animation constants
        private const val ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_DURATION_MS: Long = 250
        private const val ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_IN_START_DELAY_MS: Long = 500
        private const val ON_LOAD_ANIM_CENTER_VIEW_DEFAULT_FADE_OUT_START_DELAY_MS: Long = 750
        private const val ON_LOAD_ANIM_SECONDARY_DURATION_MS: Long = 250
        private const val ON_LOAD_ANIM_SECONDARY_START_DELAY_MS: Long = 250

        // Expand and collapse animation constants
        private const val EXPAND_DURATION_MS: Long = 300
        private const val EXPAND_FADE_OUT_CHILDREN_DURATION_MS: Long = 75
        private const val COLLAPSE_DURATION_MS: Long = 250
        private const val COLLAPSE_FADE_IN_CHILDREN_DURATION_MS: Long = 100
    }
}