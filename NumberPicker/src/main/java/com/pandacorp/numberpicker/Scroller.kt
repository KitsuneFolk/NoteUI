/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pandacorp.numberpicker

import android.content.Context
import android.hardware.SensorManager
import android.os.Build
import android.view.ViewConfiguration
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sign

/**
 * This class encapsulates scrolling. You can use scrollers ([Scroller]
 * to collect the data you need to produce a scrolling animationfor
 * example, in response to a fling gesture. Scrollers track scroll offsets
 * for you over time, but they don't automatically apply those positions
 * to your view. It's your responsibility to get and apply new coordinates
 * at a rate that will make the scrolling animation look smooth.
 *
 *
 * Here is a simple example:
 *
 * <pre> private Scroller mScroller = new Scroller(context);
 * ...
 * public void zoomIn() {
 * // Revert any animation currently in progress
 * mScroller.forceFinished(true);
 * // Start scrolling by providing a starting point and
 * // the distance to travel
 * mScroller.startScroll(0, 0, 100, 0);
 * // Invalidate to request a redraw
 * invalidate();
 * }</pre>
 *
 *
 * To track the changing positions of the x/y coordinates, use
 * [.computeScrollOffset]. The method returns a boolean to indicate
 * whether the scroller is finished. If it isn't, it means that a fling or
 * programmatic pan operation is still in progress. You can use this method to
 * find the current offsets of the x and y coordinates, for example:
 *
 * <pre>if (mScroller.computeScrollOffset()) {
 * // Get current x and y positions
 * int currX = mScroller.getCurrX();
 * int currY = mScroller.getCurrY();
 * ...
 * }</pre>
 */
