package com.pandacorp.noteui.presentation.utils.views.animatedtextview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityNodeInfo
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@SuppressLint("RtlHardcoded")
class AnimatedTextView @JvmOverloads constructor(
    context: Context?,
    splitByWords: Boolean = false,
    preserveIndex: Boolean = false,
    startFromEnd: Boolean = false
) : View(context) {
    class AnimatedTextDrawable(
        private val splitByWords: Boolean,
        private val preserveIndex: Boolean,
        private val startFromEnd: Boolean
    ) : Drawable() {
        private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        private var gravity = 0
        private var isRTL = false
        private var currentWidth = 0f
        var height = 0f
            private set
        private var currentParts: Array<Part?>? = null
        private var currentText: CharSequence? = null
        private var oldWidth = 0f
        private var oldHeight = 0f
        private var oldParts: Array<Part?>? = null
        private var oldText: CharSequence? = null

        private class Part(var layout: StaticLayout?, var offset: Float, var toOppositeIndex: Int) {
            var left = if (layout == null || layout!!.lineCount <= 0) 0f else layout!!.getLineLeft(0)
            var width = if (layout == null || layout!!.lineCount <= 0) 0f else layout!!.getLineWidth(0)
        }

        private var t = 0f
        private var moveDown = true
        var animator: ValueAnimator? = null
        private var toSetText: CharSequence? = null
        private var toSetTextMoveDown = false
        private var animateDelay: Long = 0
        private var animateDuration: Long = 450
        private var animateInterpolator: TimeInterpolator = CubicBezierInterpolator.EASE_OUT_QUINT
        private var moveAmplitude = 1f
        private var alpha = 255
        private val bounds = Rect()
        private var onAnimationFinishListener: Runnable? = null
        var allowCancel = false
        private var ignoreRTL = false
        private var updateAll = false

        fun setOnAnimationFinishListener(listener: Runnable?) {
            onAnimationFinishListener = listener
        }

        private fun applyAlphaInternal(t: Float) {
            textPaint.alpha = (alpha * t).toInt()
        }

        override fun draw(canvas: Canvas) {
            canvas.save()
            canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
            val fullWidth = bounds.width()
            val fullHeight = bounds.height()
            if (currentParts != null && oldParts != null && t != 1f) {
                val width = AndroidUtilities.lerp(oldWidth, currentWidth, t)
                val height = AndroidUtilities.lerp(oldHeight, height, t)
                canvas.translate(0f, (fullHeight - height) / 2f)
                for (current in currentParts!!) {
                    val j = current!!.toOppositeIndex
                    var x = current.offset
                    var y = 0f
                    if (isRTL && !ignoreRTL) {
                        x = currentWidth - (x + current.width)
                    }
                    if (j >= 0) {
                        val old = oldParts!![j]
                        var oldX = old!!.offset
                        if (isRTL && !ignoreRTL) {
                            oldX = oldWidth - (oldX + old.width)
                        }
                        x = AndroidUtilities.lerp(oldX - old.left, x - current.left, t)
                        applyAlphaInternal(1f)
                    } else {
                        x -= current.left
                        y = -textPaint.textSize * moveAmplitude * (1f - t) * if (moveDown) 1f else -1f
                        applyAlphaInternal(t)
                    }
                    canvas.save()
                    val lwidth = if (j >= 0) width else currentWidth
                    if (gravity or Gravity.LEFT.inv() != 0.inv()) {
                        if (gravity or Gravity.RIGHT.inv() == 0.inv()) {
                            x += fullWidth - lwidth
                        } else if (gravity or Gravity.CENTER_HORIZONTAL.inv() == 0.inv()) {
                            x += (fullWidth - lwidth) / 2f
                        } else if (isRTL && !ignoreRTL) {
                            x += fullWidth - lwidth
                        }
                    }
                    canvas.translate(x, y)
                    current.layout!!.draw(canvas)
                    canvas.restore()
                }
                for (old in oldParts!!) {
                    val j = old!!.toOppositeIndex
                    if (j >= 0) {
                        continue
                    }
                    var x = old.offset
                    val y = textPaint.textSize * moveAmplitude * t * if (moveDown) 1f else -1f
                    applyAlphaInternal(1f - t)
                    canvas.save()
                    if (isRTL && !ignoreRTL) {
                        x = oldWidth - (x + old.width)
                    }
                    x -= old.left
                    if (gravity or Gravity.LEFT.inv() != 0.inv()) {
                        if (gravity or Gravity.RIGHT.inv() == 0.inv()) {
                            x += fullWidth - oldWidth
                        } else if (gravity or Gravity.CENTER_HORIZONTAL.inv() == 0.inv()) {
                            x += (fullWidth - oldWidth) / 2f
                        } else if (isRTL && !ignoreRTL) {
                            x += fullWidth - oldWidth
                        }
                    }
                    canvas.translate(x, y)
                    old.layout!!.draw(canvas)
                    canvas.restore()
                }
            } else {
                canvas.translate(0f, (fullHeight - height) / 2f)
                if (currentParts != null) {
                    applyAlphaInternal(1f)
                    for (currentPart in currentParts!!) {
                        canvas.save()
                        var x = currentPart!!.offset
                        if (isRTL && !ignoreRTL) {
                            x = currentWidth - (x + currentPart.width)
                        }
                        x -= currentPart.left
                        if (gravity or Gravity.LEFT.inv() != 0.inv()) {
                            if (gravity or Gravity.RIGHT.inv() == 0.inv()) {
                                x += fullWidth - currentWidth
                            } else if (gravity or Gravity.CENTER_HORIZONTAL.inv() == 0.inv()) {
                                x += (fullWidth - currentWidth) / 2f
                            } else if (isRTL && !ignoreRTL) {
                                x += fullWidth - currentWidth
                            }
                        }
                        //                        boolean isAppeared = currentLayoutToOldIndex != null && i < currentLayoutToOldIndex.length && currentLayoutToOldIndex[i] < 0;
                        canvas.translate(x, 0f)
                        currentPart.layout!!.draw(canvas)
                        canvas.restore()
                    }
                }
            }
            canvas.restore()
        }

        val isAnimating: Boolean
            get() = animator != null && animator!!.isRunning

        fun setText(text: CharSequence?) {
            setText(text, true)
        }

        fun setText(text: CharSequence?, withAnimation: Boolean) {
            setText(text, withAnimation, true)
        }

        fun setText(text: CharSequence?, withAnimation: Boolean, moveDown: Boolean) {
            var newText = text
            var animated = withAnimation
            if (currentText == null || newText == null) {
                animated = false
            }
            if (newText == null) {
                newText = ""
            }
            val width = bounds.width()
            if (animated) {
                if (allowCancel) {
                    if (animator != null) {
                        animator!!.cancel()
                        animator = null
                    }
                } else if (isAnimating) {
                    toSetText = newText
                    toSetTextMoveDown = moveDown
                    return
                }
                if (newText == currentText) {
                    return
                }
                oldText = currentText
                currentText = newText
                val currentParts = ArrayList<Part>()
                val oldParts = ArrayList<Part>()
                height = 0f
                currentWidth = height
                oldHeight = 0f
                oldWidth = oldHeight
                isRTL = AndroidUtilities.isRTL(currentText)

                // order execution matters
                val onEqualRegion = RegionCallback { part, _, _ ->
                    val layout = makeLayout(
                        part,
                        width - ceil(
                            currentWidth.coerceAtMost(oldWidth).toDouble(),
                        ).toInt(),
                    )
                    val currentPart = Part(layout, currentWidth, oldParts.size)
                    val oldPart = Part(layout, oldWidth, oldParts.size)
                    currentParts.add(currentPart)
                    oldParts.add(oldPart)
                    val partWidth = currentPart.width
                    currentWidth += partWidth
                    oldWidth += partWidth
                    height = height.coerceAtLeast(layout.height.toFloat())
                    oldHeight = oldHeight.coerceAtLeast(layout.height.toFloat())
                }
                val onNewPart = RegionCallback { part: CharSequence, _: Int, _: Int ->
                    val layout = makeLayout(part, width - ceil(currentWidth.toDouble()).toInt())
                    val currentPart = Part(layout, currentWidth, -1)
                    currentParts.add(currentPart)
                    currentWidth += currentPart.width
                    height = max(height, layout.height.toFloat())
                }
                val onOldPart = RegionCallback { part: CharSequence, _: Int, _: Int ->
                    val layout = makeLayout(part, width - ceil(oldWidth.toDouble()).toInt())
                    val oldPart = Part(layout, oldWidth, -1)
                    oldParts.add(oldPart)
                    oldWidth += oldPart.width
                    oldHeight = oldHeight.coerceAtLeast(layout.height.toFloat())
                }
                val from = if (splitByWords) WordSequence(oldText) else oldText!!
                val to = if (splitByWords) WordSequence(currentText) else currentText!!
                diff(from, to, onEqualRegion, onNewPart, onOldPart)
                //                betterDiff(from, to, onEqualRegion, onNewPart, onOldPart);
                if (this.currentParts == null || this.currentParts!!.size != currentParts.size) {
                    this.currentParts = arrayOfNulls(currentParts.size)
                }
                this.currentParts?.let { currentParts.toArray(it) }
                if (this.oldParts == null || this.oldParts!!.size != oldParts.size) {
                    this.oldParts = arrayOfNulls(oldParts.size)
                }
                this.oldParts?.let { oldParts.toArray(it) }
                if (animator != null) {
                    animator!!.cancel()
                }
                this.moveDown = moveDown
                animator = ValueAnimator.ofFloat(0f.also { t = it }, 1f).apply {
                    this.addUpdateListener { anm: ValueAnimator ->
                        t = anm.animatedValue as Float
                        invalidateSelf()
                    }
                    this.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            this@AnimatedTextDrawable.oldParts = null
                            oldText = null
                            oldWidth = 0f
                            t = 0f
                            invalidateSelf()
                            animator = null
                            if (toSetText != null) {
                                setText(toSetText, true, toSetTextMoveDown)
                                toSetText = null
                                toSetTextMoveDown = false
                            } else if (onAnimationFinishListener != null) {
                                onAnimationFinishListener!!.run()
                            }
                        }
                    })
                    this.startDelay = animateDelay
                    this.duration = animateDuration
                    this.interpolator = animateInterpolator
                }
                animator?.start()
            } else {
                if (animator != null) {
                    animator!!.cancel()
                }
                animator = null
                toSetText = null
                toSetTextMoveDown = false
                t = 0f
                if (newText != currentText) {
                    currentParts = arrayOfNulls(1)
                    currentParts!![0] = Part(makeLayout(newText.also { currentText = it }, width), 0f, -1)
                    currentWidth = currentParts!![0]!!.width
                    height = currentParts!![0]!!.layout!!.height.toFloat()
                    isRTL = AndroidUtilities.isRTL(currentText)
                }
                oldParts = null
                oldText = null
                oldWidth = 0f
                oldHeight = 0f
                invalidateSelf()
            }
        }

        fun getText(): CharSequence? {
            return currentText
        }

        val width: Float
            get() = max(currentWidth, oldWidth)

        fun getCurrentWidth(): Float {
            return if (currentParts != null && oldParts != null) {
                AndroidUtilities.lerp(oldWidth, currentWidth, t)
            } else {
                currentWidth
            }
        }

        private fun makeLayout(textPart: CharSequence, width: Int): StaticLayout {
            var newWidth = width
            if (newWidth <= 0) {
                newWidth = min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y)
            }
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(textPart, 0, textPart.length, textPaint, newWidth)
                    .setMaxLines(1)
                    .setLineSpacing(0f, 1f)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setEllipsize(TextUtils.TruncateAt.END)
                    .setEllipsizedWidth(newWidth)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(
                    textPart,
                    0, textPart.length,
                    textPaint,
                    newWidth,
                    Layout.Alignment.ALIGN_NORMAL,
                    1f,
                    0f,
                    false,
                    TextUtils.TruncateAt.END,
                    newWidth,
                )
            }
        }

        private class WordSequence(text: CharSequence?) : CharSequence {
            private var words: Array<CharSequence?>

            init {
                if (text == null) {
                    words = arrayOfNulls(0)
                } else {
                    val length = text.length
                    var spacesCount = 0
                    for (i in 0 until length) {
                        if (text[i] == SPACE) {
                            spacesCount++
                        }
                    }
                    var j = 0
                    words = arrayOfNulls(spacesCount + 1)
                    var start = 0
                    for (i in 0..length) {
                        if (i == length || text[i] == SPACE) {
                            words[j++] = text.subSequence(start, i + if (i < length) 1 else 0)
                            start = i + 1
                        }
                    }
                }
            }

            fun wordAt(i: Int): CharSequence? {
                return if (i < 0 || i >= words.size) {
                    null
                } else {
                    words[i]
                }
            }

            override val length: Int
                get() = words.size

            override fun get(index: Int): Char {
                var i = index
                for (word in words) {
                    if (i < word!!.length) {
                        return word[i]
                    }
                    i -= word.length
                }
                return 0.toChar()
            }

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                return TextUtils.concat(*words.copyOfRange(startIndex, endIndex))
            }

            override fun toString(): String {
                val sb = StringBuilder()
                for (word in words) {
                    sb.append(word)
                }
                return sb.toString()
            }

            companion object {
                private const val SPACE = ' '
            }
        }

        private fun diff(
            oldText: CharSequence,
            newText: CharSequence,
            onEqualPart: RegionCallback,
            onNewPart: RegionCallback,
            onOldPart: RegionCallback
        ) {
            if (updateAll) {
                onOldPart.run(oldText, 0, oldText.length)
                onNewPart.run(newText, 0, newText.length)
                return
            }
            if (preserveIndex) {
                var equal = true
                var start = 0
                val minLength = min(newText.length, oldText.length)
                if (startFromEnd) {
                    val indexes = ArrayList<Int>()
                    var eq = true
                    for (i in 0..minLength) {
                        val a = newText.length - i - 1
                        val b = oldText.length - i - 1
                        val thisEqual = a >= 0 && b >= 0 && partEquals(newText, oldText, a, b)
                        if (equal != thisEqual || i == minLength) {
                            if (i - start > 0) {
                                if (indexes.size == 0) {
                                    eq = equal
                                }
                                indexes.add(i - start)
                            }
                            equal = thisEqual
                            start = i
                        }
                    }
                    var a = newText.length - minLength
                    var b = oldText.length - minLength
                    if (a > 0) {
                        onNewPart.run(newText.subSequence(0, a), 0, a)
                    }
                    if (b > 0) {
                        onOldPart.run(oldText.subSequence(0, b), 0, b)
                    }
                    for (i in indexes.indices.reversed()) {
                        val count = indexes[i]
                        if (i % 2 == 0 == eq) {
                            if (newText.length > oldText.length) {
                                onEqualPart.run(newText.subSequence(a, a + count), a, a + count)
                            } else {
                                onEqualPart.run(oldText.subSequence(b, b + count), b, b + count)
                            }
                        } else {
                            onNewPart.run(newText.subSequence(a, a + count), a, a + count)
                            onOldPart.run(oldText.subSequence(b, b + count), b, b + count)
                        }
                        a += count
                        b += count
                    }
                } else {
                    for (i in 0..minLength) {
                        val thisEqual = i < minLength && partEquals(newText, oldText, i, i)
                        if (equal != thisEqual || i == minLength) {
                            if (i - start > 0) {
                                if (equal) {
                                    onEqualPart.run(newText.subSequence(start, i), start, i)
                                } else {
                                    onNewPart.run(newText.subSequence(start, i), start, i)
                                    onOldPart.run(oldText.subSequence(start, i), start, i)
                                }
                            }
                            equal = thisEqual
                            start = i
                        }
                    }
                    if (newText.length - minLength > 0) {
                        onNewPart.run(newText.subSequence(minLength, newText.length), minLength, newText.length)
                    }
                    if (oldText.length - minLength > 0) {
                        onOldPart.run(oldText.subSequence(minLength, oldText.length), minLength, oldText.length)
                    }
                }
            } else {
                var astart = 0
                var bstart = 0
                var equal = true
                var a = 0
                var b = 0
                val minLength = min(newText.length, oldText.length)
                while (a <= minLength) {
                    val thisEqual = a < minLength && partEquals(newText, oldText, a, b)
                    if (equal != thisEqual || a == minLength) {
                        if (a == minLength) {
                            a = newText.length
                            b = oldText.length
                        }
                        val alen = a - astart
                        val blen = b - bstart
                        if (alen > 0 || blen > 0) {
                            if (alen == blen && equal) {
                                // equal part on [astart, a)
                                onEqualPart.run(newText.subSequence(astart, a), astart, a)
                            } else {
                                if (alen > 0) {
                                    // new part on [astart, a)
                                    onNewPart.run(newText.subSequence(astart, a), astart, a)
                                }
                                if (blen > 0) {
                                    // old part on [bstart, b)
                                    onOldPart.run(oldText.subSequence(bstart, b), bstart, b)
                                }
                            }
                        }
                        equal = thisEqual
                        astart = a
                        bstart = b
                    }
                    if (thisEqual) {
                        b++
                    }
                    ++a
                }
            }
        }

        var textSize: Float
            get() = textPaint.textSize
            set(textSizePx) {
                textPaint.textSize = textSizePx
            }

        fun setTextColor(color: Int) {
            textPaint.color = color
            alpha = Color.alpha(color)
        }

        fun setAnimationProperties(
            moveAmplitude: Float,
            startDelay: Long,
            duration: Long,
            interpolator: TimeInterpolator
        ) {
            this.moveAmplitude = moveAmplitude
            animateDelay = startDelay
            animateDuration = duration
            animateInterpolator = interpolator
        }

        private fun interface RegionCallback {
            fun run(part: CharSequence, start: Int, end: Int)
        }

        override fun setAlpha(alpha: Int) {
            this.alpha = alpha
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            textPaint.colorFilter = colorFilter
        }

        @Deprecated("Deprecated in Java", ReplaceWith("PixelFormat.TRANSPARENT", "android.graphics.PixelFormat"))
        override fun getOpacity(): Int {
            return PixelFormat.TRANSPARENT
        }

        override fun setBounds(bounds: Rect) {
            super.setBounds(bounds)
            this.bounds.set(bounds)
        }

        override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
            super.setBounds(left, top, right, bottom)
            bounds[left, top, right] = bottom
        }

        override fun getDirtyBounds(): Rect {
            return bounds
        }

        companion object {
            fun partEquals(a: CharSequence?, b: CharSequence?, aIndex: Int, bIndex: Int): Boolean {
                if (a is WordSequence && b is WordSequence) {
                    val wordA = a.wordAt(aIndex)
                    val wordB = b.wordAt(bIndex)
                    return wordA == null && wordB == null || wordA != null && wordA == wordB
                }
                return a == null && b == null || a != null && b != null && a[aIndex] == b[bIndex]
            }
        }
    }

    val drawable: AnimatedTextDrawable
    private var lastMaxWidth = 0
    private var text: CharSequence? = null
    private var hint: CharSequence? = null
    private var toSetText: CharSequence? = null
    private var toSetMoveDown = false
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = MeasureSpec.getSize(widthMeasureSpec)
        var height = MeasureSpec.getSize(heightMeasureSpec)
        if (lastMaxWidth != width && layoutParams.width != 0) {
            drawable.setBounds(paddingLeft, paddingTop, width - paddingRight, height - paddingBottom)
            setTextInternal(drawable.getText(), withAnimation = false, moveDown = true)
        }
        lastMaxWidth = width
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST || width == 0) {
            width = paddingLeft + ceil(drawable.width.toDouble()).toInt() + paddingRight
        }
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST || height == 0) {
            height = paddingTop + ceil(drawable.height.toDouble()).toInt() + paddingBottom
        }
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        drawable.setBounds(paddingLeft, paddingTop, measuredWidth - paddingRight, measuredHeight - paddingBottom)
        drawable.draw(canvas)
    }

    fun setText(text: CharSequence?) {
        setText(text, withAnimation = true, moveDown = true)
    }

    fun setText(text: CharSequence?, withAnimation: Boolean, moveDown: Boolean) {
        this.text = text
        if (this.text != null && this.text!!.isNotEmpty()) {
            setTextInternal(text, withAnimation, moveDown)
        } else {
            setHint(hint, withAnimation, moveDown)
        }
    }

    private var first = true

    init {
        drawable = AnimatedTextDrawable(splitByWords, preserveIndex, startFromEnd)
        drawable.callback = this
        drawable.setOnAnimationFinishListener {
            if (toSetText != null) {
                // wrapped toSetText here to do requestLayout()
                this@AnimatedTextView.setText(toSetText, toSetMoveDown, true)
                toSetText = null
                toSetMoveDown = false
            }
        }
    }

    private fun setTextInternal(text: CharSequence?, withAnimation: Boolean, moveDown: Boolean) {
        var animated = withAnimation
        animated = !first && animated
        first = false
        if (animated) {
            if (drawable.allowCancel) {
                if (drawable.animator != null) {
                    drawable.animator!!.cancel()
                    drawable.animator = null
                }
            } else if (drawable.isAnimating) {
                toSetText = text
                toSetMoveDown = moveDown
                return
            }
        }
        val wasWidth = drawable.width.toInt()
        drawable.setBounds(paddingLeft, paddingTop, lastMaxWidth - paddingRight, measuredHeight - paddingBottom)
        drawable.setText(text, animated, moveDown)
        if (wasWidth < drawable.width || !animated && wasWidth.toFloat() != drawable.width) {
            requestLayout()
        }
    }

    fun setHint(hint: CharSequence?, withAnimation: Boolean, moveDown: Boolean) {
        this.hint = TextUtils.stringOrSpannedString(hint)
        if (text == null || text!!.isEmpty()) {
            setHintInternal(hint, withAnimation, moveDown)
        }
    }

    private fun setHintInternal(hint: CharSequence?, withAnimation: Boolean, moveDown: Boolean) {
        setTextInternal(hint, withAnimation, moveDown)
    }

    fun width(): Int {
        return paddingLeft + ceil(drawable.getCurrentWidth().toDouble()).toInt() + paddingRight
    }

    fun setTextColor(color: Int) {
        drawable.setTextColor(color)
        invalidate()
    }

    fun setTextSize(textSizePx: Float) {
        drawable.textSize = textSizePx
    }

    fun setAnimationProperties(
        moveAmplitude: Float,
        startDelay: Long,
        duration: Long,
        interpolator: TimeInterpolator
    ) {
        drawable.setAnimationProperties(moveAmplitude, startDelay, duration, interpolator)
    }

    override fun invalidateDrawable(drawable: Drawable) {
        super.invalidateDrawable(drawable)
        invalidate()
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = "android.widget.TextView"
        info.text = text
    }
}