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
package com.pandacorp.searchbar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ActionMenuView
import androidx.core.view.ViewCompat
import com.google.android.material.animation.AnimatableView
import com.google.android.material.animation.AnimationUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.internal.ExpandCollapseAnimationHelper
import com.google.android.material.internal.MultiViewUpdateListener
import com.google.android.material.internal.ViewUtils
import com.google.android.material.shape.MaterialShapeDrawable

/** Helper class for [SearchBar] animations.  */
@SuppressLint("RestrictedApi")
internal class SearchBarAnimationHelper {
    private val expandAnimationListeners: MutableSet<AnimatorListenerAdapter> = LinkedHashSet()
    private val collapseAnimationListeners: MutableSet<AnimatorListenerAdapter> = LinkedHashSet()
    private var secondaryViewAnimator: Animator? = null
    private var defaultCenterViewAnimator: Animator? = null
    var isExpanding = false
        private set
    var isCollapsing = false
        private set
    private var runningExpandOrCollapseAnimator: Animator? = null

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
                }
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

    private fun getExpandAnimator(searchBar: SearchBar, expandedView: View, appBarLayout: AppBarLayout?,): Animator {
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
                }
            )
            .expandAnimator
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
            }
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

    private fun getCollapseAnimator(searchBar: SearchBar, expandedView: View, appBarLayout: AppBarLayout?,): Animator {
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
                }
            )
            .collapseAnimator
    }

    private fun getExpandCollapseAnimationHelper(
        searchBar: SearchBar,
        expandedView: View,
        appBarLayout: AppBarLayout?,
    ): ExpandCollapseAnimationHelper {
        return ExpandCollapseAnimationHelper(searchBar, expandedView)
            .setAdditionalUpdateListener(
                getExpandedViewBackgroundUpdateListener(searchBar, expandedView)
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
        // Expand and collapse animation constants
        private const val EXPAND_DURATION_MS: Long = 300
        private const val EXPAND_FADE_OUT_CHILDREN_DURATION_MS: Long = 75
        private const val COLLAPSE_DURATION_MS: Long = 250
        private const val COLLAPSE_FADE_IN_CHILDREN_DURATION_MS: Long = 100
    }
}