class Scroller @JvmOverloads constructor(
    context: Context, interpolator: Interpolator? = null, flywheel: Boolean =
        context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.HONEYCOMB
) {
    private var mInterpolator: Interpolator? = null
    private var mMode = 0

    /**
     * Returns the start X offset in the scroll.
     *
     * @return The start X offset as an absolute distance from the origin.
     */
    var startX = 0
        private set

    /**
     * Returns the start Y offset in the scroll.
     *
     * @return The start Y offset as an absolute distance from the origin.
     */
    var startY = 0
        private set
    private var mFinalX = 0
    private var mFinalY = 0
    private var mMinX = 0
    private var mMaxX = 0
    private var mMinY = 0
    private var mMaxY = 0

    /**
     * Returns the current X offset in the scroll.
     *
     * @return The new X offset as an absolute distance from the origin.
     */
    var currX = 0
        private set

    /**
     * Returns the current Y offset in the scroll.
     *
     * @return The new Y offset as an absolute distance from the origin.
     */
    var currY = 0
        private set
    private var mStartTime: Long = 0

    /**
     * Returns how long the scroll event will take, in milliseconds.
     *
     * @return The duration of the scroll in milliseconds.
     */
    private var duration = 0
    private var mDurationReciprocal = 0f
    private var mDeltaX = 0f
    private var mDeltaY = 0f

    /**
     *
     * Returns whether the scroller has finished scrolling.
     *
     * @return True if the scroller has finished scrolling, false otherwise.
     */
    var isFinished = true
        private set
    private val mFlywheel: Boolean
    private var mVelocity = 0f
    private var mCurrVelocity = 0f
    private var mDistance = 0
    private var mFlingFriction = ViewConfiguration.getScrollFriction()
    private var mDeceleration: Float
    private val mPpi: Float

    // A context-specific coefficient adjusted to physical values.
    private val mPhysicalCoeff: Float
    /**
     * Create a Scroller with the specified interpolator. If the interpolator is
     * null, the default (viscous) interpolator will be used. Specify whether or
     * not to support progressive "flywheel" behavior in flinging.
     */
    /**
     * Create a Scroller with the specified interpolator. If the interpolator is
     * null, the default (viscous) interpolator will be used. "Flywheel" behavior will
     * be in effect for apps targeting Honeycomb or newer.
     */
    /**
     * Create a Scroller with the default duration and interpolator.
     */
    init {
        mInterpolator = interpolator ?: ViscousFluidInterpolator()
        mPpi = context.resources.displayMetrics.density * 160.0f
        mDeceleration = computeDeceleration(ViewConfiguration.getScrollFriction())
        mFlywheel = flywheel
        mPhysicalCoeff = computeDeceleration(0.84f) // look and feel tuning
    }

    private fun computeDeceleration(friction: Float): Float {
        return (SensorManager.GRAVITY_EARTH // g (m/s^2)
                * 39.37f // inch/meter
                * mPpi // pixels per inch
                * friction)
    }

    /**
     * Force the finished field to a particular value.
     *
     * @param finished The new finished value.
     */
    fun forceFinished(finished: Boolean) {
        isFinished = finished
    }

    private val currVelocity: Float
        /**
         * Returns the current velocity.
         *
         * @return The original velocity less the deceleration. Result may be
         * negative.
         */
        get() = if (mMode == FLING_MODE) mCurrVelocity else mVelocity - mDeceleration * timePassed() / 2000.0f
    var finalX: Int
        /**
         * Returns where the scroll will end. Valid only for "fling" scrolls.
         *
         * @return The final X offset as an absolute distance from the origin.
         */
        get() = mFinalX
        /**
         * Sets the final position (X) for this scroller.
         *
         * @param newX The new X offset as an absolute distance from the origin.
         * @see .extendDuration
         * @see .setFinalY
         */
        set(newX) {
            mFinalX = newX
            mDeltaX = (mFinalX - startX).toFloat()
            isFinished = false
        }
    var finalY: Int
        /**
         * Returns where the scroll will end. Valid only for "fling" scrolls.
         *
         * @return The final Y offset as an absolute distance from the origin.
         */
        get() = mFinalY
        /**
         * Sets the final position (Y) for this scroller.
         *
         * @param newY The new Y offset as an absolute distance from the origin.
         * @see .extendDuration
         * @see .setFinalX
         */
        set(newY) {
            mFinalY = newY
            mDeltaY = (mFinalY - startY).toFloat()
            isFinished = false
        }

    /**
     * Call this when you want to know the new location.  If it returns true,
     * the animation is not yet finished.
     */
    fun computeScrollOffset(): Boolean {
        if (isFinished) {
            return false
        }
        val timePassed = (AnimationUtils.currentAnimationTimeMillis() - mStartTime).toInt()
        if (timePassed < duration) {
            when (mMode) {
                SCROLL_MODE -> {
                    val x = mInterpolator!!.getInterpolation(timePassed * mDurationReciprocal)
                    currX = startX + (x * mDeltaX).roundToInt()
                    currY = startY + (x * mDeltaY).roundToInt()
                }

                FLING_MODE -> {
                    val t = timePassed.toFloat() / duration
                    val index = (NB_SAMPLES * t).toInt()
                    var distanceCoef = 1f
                    var velocityCoef = 0f
                    if (index < NB_SAMPLES) {
                        val tInf = index.toFloat() / NB_SAMPLES
                        val tSup = (index + 1).toFloat() / NB_SAMPLES
                        val dInf = SPLINE_POSITION[index]
                        val dSup = SPLINE_POSITION[index + 1]
                        velocityCoef = (dSup - dInf) / (tSup - tInf)
                        distanceCoef = dInf + (t - tInf) * velocityCoef
                    }
                    mCurrVelocity = velocityCoef * mDistance / duration * 1000.0f
                    currX = startX + (distanceCoef * (mFinalX - startX)).roundToInt()
                    // Pin to mMinX <= mCurrX <= mMaxX
                    currX = currX.coerceAtMost(mMaxX)
                    currX = currX.coerceAtLeast(mMinX)
                    currY = startY + (distanceCoef * (mFinalY - startY)).roundToInt()
                    // Pin to mMinY <= mCurrY <= mMaxY
                    currY = currY.coerceAtMost(mMaxY)
                    currY = currY.coerceAtLeast(mMinY)
                    if (currX == mFinalX && currY == mFinalY) {
                        isFinished = true
                    }
                }
            }
        } else {
            currX = mFinalX
            currY = mFinalY
            isFinished = true
        }
        return true
    }

    /**
     * Start scrolling by providing a starting point and the distance to travel.
     * The scroll will use the default value of 250 milliseconds for the
     * duration.
     *
     * @param startX Starting horizontal scroll offset in pixels. Positive
     * numbers will scroll the content to the left.
     * @param startY Starting vertical scroll offset in pixels. Positive numbers
     * will scroll the content up.
     * @param dx Horizontal distance to travel. Positive numbers will scroll the
     * content to the left.
     * @param dy Vertical distance to travel. Positive numbers will scroll the
     * content up.
     */
    @JvmOverloads
    fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int = DEFAULT_DURATION) {
        mMode = SCROLL_MODE
        isFinished = false
        this.duration = duration
        mStartTime = AnimationUtils.currentAnimationTimeMillis()
        this.startX = startX
        this.startY = startY
        mFinalX = startX + dx
        mFinalY = startY + dy
        mDeltaX = dx.toFloat()
        mDeltaY = dy.toFloat()
        mDurationReciprocal = 1.0f / duration.toFloat()
    }

    /**
     * Start scrolling based on a fling gesture. The distance travelled will
     * depend on the initial velocity of the fling.
     *
     * @param startX Starting point of the scroll (X)
     * @param startY Starting point of the scroll (Y)
     * @param velocityX Initial velocity of the fling (X) measured in pixels per
     * second.
     * @param velocityY Initial velocity of the fling (Y) measured in pixels per
     * second
     * @param minX Minimum X value. The scroller will not scroll past this
     * point.
     * @param maxX Maximum X value. The scroller will not scroll past this
     * point.
     * @param minY Minimum Y value. The scroller will not scroll past this
     * point.
     * @param maxY Maximum Y value. The scroller will not scroll past this
     * point.
     */
    fun fling(
        startX: Int, startY: Int, velocityX: Int, velocityY: Int,
        minX: Int, maxX: Int, minY: Int, maxY: Int
    ) {
        // Continue a scroll or fling in progress
        var mVelocityX = velocityX
        var mVelocityY = velocityY
        if (mFlywheel && !isFinished) {
            val oldVel = currVelocity
            val dx = (mFinalX - this.startX).toFloat()
            val dy = (mFinalY - this.startY).toFloat()
            val hyp = hypot(dx.toDouble(), dy.toDouble()).toFloat()
            val ndx = dx / hyp
            val ndy = dy / hyp
            val oldVelocityX = ndx * oldVel
            val oldVelocityY = ndy * oldVel
            if (sign(mVelocityX.toFloat()) == sign(oldVelocityX) &&
                sign(mVelocityY.toFloat()) == sign(oldVelocityY)
            ) {
                mVelocityX += oldVelocityX.toInt()
                mVelocityY += oldVelocityY.toInt()
            }
        }
        mMode = FLING_MODE
        isFinished = false
        val velocity = hypot(mVelocityX.toDouble(), mVelocityY.toDouble()).toFloat()
        mVelocity = velocity
        duration = getSplineFlingDuration(velocity)
        mStartTime = AnimationUtils.currentAnimationTimeMillis()
        this.startX = startX
        this.startY = startY
        val coeffX = if (velocity == 0f) 1.0f else mVelocityX / velocity
        val coeffY = if (velocity == 0f) 1.0f else mVelocityY / velocity
        val totalDistance = getSplineFlingDistance(velocity)
        mDistance = (totalDistance * sign(velocity)).toInt()
        mMinX = minX
        mMaxX = maxX
        mMinY = minY
        mMaxY = maxY
        mFinalX = startX + (totalDistance * coeffX).roundToInt()
        // Pin to mMinX <= mFinalX <= mMaxX
        mFinalX = mFinalX.coerceAtMost(mMaxX)
        mFinalX = mFinalX.coerceAtLeast(mMinX)
        mFinalY = startY + (totalDistance * coeffY).roundToInt()
        // Pin to mMinY <= mFinalY <= mMaxY
        mFinalY = mFinalY.coerceAtMost(mMaxY)
        mFinalY = mFinalY.coerceAtLeast(mMinY)
    }

    private fun getSplineDeceleration(velocity: Float): Double {
        return ln((INFLEXION * abs(velocity) / (mFlingFriction * mPhysicalCoeff)).toDouble())
    }

    private fun getSplineFlingDuration(velocity: Float): Int {
        val l = getSplineDeceleration(velocity)
        val decelMinusOne = DECELERATION_RATE - 1.0
        return (1000.0 * exp(l / decelMinusOne)).toInt()
    }

    private fun getSplineFlingDistance(velocity: Float): Double {
        val l = getSplineDeceleration(velocity)
        val decelMinusOne = DECELERATION_RATE - 1.0
        return mFlingFriction * mPhysicalCoeff * exp(DECELERATION_RATE / decelMinusOne * l)
    }

    /**
     * Returns the time elapsed since the beginning of the scrolling.
     *
     * @return The elapsed time in milliseconds.
     */
    private fun timePassed(): Int {
        return (AnimationUtils.currentAnimationTimeMillis() - mStartTime).toInt()
    }

    internal class ViscousFluidInterpolator : Interpolator {
        override fun getInterpolation(input: Float): Float {
            val interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input)
            return if (interpolated > 0) {
                interpolated + VISCOUS_FLUID_OFFSET
            } else interpolated
        }

        companion object {
            /** Controls the viscous fluid effect (how much of it).  */
            private const val VISCOUS_FLUID_SCALE = 8.0f
            private val VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(1.0f)
            private val VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f)

            private fun viscousFluid(x: Float): Float {
                var mX = x
                mX *= VISCOUS_FLUID_SCALE
                if (mX < 1.0f) {
                    mX -= 1.0f - exp(-mX.toDouble()).toFloat()
                } else {
                    val start = 0.36787945f // 1/e == exp(-1)
                    mX = 1.0f - exp((1.0f - mX).toDouble()).toFloat()
                    mX = start + mX * (1.0f - start)
                }
                return mX
            }
        }
    }

    companion object {
        private const val DEFAULT_DURATION = 250
        private const val SCROLL_MODE = 0
        private const val FLING_MODE = 1
        private val DECELERATION_RATE = (ln(0.78) / ln(0.9)).toFloat()
        private const val INFLEXION = 0.35f // Tension lines cross at (INFLEXION, 1)
        private const val START_TENSION = 0.5f
        private const val END_TENSION = 1.0f
        private const val P1 = START_TENSION * INFLEXION
        private const val P2 = 1.0f - END_TENSION * (1.0f - INFLEXION)
        private const val NB_SAMPLES = 100
        private val SPLINE_POSITION = FloatArray(NB_SAMPLES + 1)
        private val SPLINE_TIME = FloatArray(NB_SAMPLES + 1)

        init {
            var xMin = 0.0f
            var yMin = 0.0f
            for (i in 0 until NB_SAMPLES) {
                val alpha = i.toFloat() / NB_SAMPLES
                var xMax = 1.0f
                var x: Float
                var tx: Float
                var coef: Float
                while (true) {
                    x = xMin + (xMax - xMin) / 2.0f
                    coef = 3.0f * x * (1.0f - x)
                    tx = coef * ((1.0f - x) * P1 + x * P2) + x * x * x
                    if (abs(tx - alpha) < 1E-5) break
                    if (tx > alpha) xMax = x else xMin = x
                }
                SPLINE_POSITION[i] = coef * ((1.0f - x) * START_TENSION + x) + x * x * x
                var yMax = 1.0f
                var y: Float
                var dy: Float
                while (true) {
                    y = yMin + (yMax - yMin) / 2.0f
                    coef = 3.0f * y * (1.0f - y)
                    dy = coef * ((1.0f - y) * START_TENSION + y) + y * y * y
                    if (abs(dy - alpha) < 1E-5) break
                    if (dy > alpha) yMax = y else yMin = y
                }
                SPLINE_TIME[i] = coef * ((1.0f - y) * P1 + y * P2) + y * y * y
            }
            SPLINE_TIME[NB_SAMPLES] = 1.0f
            SPLINE_POSITION[NB_SAMPLES] = SPLINE_TIME[NB_SAMPLES]
        }
    }
}