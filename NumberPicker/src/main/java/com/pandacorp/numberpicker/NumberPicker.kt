package com.pandacorp.numberpicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.accessibility.AccessibilityEvent
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

/**
 * A widget that enables the user to select a number from a predefined range.
 */
class NumberPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs) {
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(VERTICAL, HORIZONTAL)
    annotation class Orientation

    /**
     * The text for showing the current value.
     */
    private val mSelectedText: EditText

    /**
     * The center X position of the selected text.
     */
    private var mSelectedTextCenterX = 0f

    /**
     * The center Y position of the selected text.
     */
    private var mSelectedTextCenterY = 0f

    /**
     * The min height of this widget.
     */
    private var mMinHeight = 0

    /**
     * The max height of this widget.
     */
    private var mMaxHeight = 0

    /**
     * The max width of this widget.
     */
    private var mMinWidth = 0

    /**
     * The max width of this widget.
     */
    private var mMaxWidth = 0

    /**
     * Flag whether to compute the max width.
     */
    private val mComputeMaxWidth: Boolean

    /**
     * The align of the selected text.
     */
    private var mSelectedTextAlign = DEFAULT_TEXT_ALIGN

    /**
     * The color of the selected text.
     */
    private var mSelectedTextColor = DEFAULT_TEXT_COLOR

    /**
     * The size of the selected text.
     */
    private var mSelectedTextSize = DEFAULT_TEXT_SIZE

    /**
     * Flag whether the selected text should strikethroughed.
     */
    private val mSelectedTextStrikeThru: Boolean

    /**
     * Flag whether the selected text should underlined.
     */
    private val mSelectedTextUnderline: Boolean

    /**
     * The typeface of the selected text.
     */
    private var mSelectedTypeface: Typeface?

    /**
     * The align of the text.
     */
    private var mTextAlign = DEFAULT_TEXT_ALIGN

    /**
     * The color of the text.
     */
    private var mTextColor = DEFAULT_TEXT_COLOR

    /**
     * The size of the text.
     */
    private var mTextSize = DEFAULT_TEXT_SIZE

    /**
     * Flag whether the text should strikethroughed.
     */
    private val mTextStrikeThru: Boolean

    /**
     * Flag whether the text should underlined.
     */
    private val mTextUnderline: Boolean

    /**
     * The typeface of the text.
     */
    private var mTypeface: Typeface?

    /**
     * The height of the gap between text elements if the selector wheel.
     */
    private var mSelectorTextGapHeight = 0

    /**
     * The values to be displayed instead the indices.
     */
    private var mDisplayedValues: Array<String>? = null

    /**
     * Lower value of the range of numbers allowed for the NumberPicker
     */
    private var mMinValue = DEFAULT_MIN_VALUE

    /**
     * Upper value of the range of numbers allowed for the NumberPicker
     */
    private var mMaxValue = DEFAULT_MAX_VALUE

    /**
     * Current value of this NumberPicker
     */
    private var mValue: Int = 0

    /**
     * Listener to be notified upon current value click.
     */
    private var mOnClickListener: OnClickListener? = null

    /**
     * Listener to be notified upon current value change.
     */
    private var mOnValueChangeListener: OnValueChangeListener? = null

    /**
     * Formatter for for displaying the current value.
     */
    private var mFormatter: Formatter?

    /**
     * Cache for the string representation of selector indices.
     */
    private val mSelectorIndexToStringCache = SparseArray<String?>()

    /**
     * The number of items show in the selector wheel.
     */
    private var mWheelItemCount = DEFAULT_WHEEL_ITEM_COUNT

    /**
     * The real number of items show in the selector wheel.
     */
    private var mRealWheelItemCount = DEFAULT_WHEEL_ITEM_COUNT

    /**
     * The index of the middle selector item.
     */
    private var mWheelMiddleItemIndex = mWheelItemCount / 2

    /**
     * The selector indices whose value are show by the selector.
     */
    private var selectorIndices = IntArray(mWheelItemCount)

    /**
     * The [Paint] for drawing the selector.
     */
    private val mSelectorWheelPaint: Paint

    /**
     * The size of a selector element (text + gap).
     */
    private var mSelectorElementSize = 0

    /**
     * The initial offset of the scroll selector.
     */
    private var mInitialScrollOffset = Int.MIN_VALUE

    /**
     * The current offset of the scroll selector.
     */
    private var mCurrentScrollOffset = 0

    /**
     * The [Scroller] responsible for flinging the selector.
     */
    private val mFlingScroller: Scroller

    /**
     * The [Scroller] responsible for adjusting the selector.
     */
    private val mAdjustScroller: Scroller

    /**
     * The previous X coordinate while scrolling the selector.
     */
    private var mPreviousScrollerX = 0

    /**
     * The previous Y coordinate while scrolling the selector.
     */
    private var mPreviousScrollerY = 0

    /**
     * Handle to the reusable command for changing the current value from long press by one.
     */
    private var mChangeCurrentByOneFromLongPressCommand: ChangeCurrentByOneFromLongPressCommand? = null

    /**
     * The X position of the last down event.
     */
    private var mLastDownEventX = 0f

    /**
     * The Y position of the last down event.
     */
    private var mLastDownEventY = 0f

    /**
     * The X position of the last down or move event.
     */
    private var mLastDownOrMoveEventX = 0f

    /**
     * The Y position of the last down or move event.
     */
    private var mLastDownOrMoveEventY = 0f

    /**
     * Determines speed during touch scrolling.
     */
    private var mVelocityTracker: VelocityTracker? = null

    /**
     * @see ViewConfiguration.getScaledTouchSlop
     */
    private val mTouchSlop: Int

    /**
     * @see ViewConfiguration.getScaledMinimumFlingVelocity
     */
    private val mMinimumFlingVelocity: Int

    /**
     * @see ViewConfiguration.getScaledMaximumFlingVelocity
     */
    private val mMaximumFlingVelocity: Int

    /**
     * Flag whether the selector should wrap around.
     */
    private var mWrapSelectorWheel: Boolean = false

    /**
     * User choice on whether the selector wheel should be wrapped.
     */
    private var mWrapSelectorWheelPreferred = true

    /**
     * Divider for showing item to be selected while scrolling
     */
    private var mDividerDrawable: Drawable? = null

    /**
     * The color of the divider.
     */
    private var mDividerColor = DEFAULT_DIVIDER_COLOR

    /**
     * The distance between the two dividers.
     */
    private val mDividerDistance: Int

    /**
     * The thickness of the divider.
     */
    private val mDividerLength: Int

    /**
     * The thickness of the divider.
     */
    private val mDividerThickness: Int

    /**
     * The top of the top divider.
     */
    private var mTopDividerTop = 0

    /**
     * The bottom of the bottom divider.
     */
    private var mBottomDividerBottom = 0

    /**
     * The left of the top divider.
     */
    private var mLeftDividerLeft = 0

    /**
     * The right of the right divider.
     */
    private var mRightDividerRight = 0

    /**
     * The type of the divider.
     */
    private val mDividerType: Int

    /**
     * The current scroll state of the number picker.
     */
    private var mScrollState = OnScrollListener.SCROLL_STATE_IDLE

    /**
     * The keycode of the last handled DPAD down event.
     */
    private var mLastHandledDownDpadKeyCode = -1

    /**
     * Flag whether the selector wheel should hidden until the picker has focus.
     */
    private val mHideWheelUntilFocused: Boolean

    /**
     * The orientation of this widget.
     */
    private var mOrientation: Int

    /**
     * The order of this widget.
     */
    private val order: Int

    /**
     * Flag whether the fading edge should enabled.
     */
    private var mFadingEdgeEnabled = true

    /**
     * The strength of fading edge while drawing the selector.
     */
    private var mFadingEdgeStrength = DEFAULT_FADING_EDGE_STRENGTH

    /**
     * Flag whether the scroller should enabled.
     */
    private var isScrollerEnabled = true

    var textColor: Int
        get() = mTextColor
        set(color) {
            mTextColor = color
            mSelectorWheelPaint.color = mTextColor
        }
    var textSize: Float
        get() = spToPx(mTextSize)
        set(textSize) {
            mTextSize = textSize
            mSelectorWheelPaint.textSize = mTextSize
        }

    var value: Int
        /**
         * Returns the value of the picker.
         *
         * @return The value.
         */
        get() = mValue
        set(value) {
            setValueInternal(value, false)
        }
    var minValue: Int
        /**
         * Returns the min value of the picker.
         *
         * @return The min value
         */
        get() = mMinValue

        /**
         * Sets the min value of the picker.
         *
         * @param minValue The min value inclusive.
         * **Note:** The length of the displayed values array
         * set via [.setDisplayedValues] must be equal to the
         * range of selectable numbers which is equal to
         * [.getMaxValue] - [.getMinValue] + 1.
         */
        set(minValue) {
            mMinValue = minValue
            if (mMinValue > mValue) {
                mValue = mMinValue
            }
            updateWrapSelectorWheel()
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }
    var maxValue: Int
        /**
         * Returns the max value of the picker.
         *
         * @return The max value.
         */
        get() = mMaxValue

        /**
         * Sets the max value of the picker.
         *
         * @param maxValue The max value inclusive.
         * **Note:** The length of the displayed values array
         * set via [.setDisplayedValues] must be equal to the
         * range of selectable numbers which is equal to
         * [.getMaxValue] - [.getMinValue] + 1.
         */
        set(maxValue) {
            require(maxValue >= 0) { "maxValue must be >= 0" }
            mMaxValue = maxValue
            if (mMaxValue < mValue) {
                mValue = mMaxValue
            }
            updateWrapSelectorWheel()
            initializeSelectorWheelIndices()
            updateInputTextView()
            tryComputeMaxWidth()
            invalidate()
        }

    /**
     * The line spacing multiplier of the text.
     */
    private var mLineSpacingMultiplier = DEFAULT_LINE_SPACING_MULTIPLIER

    /**
     * Flag whether the accessibility description enabled.
     */
    private val mAccessibilityDescriptionEnabled: Boolean

    /**
     * The number formatter for current locale.
     */
    private var mNumberFormatter: NumberFormat

    /**
     * Interface to listen for changes of the current value.
     */
    fun interface OnValueChangeListener {
        /**
         * Called upon a change of the current value.
         *
         * @param picker The NumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        fun onValueChange(
            picker: NumberPicker?,
            oldVal: Int,
            newVal: Int
        )
    }

    /**
     * The amount of space between items.
     */
    private val mItemSpacing: Int

    /**
     * Interface to listen for the picker scroll state.
     */
    interface OnScrollListener {
        companion object {
            /**
             * The view is not scrolling.
             */
            const val SCROLL_STATE_IDLE = 0

            /**
             * The user is scrolling using touch, and his finger is still on the screen.
             */
            const val SCROLL_STATE_TOUCH_SCROLL = 1

            /**
             * The user had previously been scrolling using touch and performed a fling.
             */
            const val SCROLL_STATE_FLING = 2
        }
    }

    /**
     * Interface used to format current value into a string for presentation.
     */
    interface Formatter {
        /**
         * Formats a string representation of the current value.
         *
         * @param value The currently selected value.
         * @return A formatted string representation.
         */
        fun format(value: Int): String
    }

    init {
        mNumberFormatter = NumberFormat.getInstance()
        val attributes =
            context.obtainStyledAttributes(
                attrs,
                R.styleable.NumberPicker,
                defStyle,
                0,
            )
        val selectionDivider =
            attributes.getDrawable(
                R.styleable.NumberPicker_np_divider,
            )
        if (selectionDivider != null) {
            selectionDivider.callback = this
            if (selectionDivider.isStateful) {
                selectionDivider.state = drawableState
            }
            mDividerDrawable = selectionDivider
        } else {
            mDividerColor =
                attributes.getColor(
                    R.styleable.NumberPicker_np_dividerColor,
                    mDividerColor,
                )
            setDividerColor(mDividerColor)
        }
        val displayMetrics = resources.displayMetrics
        val defDividerDistance =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_DEFAULT_DIVIDER_DISTANCE.toFloat(),
                displayMetrics,
            ).toInt()
        val defDividerThickness =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_DEFAULT_DIVIDER_THICKNESS.toFloat(),
                displayMetrics,
            ).toInt()
        mDividerDistance =
            attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_dividerDistance, defDividerDistance,
            )
        mDividerLength =
            attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_dividerLength, 0,
            )
        mDividerThickness =
            attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_dividerThickness, defDividerThickness,
            )
        mDividerType = attributes.getInt(R.styleable.NumberPicker_np_dividerType, SIDE_LINES)
        order = attributes.getInt(R.styleable.NumberPicker_np_order, ASCENDING)
        mOrientation = attributes.getInt(R.styleable.NumberPicker_np_orientation, VERTICAL)
        val width =
            attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_width,
                SIZE_UNSPECIFIED,
            ).toFloat()
        val height =
            attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_height,
                SIZE_UNSPECIFIED,
            ).toFloat()
        setWidthAndHeight()
        mComputeMaxWidth = true
        mValue = attributes.getInt(R.styleable.NumberPicker_np_value, mValue)
        mMaxValue = attributes.getInt(R.styleable.NumberPicker_np_max, mMaxValue)
        mMinValue = attributes.getInt(R.styleable.NumberPicker_np_min, mMinValue)
        mSelectedTextAlign =
            attributes.getInt(
                R.styleable.NumberPicker_np_selectedTextAlign,
                mSelectedTextAlign,
            )
        mSelectedTextColor =
            attributes.getColor(
                R.styleable.NumberPicker_np_selectedTextColor,
                mSelectedTextColor,
            )
        mSelectedTextSize =
            attributes.getDimension(
                R.styleable.NumberPicker_np_selectedTextSize,
                spToPx(mSelectedTextSize),
            )
        mSelectedTextStrikeThru =
            attributes.getBoolean(
                R.styleable.NumberPicker_np_selectedTextStrikeThru, false,
            )
        mSelectedTextUnderline =
            attributes.getBoolean(
                R.styleable.NumberPicker_np_selectedTextUnderline, false,
            )
        mSelectedTypeface =
            Typeface.create(
                attributes.getString(
                    R.styleable.NumberPicker_np_selectedTypeface,
                ),
                Typeface.NORMAL,
            )
        mTextAlign = attributes.getInt(R.styleable.NumberPicker_np_textAlign, mTextAlign)
        mTextColor = attributes.getColor(R.styleable.NumberPicker_np_textColor, mTextColor)
        mTextSize =
            attributes.getDimension(
                R.styleable.NumberPicker_np_textSize,
                spToPx(mTextSize),
            )
        mTextStrikeThru =
            attributes.getBoolean(
                R.styleable.NumberPicker_np_textStrikeThru, false,
            )
        mTextUnderline =
            attributes.getBoolean(
                R.styleable.NumberPicker_np_textUnderline, false,
            )
        mTypeface =
            Typeface.create(
                attributes.getString(R.styleable.NumberPicker_np_typeface),
                Typeface.NORMAL,
            )
        mFormatter = stringToFormatter(attributes.getString(R.styleable.NumberPicker_np_formatter))
        mFadingEdgeEnabled =
            attributes.getBoolean(
                R.styleable.NumberPicker_np_fadingEdgeEnabled,
                mFadingEdgeEnabled,
            )
        mFadingEdgeStrength =
            attributes.getFloat(
                R.styleable.NumberPicker_np_fadingEdgeStrength,
                mFadingEdgeStrength,
            )
        isScrollerEnabled =
            attributes.getBoolean(
                R.styleable.NumberPicker_np_scrollerEnabled,
                isScrollerEnabled,
            )
        mWheelItemCount =
            attributes.getInt(
                R.styleable.NumberPicker_np_wheelItemCount,
                mWheelItemCount,
            )
        mLineSpacingMultiplier =
            attributes.getFloat(
                R.styleable.NumberPicker_np_lineSpacingMultiplier, mLineSpacingMultiplier,
            )
        val mMaxFlingVelocityCoefficient =
            attributes.getInt(
                R.styleable.NumberPicker_np_maxFlingVelocityCoefficient,
                DEFAULT_MAX_FLING_VELOCITY_COEFFICIENT,
            )
        mHideWheelUntilFocused =
            attributes.getBoolean(
                R.styleable.NumberPicker_np_hideWheelUntilFocused, false,
            )
        mAccessibilityDescriptionEnabled =
            attributes.getBoolean(
                R.styleable.NumberPicker_np_accessibilityDescriptionEnabled, true,
            )
        mItemSpacing =
            attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_itemSpacing, 0,
            )
        // By default LinearLayout that we extend is not drawn. This is
        // its draw() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // draw() method to be called. Therefore, we declare we will draw.
        setWillNotDraw(false)
        val inflater =
            context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE,
            ) as LayoutInflater
        inflater.inflate(R.layout.number_picker_material, this, true)

        // input text
        mSelectedText = findViewById(R.id.np__numberpicker_input)
        mSelectedText.isEnabled = false
        mSelectedText.isFocusable = false
        mSelectedText.imeOptions = EditorInfo.IME_ACTION_NONE

        // create the selector wheel paint
        val paint = Paint()
        paint.isAntiAlias = true
        paint.textAlign = Paint.Align.CENTER
        mSelectorWheelPaint = paint
        setSelectedTextColor(mSelectedTextColor)
        textColor = mTextColor
        textSize = mTextSize
        setSelectedTextSize(mSelectedTextSize)
        setTypeface(mTypeface)
        setSelectedTypeface(mSelectedTypeface)
        setFormatter(mFormatter)
        updateInputTextView()
        value = mValue
        maxValue = mMaxValue
        minValue = mMinValue
        setWheelItemCount(mWheelItemCount)
        mWrapSelectorWheel =
            attributes.getBoolean(
                R.styleable.NumberPicker_np_wrapSelectorWheel,
                mWrapSelectorWheel,
            )
        setWrapSelectorWheel(mWrapSelectorWheel)
        if (width != SIZE_UNSPECIFIED.toFloat() && height != SIZE_UNSPECIFIED.toFloat()) {
            scaleX = width / mMinWidth
            scaleY = height / mMaxHeight
        } else if (width != SIZE_UNSPECIFIED.toFloat()) {
            val scale = width / mMinWidth
            scaleX = scale
            scaleY = scale
        } else if (height != SIZE_UNSPECIFIED.toFloat()) {
            val scale = height / mMaxHeight
            scaleX = scale
            scaleY = scale
        }

        // initialize constants
        val mViewConfiguration = ViewConfiguration.get(context)
        mTouchSlop = mViewConfiguration.scaledTouchSlop
        mMinimumFlingVelocity = mViewConfiguration.scaledMinimumFlingVelocity
        mMaximumFlingVelocity = (
            mViewConfiguration.scaledMaximumFlingVelocity /
                mMaxFlingVelocityCoefficient
        )

        // create the fling and adjust scrollers
        mFlingScroller = Scroller(context, null, true)
        mAdjustScroller = Scroller(context, DecelerateInterpolator(2.5f))

        // If not explicitly specified this view is important for accessibility.
        if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Should be focusable by default, as the text view whose visibility changes is focusable
            if (focusable == FOCUSABLE_AUTO) {
                focusable = FOCUSABLE
                isFocusableInTouchMode = true
            }
        }
        attributes.recycle()
    }

    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        val msrdWdth = measuredWidth
        val msrdHght = measuredHeight

        // Input text centered horizontally.
        val inptTxtMsrdWdth = mSelectedText.measuredWidth
        val inptTxtMsrdHght = mSelectedText.measuredHeight
        val inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2
        val inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2
        val inptTxtRight = inptTxtLeft + inptTxtMsrdWdth
        val inptTxtBottom = inptTxtTop + inptTxtMsrdHght
        mSelectedText.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom)
        mSelectedTextCenterX = mSelectedText.x + mSelectedText.measuredWidth / 2f - 2f
        mSelectedTextCenterY = mSelectedText.y + mSelectedText.measuredHeight / 2f - 5f
        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel()
            initializeFadingEdges()
            val dividerDistance = 2 * mDividerThickness + mDividerDistance
            if (isHorizontalMode) {
                mLeftDividerLeft = (width - mDividerDistance) / 2 - mDividerThickness
                mRightDividerRight = mLeftDividerLeft + dividerDistance
                mBottomDividerBottom = height
            } else {
                mTopDividerTop = (height - mDividerDistance) / 2 - mDividerThickness
                mBottomDividerBottom = mTopDividerTop + dividerDistance
            }
        }
    }

    override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) {
        // Try greedily to fit the max width and height.
        val newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth)
        val newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight)
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
        // Flag if we are measured with width or height less than the respective min.
        val widthSize =
            resolveSizeAndStateRespectingMinSize(
                mMinWidth,
                measuredWidth,
                widthMeasureSpec,
            )
        val heightSize =
            resolveSizeAndStateRespectingMinSize(
                mMinHeight,
                measuredHeight,
                heightMeasureSpec,
            )
        setMeasuredDimension(widthSize, heightSize)
    }

    /**
     * Move to the final position of a scroller. Ensures to force finish the scroller
     * and if it is not at its final position a scroll of the selector wheel is
     * performed to fast forward to the final position.
     *
     * @param scroller The scroller to whose final position to get.
     * @return True of the a move was performed, i.e. the scroller was not in final position.
     */
    private fun moveToFinalScrollerPosition(scroller: Scroller): Boolean {
        scroller.forceFinished(true)
        if (isHorizontalMode) {
            var amountToScroll = scroller.finalX - scroller.currX
            val futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize
            var overshootAdjustment = mInitialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize
                    } else {
                        overshootAdjustment += mSelectorElementSize
                    }
                }
                amountToScroll += overshootAdjustment
                scrollBy(amountToScroll, 0)
                return true
            }
        } else {
            var amountToScroll = scroller.finalY - scroller.currY
            val futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize
            var overshootAdjustment = mInitialScrollOffset - futureScrollOffset
            if (overshootAdjustment != 0) {
                if (abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize
                    } else {
                        overshootAdjustment += mSelectorElementSize
                    }
                }
                amountToScroll += overshootAdjustment
                scrollBy(0, amountToScroll)
                return true
            }
        }
        return false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        val action = event.action and MotionEvent.ACTION_MASK
        if (action != MotionEvent.ACTION_DOWN) {
            return false
        }
        removeAllCallbacks()
        // Make sure we support flinging inside scrollables.
        parent.requestDisallowInterceptTouchEvent(true)
        if (isHorizontalMode) {
            mLastDownEventX = event.x
            mLastDownOrMoveEventX = mLastDownEventX
            if (!mFlingScroller.isFinished) {
                mFlingScroller.forceFinished(true)
                mAdjustScroller.forceFinished(true)
                onScrollerFinished(mFlingScroller)
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
            } else if (!mAdjustScroller.isFinished) {
                mFlingScroller.forceFinished(true)
                mAdjustScroller.forceFinished(true)
                onScrollerFinished(mAdjustScroller)
            } else if (mLastDownEventX >= mLeftDividerLeft &&
                mLastDownEventX <= mRightDividerRight
            ) {
                if (mOnClickListener != null) {
                    mOnClickListener!!.onClick(this)
                }
            } else if (mLastDownEventX < mLeftDividerLeft) {
                postChangeCurrentByOneFromLongPress(false)
            } else if (mLastDownEventX > mRightDividerRight) {
                postChangeCurrentByOneFromLongPress(true)
            }
        } else {
            mLastDownEventY = event.y
            mLastDownOrMoveEventY = mLastDownEventY
            if (!mFlingScroller.isFinished) {
                mFlingScroller.forceFinished(true)
                mAdjustScroller.forceFinished(true)
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
            } else if (!mAdjustScroller.isFinished) {
                mFlingScroller.forceFinished(true)
                mAdjustScroller.forceFinished(true)
            } else if (mLastDownEventY >= mTopDividerTop &&
                mLastDownEventY <= mBottomDividerBottom
            ) {
                if (mOnClickListener != null) {
                    mOnClickListener!!.onClick(this)
                }
            } else if (mLastDownEventY < mTopDividerTop) {
                postChangeCurrentByOneFromLongPress(false)
            } else if (mLastDownEventY > mBottomDividerBottom) {
                postChangeCurrentByOneFromLongPress(true)
            }
        }
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) {
            return false
        }
        if (!isScrollerEnabled) {
            return false
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
        mVelocityTracker!!.addMovement(event)
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_MOVE -> {
                if (isHorizontalMode) {
                    val currentMoveX = event.x
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownX = abs(currentMoveX - mLastDownEventX).toInt()
                        if (deltaDownX > mTouchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveX = (currentMoveX - mLastDownOrMoveEventX).toInt()
                        scrollBy(deltaMoveX, 0)
                        invalidate()
                    }
                    mLastDownOrMoveEventX = currentMoveX
                } else {
                    val currentMoveY = event.y
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        val deltaDownY = abs(currentMoveY - mLastDownEventY).toInt()
                        if (deltaDownY > mTouchSlop) {
                            removeAllCallbacks()
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL)
                        }
                    } else {
                        val deltaMoveY = (currentMoveY - mLastDownOrMoveEventY).toInt()
                        scrollBy(0, deltaMoveY)
                        invalidate()
                    }
                    mLastDownOrMoveEventY = currentMoveY
                }
            }

            MotionEvent.ACTION_UP -> {
                removeChangeCurrentByOneFromLongPress()
                val velocityTracker = mVelocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, mMaximumFlingVelocity.toFloat())
                if (isHorizontalMode) {
                    val initialVelocity = velocityTracker.xVelocity.toInt()
                    if (abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventX = event.x.toInt()
                        val deltaMoveX = abs(eventX - mLastDownEventX).toInt()
                        if (deltaMoveX <= mTouchSlop) {
                            val selectorIndexOffset = (
                                eventX / mSelectorElementSize -
                                    mWheelMiddleItemIndex
                            )
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true)
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false)
                            } else {
                                ensureScrollWheelAdjusted()
                            }
                        } else {
                            ensureScrollWheelAdjusted()
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    }
                } else {
                    val initialVelocity = velocityTracker.yVelocity.toInt()
                    if (abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity)
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING)
                    } else {
                        val eventY = event.y.toInt()
                        val deltaMoveY = abs(eventY - mLastDownEventY).toInt()
                        if (deltaMoveY <= mTouchSlop) {
                            val selectorIndexOffset = (
                                eventY / mSelectorElementSize -
                                    mWheelMiddleItemIndex
                            )
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true)
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false)
                            } else {
                                ensureScrollWheelAdjusted()
                            }
                        } else {
                            ensureScrollWheelAdjusted()
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
                    }
                }
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
            }
        }
        return true
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        when (val keyCode = event.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> removeAllCallbacks()
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_UP ->
                when (event.action) {
                    KeyEvent.ACTION_DOWN ->
                        if (mWrapSelectorWheel || (if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) value < maxValue else value > minValue)) {
                            requestFocus()
                            mLastHandledDownDpadKeyCode = keyCode
                            removeAllCallbacks()
                            if (mFlingScroller.isFinished) {
                                changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                            }
                            return true
                        }

                    KeyEvent.ACTION_UP ->
                        if (mLastHandledDownDpadKeyCode == keyCode) {
                            mLastHandledDownDpadKeyCode = -1
                            return true
                        }
                }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchTrackballEvent(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> removeAllCallbacks()
        }
        return super.dispatchTrackballEvent(event)
    }

    override fun computeScroll() {
        if (!isScrollerEnabled) {
            return
        }
        var scroller = mFlingScroller
        if (scroller.isFinished) {
            scroller = mAdjustScroller
            if (scroller.isFinished) {
                return
            }
        }
        scroller.computeScrollOffset()
        if (isHorizontalMode) {
            val currentScrollerX = scroller.currX
            if (mPreviousScrollerX == 0) {
                mPreviousScrollerX = scroller.startX
            }
            scrollBy(currentScrollerX - mPreviousScrollerX, 0)
            mPreviousScrollerX = currentScrollerX
        } else {
            val currentScrollerY = scroller.currY
            if (mPreviousScrollerY == 0) {
                mPreviousScrollerY = scroller.startY
            }
            scrollBy(0, currentScrollerY - mPreviousScrollerY)
            mPreviousScrollerY = currentScrollerY
        }
        if (scroller.isFinished) {
            onScrollerFinished(scroller)
        } else {
            postInvalidate()
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mSelectedText.isEnabled = enabled
    }

    override fun scrollBy(
        x: Int,
        y: Int
    ) {
        val mSelectorElementHeight = mSelectorElementSize
        val selectorIndices = selectorIndices
        val startScrollOffset = mCurrentScrollOffset
        if (!mWrapSelectorWheel && y > 0 && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
            mCurrentScrollOffset = mInitialScrollOffset
            return
        }
        if (!mWrapSelectorWheel && y < 0 && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
            mCurrentScrollOffset = mInitialScrollOffset
            return
        }
        mCurrentScrollOffset += y
        while (mCurrentScrollOffset - mInitialScrollOffset > mSelectorTextGapHeight) {
            mCurrentScrollOffset -= mSelectorElementHeight
            decrementSelectorIndices(selectorIndices)
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true)
            if (!mWrapSelectorWheel && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -mSelectorTextGapHeight) {
            mCurrentScrollOffset += mSelectorElementHeight
            incrementSelectorIndices(selectorIndices)
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true)
            if (!mWrapSelectorWheel && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset
            }
        }
        if (startScrollOffset != mCurrentScrollOffset) {
            onScrollChanged(0, mCurrentScrollOffset, 0, startScrollOffset)
        }
    }

    private fun computeScrollOffset(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) mCurrentScrollOffset else 0
    }

    private fun computeScrollRange(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) (mMaxValue - mMinValue + 1) * mSelectorElementSize else 0
    }

    private fun computeScrollExtent(isHorizontalMode: Boolean): Int {
        return if (isHorizontalMode) width else height
    }

    override fun computeHorizontalScrollOffset(): Int {
        return computeScrollOffset(isHorizontalMode)
    }

    override fun computeHorizontalScrollRange(): Int {
        return computeScrollRange(isHorizontalMode)
    }

    override fun computeHorizontalScrollExtent(): Int {
        return computeScrollExtent(isHorizontalMode)
    }

    override fun computeVerticalScrollOffset(): Int {
        return computeScrollOffset(!isHorizontalMode)
    }

    override fun computeVerticalScrollRange(): Int {
        return computeScrollRange(!isHorizontalMode)
    }

    override fun computeVerticalScrollExtent(): Int {
        return computeScrollExtent(isHorizontalMode)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mNumberFormatter = NumberFormat.getInstance()
    }

    /**
     * Set listener to be notified on click of the current value.
     *
     * @param onClickListener The listener.
     */
    override fun setOnClickListener(onClickListener: OnClickListener?) {
        mOnClickListener = onClickListener
    }

    /**
     * Sets the listener to be notified on change of the current value.
     *
     * @param onValueChangedListener The listener.
     */
    fun setOnValueChangedListener(onValueChangedListener: OnValueChangeListener?) {
        mOnValueChangeListener = onValueChangedListener
    }

    /**
     * Set the formatter to be used for formatting the current value.
     *
     *
     * Note: If you have provided alternative values for the values this
     * formatter is never invoked.
     *
     *
     * @param formatter The formatter object.
     * @see .setDisplayedValues
     */
    private fun setFormatter(formatter: Formatter?) {
        if (formatter === mFormatter) {
            return
        }
        mFormatter = formatter
        initializeSelectorWheelIndices()
        updateInputTextView()
    }

    private val maxTextSize: Float
        get() = mTextSize.coerceAtLeast(mSelectedTextSize)

    private fun getPaintCenterY(fontMetrics: Paint.FontMetrics?): Float {
        return if (fontMetrics == null) {
            0f
        } else {
            abs(fontMetrics.top + fontMetrics.bottom) / 2
        }
    }

    /**
     * Computes the max width if no such specified as an attribute.
     */
    private fun tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return
        }
        mSelectorWheelPaint.textSize = maxTextSize
        var maxTextWidth = 0
        if (mDisplayedValues == null) {
            var maxDigitWidth = 0f
            for (i in 0..9) {
                val digitWidth = mSelectorWheelPaint.measureText(formatNumber(i))
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth
                }
            }
            var numberOfDigits = 0
            var current = mMaxValue
            while (current > 0) {
                numberOfDigits++
                current /= 10
            }
            maxTextWidth = (numberOfDigits * maxDigitWidth).toInt()
        } else {
            for (displayedValue in mDisplayedValues!!) {
                val textWidth = mSelectorWheelPaint.measureText(displayedValue)
                if (textWidth > maxTextWidth) {
                    maxTextWidth = textWidth.toInt()
                }
            }
        }
        maxTextWidth += mSelectedText.paddingLeft + mSelectedText.paddingRight
        if (mMaxWidth != maxTextWidth) {
            mMaxWidth = maxTextWidth.coerceAtLeast(mMinWidth)
            invalidate()
        }
    }

    /**
     * Sets whether the selector wheel shown during flinging/scrolling should
     * wrap values.
     *
     *
     * By default if the range (max - min) is more than the number of items shown
     * on the selector wheel the selector wheel wrapping is enabled.
     *
     *
     *
     * **Note:** If the number of items, i.e. the range (
     * [.getMaxValue] - [.getMinValue]) is less than
     * the number of items shown on the selector wheel, the selector wheel will
     * not wrap. Hence, in such a case calling this method is a NOP.
     *
     *
     * @param wrapSelectorWheel Whether to wrap.
     */
    private fun setWrapSelectorWheel(wrapSelectorWheel: Boolean) {
        mWrapSelectorWheelPreferred = wrapSelectorWheel
        updateWrapSelectorWheel()
    }

    /**
     * Whether or not the selector wheel should be wrapped is determined by user choice and whether
     * the choice is allowed. The former comes from [.setWrapSelectorWheel], the
     * latter is calculated based on min & max value set vs selector's visual length. Therefore,
     * this method should be called any time any of the 3 values (i.e. user choice, min and max
     * value) gets updated.
     */
    private fun updateWrapSelectorWheel() {
        mWrapSelectorWheel = isWrappingAllowed && mWrapSelectorWheelPreferred
    }

    private val isWrappingAllowed: Boolean
        get() = mMaxValue - mMinValue >= selectorIndices.size - 1

    /**
     * Sets the values to be displayed.
     *
     * @param displayedValues The displayed values.
     * **Note:** The length of the displayed values array
     * must be equal to the range of selectable numbers which is equal to
     * [.getMaxValue] - [.getMinValue] + 1.
     */
    fun setDisplayedValues(displayedValues: Array<String>?) {
        if (mDisplayedValues.contentEquals(displayedValues)) {
            return
        }
        mDisplayedValues = displayedValues
        if (mDisplayedValues != null) {
            // Allow text entry rather than strictly numeric entry.
            mSelectedText.setRawInputType(
                InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            )
        } else {
            mSelectedText.setRawInputType(InputType.TYPE_CLASS_NUMBER)
        }
        updateInputTextView()
        initializeSelectorWheelIndices()
        tryComputeMaxWidth()
    }

    private fun getFadingEdgeStrength(isHorizontalMode: Boolean): Float {
        return if (isHorizontalMode && mFadingEdgeEnabled) mFadingEdgeStrength else 0f
    }

    override fun getTopFadingEdgeStrength(): Float {
        return getFadingEdgeStrength(!isHorizontalMode)
    }

    override fun getBottomFadingEdgeStrength(): Float {
        return getFadingEdgeStrength(!isHorizontalMode)
    }

    override fun getLeftFadingEdgeStrength(): Float {
        return getFadingEdgeStrength(isHorizontalMode)
    }

    override fun getRightFadingEdgeStrength(): Float {
        return getFadingEdgeStrength(isHorizontalMode)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeAllCallbacks()
    }

    @CallSuper
    override fun drawableStateChanged() {
        super.drawableStateChanged()
        if (mDividerDrawable != null && mDividerDrawable!!.isStateful &&
            mDividerDrawable!!.setState(drawableState)
        ) {
            invalidateDrawable(mDividerDrawable!!)
        }
    }

    @CallSuper
    override fun jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState()
        if (mDividerDrawable != null) {
            mDividerDrawable!!.jumpToCurrentState()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // save canvas
        canvas.save()
        val showSelectorWheel = !mHideWheelUntilFocused || hasFocus()
        var x: Float
        var y: Float
        if (isHorizontalMode) {
            x = mCurrentScrollOffset.toFloat()
            y = (mSelectedText.baseline + mSelectedText.top).toFloat()
            if (mRealWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(mLeftDividerLeft, 0, mRightDividerRight, bottom)
            }
        } else {
            x = (right - left) / 2f
            y = mCurrentScrollOffset.toFloat()
            if (mRealWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(0, mTopDividerTop, right, mBottomDividerBottom)
            }
        }

        // draw the selector wheel
        val selectorIndices = selectorIndices
        for (i in selectorIndices.indices) {
            if (i == mWheelMiddleItemIndex) {
                mSelectorWheelPaint.textAlign = Paint.Align.values()[mSelectedTextAlign]
                mSelectorWheelPaint.textSize = mSelectedTextSize
                mSelectorWheelPaint.color = mSelectedTextColor
                mSelectorWheelPaint.isStrikeThruText = mSelectedTextStrikeThru
                mSelectorWheelPaint.isUnderlineText = mSelectedTextUnderline
                mSelectorWheelPaint.typeface = mSelectedTypeface
            } else {
                mSelectorWheelPaint.textAlign = Paint.Align.values()[mTextAlign]
                mSelectorWheelPaint.textSize = mTextSize
                mSelectorWheelPaint.color = mTextColor
                mSelectorWheelPaint.isStrikeThruText = mTextStrikeThru
                mSelectorWheelPaint.isUnderlineText = mTextUnderline
                mSelectorWheelPaint.typeface = mTypeface
            }
            val selectorIndex = selectorIndices[if (isAscendingOrder) i else selectorIndices.size - i - 1]
            val scrollSelectorValue = mSelectorIndexToStringCache[selectorIndex] ?: continue
            // Do not draw the middle item if input is visible since the input
            // is shown only if the wheel is static and it covers the middle
            // item. Otherwise, if the user starts editing the text via the
            // IME he may see a dimmed version of the old value intermixed
            // with the new one.
            if (showSelectorWheel && i != mWheelMiddleItemIndex || i == mWheelMiddleItemIndex && mSelectedText.visibility != VISIBLE) {
                var textY = y
                if (!isHorizontalMode) {
                    textY += getPaintCenterY(mSelectorWheelPaint.fontMetrics)
                }
                var xOffset = 0
                var yOffset = 0
                if (i != mWheelMiddleItemIndex && mItemSpacing != 0) {
                    if (isHorizontalMode) {
                        xOffset =
                            if (i > mWheelMiddleItemIndex) {
                                mItemSpacing
                            } else {
                                -mItemSpacing
                            }
                    } else {
                        yOffset =
                            if (i > mWheelMiddleItemIndex) {
                                mItemSpacing
                            } else {
                                -mItemSpacing
                            }
                    }
                }
                drawText(scrollSelectorValue, x + xOffset, textY + yOffset, mSelectorWheelPaint, canvas)
            }
            if (isHorizontalMode) {
                x += mSelectorElementSize.toFloat()
            } else {
                y += mSelectorElementSize.toFloat()
            }
        }

        // restore canvas
        canvas.restore()

        // draw the dividers
        if (showSelectorWheel && mDividerDrawable != null) {
            if (isHorizontalMode) drawHorizontalDividers(canvas) else drawVerticalDividers(canvas)
        }
    }

    private fun drawHorizontalDividers(canvas: Canvas) {
        when (mDividerType) {
            SIDE_LINES -> {
                val top: Int
                val bottom: Int
                if (mDividerLength in 1..mMaxHeight) {
                    top = (mMaxHeight - mDividerLength) / 2
                    bottom = top + mDividerLength
                } else {
                    top = 0
                    bottom = getBottom()
                }
                // draw the left divider
                val leftOfLeftDivider = mLeftDividerLeft
                val rightOfLeftDivider = leftOfLeftDivider + mDividerThickness
                mDividerDrawable!!.setBounds(leftOfLeftDivider, top, rightOfLeftDivider, bottom)
                mDividerDrawable!!.draw(canvas)
                // draw the right divider
                val rightOfRightDivider = mRightDividerRight
                val leftOfRightDivider = rightOfRightDivider - mDividerThickness
                mDividerDrawable!!.setBounds(leftOfRightDivider, top, rightOfRightDivider, bottom)
                mDividerDrawable!!.draw(canvas)
            }

            UNDERLINE -> {
                val left: Int
                val right: Int
                if (mDividerLength in 1..mMaxWidth) {
                    left = (mMaxWidth - mDividerLength) / 2
                    right = left + mDividerLength
                } else {
                    left = mLeftDividerLeft
                    right = mRightDividerRight
                }
                val bottomOfUnderlineDivider = mBottomDividerBottom
                val topOfUnderlineDivider = bottomOfUnderlineDivider - mDividerThickness
                mDividerDrawable!!.setBounds(
                    left,
                    topOfUnderlineDivider,
                    right,
                    bottomOfUnderlineDivider,
                )
                mDividerDrawable!!.draw(canvas)
            }
        }
    }

    private fun drawVerticalDividers(canvas: Canvas) {
        val left: Int
        val right: Int
        if (mDividerLength in 1..mMaxWidth) {
            left = (mMaxWidth - mDividerLength) / 2
            right = left + mDividerLength
        } else {
            left = 0
            right = getRight()
        }
        when (mDividerType) {
            SIDE_LINES -> {
                // draw the top divider
                val topOfTopDivider = mTopDividerTop
                val bottomOfTopDivider = topOfTopDivider + mDividerThickness
                mDividerDrawable!!.setBounds(left, topOfTopDivider, right, bottomOfTopDivider)
                mDividerDrawable!!.draw(canvas)
                // draw the bottom divider
                val bottomOfBottomDivider = mBottomDividerBottom
                val topOfBottomDivider = bottomOfBottomDivider - mDividerThickness
                mDividerDrawable!!.setBounds(
                    left,
                    topOfBottomDivider,
                    right,
                    bottomOfBottomDivider,
                )
                mDividerDrawable!!.draw(canvas)
            }

            UNDERLINE -> {
                val bottomOfUnderlineDivider = mBottomDividerBottom
                val topOfUnderlineDivider = bottomOfUnderlineDivider - mDividerThickness
                mDividerDrawable!!.setBounds(
                    left,
                    topOfUnderlineDivider,
                    right,
                    bottomOfUnderlineDivider,
                )
                mDividerDrawable!!.draw(canvas)
            }
        }
    }

    private fun drawText(
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        canvas: Canvas
    ) {
        var mY = y
        if (text.contains("\n")) {
            val lines = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val height = (
                abs(paint.descent() + paint.ascent()) *
                    mLineSpacingMultiplier
            )
            val diff = (lines.size - 1) * height / 2
            mY -= diff
            for (line in lines) {
                canvas.drawText(line, x, mY, paint)
                mY += height
            }
        } else {
            canvas.drawText(text, x, mY, paint)
        }
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = NumberPicker::class.java.name
        event.isScrollable = isScrollerEnabled
        val scroll = (mMinValue + mValue) * mSelectorElementSize
        val maxScroll = (mMaxValue - mMinValue) * mSelectorElementSize
        if (isHorizontalMode) {
            event.scrollX = scroll
            event.maxScrollX = maxScroll
        } else {
            event.scrollY = scroll
            event.maxScrollY = maxScroll
        }
    }

    /**
     * Makes a measure spec that tries greedily to use the max value.
     *
     * @param measureSpec The measure spec.
     * @param maxSize     The max value for the size.
     * @return A measure spec greedily imposing the max size.
     */
    private fun makeMeasureSpec(
        measureSpec: Int,
        maxSize: Int
    ): Int {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec
        }
        val size = MeasureSpec.getSize(measureSpec)
        return when (val mode = MeasureSpec.getMode(measureSpec)) {
            MeasureSpec.EXACTLY -> measureSpec
            MeasureSpec.AT_MOST ->
                MeasureSpec.makeMeasureSpec(
                    size.coerceAtMost(maxSize),
                    MeasureSpec.EXACTLY,
                )

            MeasureSpec.UNSPECIFIED ->
                MeasureSpec.makeMeasureSpec(
                    maxSize,
                    MeasureSpec.EXACTLY,
                )

            else -> throw IllegalArgumentException("Unknown measure mode: $mode")
        }
    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec. Tries to respect the min size, unless a different size
     * is imposed by the constraints.
     *
     * @param minSize      The minimal desired size.
     * @param measuredSize The currently measured size.
     * @param measureSpec  The current measure spec.
     * @return The resolved size and state.
     */
    private fun resolveSizeAndStateRespectingMinSize(
        minSize: Int,
        measuredSize: Int,
        measureSpec: Int
    ): Int {
        return if (minSize != SIZE_UNSPECIFIED) {
            val desiredWidth = minSize.coerceAtLeast(measuredSize)
            resolveSizeAndState(desiredWidth, measureSpec, 0)
        } else {
            measuredSize
        }
    }

    /**
     * Resets the selector indices and clear the cached string representation of
     * these indices.
     */
    private fun initializeSelectorWheelIndices() {
        mSelectorIndexToStringCache.clear()
        val selectorIndices = selectorIndices
        val current = value
        for (i in selectorIndices.indices) {
            var selectorIndex = current + (i - mWheelMiddleItemIndex)
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex)
            }
            selectorIndices[i] = selectorIndex
            ensureCachedScrollSelectorValue(selectorIndices[i])
        }
    }

    /**
     * Sets the current value of this NumberPicker.
     *
     * @param current      The new value of the NumberPicker.
     * @param notifyChange Whether to notify if the current value changed.
     */
    private fun setValueInternal(
        current: Int,
        notifyChange: Boolean
    ) {
        var mCurrent = current
        if (mValue == mCurrent) {
            return
        }
        // Wrap around the values if we go past the start or end
        if (mWrapSelectorWheel) {
            mCurrent = getWrappedSelectorIndex(mCurrent)
        } else {
            mCurrent = mCurrent.coerceAtLeast(mMinValue)
            mCurrent = mCurrent.coerceAtMost(mMaxValue)
        }
        val previous = mValue
        mValue = mCurrent
        // If we're flinging, we'll update the text view at the end when it becomes visible
        if (mScrollState != OnScrollListener.SCROLL_STATE_FLING) {
            updateInputTextView()
        }
        if (notifyChange) {
            notifyChange(previous, mCurrent)
        }
        initializeSelectorWheelIndices()
        updateAccessibilityDescription()
        invalidate()
    }

    /**
     * Updates the accessibility values of the view,
     * to the currently selected value
     */
    private fun updateAccessibilityDescription() {
        if (!mAccessibilityDescriptionEnabled) {
            return
        }
        this.contentDescription = value.toString()
    }

    /**
     * Changes the current value by one which is increment or
     * decrement based on the passes argument.
     * decrement the current value.
     *
     * @param increment True to increment, false to decrement.
     */
    private fun changeValueByOne(increment: Boolean) {
        if (!moveToFinalScrollerPosition(mFlingScroller)) {
            moveToFinalScrollerPosition(mAdjustScroller)
        }
        smoothScroll(increment)
    }

    /**
     * Starts a smooth scroll
     *
     * @param increment True to increment, false to decrement.
     */
    private fun smoothScroll(increment: Boolean) {
        val diffSteps = (if (increment) -mSelectorElementSize else mSelectorElementSize) * 1
        if (isHorizontalMode) {
            mPreviousScrollerX = 0
            mFlingScroller.startScroll(0, 0, diffSteps, 0, SNAP_SCROLL_DURATION)
        } else {
            mPreviousScrollerY = 0
            mFlingScroller.startScroll(0, 0, 0, diffSteps, SNAP_SCROLL_DURATION)
        }
        invalidate()
    }

    private fun initializeSelectorWheel() {
        initializeSelectorWheelIndices()
        val selectorIndices = selectorIndices
        val totalTextSize = ((selectorIndices.size - 1) * mTextSize + mSelectedTextSize).toInt()
        val textGapCount = selectorIndices.size.toFloat()
        if (isHorizontalMode) {
            val totalTextGapWidth = (right - left - totalTextSize).toFloat()
            val mSelectorTextGapWidth = (totalTextGapWidth / textGapCount).toInt()
            mSelectorElementSize = maxTextSize.toInt() + mSelectorTextGapWidth
            mInitialScrollOffset = (mSelectedTextCenterX - mSelectorElementSize * mWheelMiddleItemIndex).toInt()
        } else {
            val totalTextGapHeight = (bottom - top - totalTextSize).toFloat()
            mSelectorTextGapHeight = (totalTextGapHeight / textGapCount).toInt()
            mSelectorElementSize = maxTextSize.toInt() + mSelectorTextGapHeight
            mInitialScrollOffset = (mSelectedTextCenterY - mSelectorElementSize * mWheelMiddleItemIndex).toInt()
        }
        mCurrentScrollOffset = mInitialScrollOffset
        updateInputTextView()
    }

    private fun initializeFadingEdges() {
        if (isHorizontalMode) {
            isHorizontalFadingEdgeEnabled = true
            isVerticalFadingEdgeEnabled = false
            setFadingEdgeLength((right - left - mTextSize.toInt()) / 2)
        } else {
            isHorizontalFadingEdgeEnabled = false
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength((bottom - top - mTextSize.toInt()) / 2)
        }
    }

    /**
     * Callback invoked upon completion of a given `scroller`.
     */
    private fun onScrollerFinished(scroller: Scroller) {
        if (scroller == mFlingScroller) {
            ensureScrollWheelAdjusted()
            updateInputTextView()
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE)
        } else if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            updateInputTextView()
        }
    }

    /**
     * Handles transition to a given `scrollState`
     */
    private fun onScrollStateChange(scrollState: Int) {
        if (mScrollState == scrollState) {
            return
        }
        mScrollState = scrollState
    }

    /**
     * Flings the selector with the given `velocity`.
     */
    private fun fling(velocity: Int) {
        if (isHorizontalMode) {
            mPreviousScrollerX = 0
            if (velocity > 0) {
                mFlingScroller.fling(0, 0, velocity, 0, 0, Int.MAX_VALUE, 0, 0)
            } else {
                mFlingScroller.fling(Int.MAX_VALUE, 0, velocity, 0, 0, Int.MAX_VALUE, 0, 0)
            }
        } else {
            mPreviousScrollerY = 0
            if (velocity > 0) {
                mFlingScroller.fling(0, 0, 0, velocity, 0, 0, 0, Int.MAX_VALUE)
            } else {
                mFlingScroller.fling(0, Int.MAX_VALUE, 0, velocity, 0, 0, 0, Int.MAX_VALUE)
            }
        }
        invalidate()
    }

    /**
     * @return The wrapped index `selectorIndex` value.
     */
    private fun getWrappedSelectorIndex(selectorIndex: Int): Int {
        if (selectorIndex > mMaxValue) {
            return mMinValue + (selectorIndex - mMaxValue) % (mMaxValue - mMinValue) - 1
        } else if (selectorIndex < mMinValue) {
            return mMaxValue - (mMinValue - selectorIndex) % (mMaxValue - mMinValue) + 1
        }
        return selectorIndex
    }

    /**
     * Increments the `selectorIndices` whose string representations
     * will be displayed in the selector.
     */
    private fun incrementSelectorIndices(selectorIndices: IntArray) {
        for (i in 0 until selectorIndices.size - 1) {
            selectorIndices[i] = selectorIndices[i + 1]
        }
        var nextScrollSelectorIndex = selectorIndices[selectorIndices.size - 2] + 1
        if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
            nextScrollSelectorIndex = mMinValue
        }
        selectorIndices[selectorIndices.size - 1] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    /**
     * Decrements the `selectorIndices` whose string representations
     * will be displayed in the selector.
     */
    private fun decrementSelectorIndices(selectorIndices: IntArray) {
        for (i in selectorIndices.size - 1 downTo 1) {
            selectorIndices[i] = selectorIndices[i - 1]
        }
        var nextScrollSelectorIndex = selectorIndices[1] - 1
        if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
            nextScrollSelectorIndex = mMaxValue
        }
        selectorIndices[0] = nextScrollSelectorIndex
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex)
    }

    /**
     * Ensures we have a cached string representation of the given `
     * selectorIndex` to avoid multiple instantiations of the same string.
     */
    private fun ensureCachedScrollSelectorValue(selectorIndex: Int) {
        val cache = mSelectorIndexToStringCache
        var scrollSelectorValue = cache[selectorIndex]
        if (scrollSelectorValue != null) {
            return
        }
        scrollSelectorValue =
            if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
                ""
            } else {
                if (mDisplayedValues != null) {
                    val displayedValueIndex = selectorIndex - mMinValue
                    if (displayedValueIndex >= mDisplayedValues!!.size) {
                        cache.remove(selectorIndex)
                        return
                    }
                    mDisplayedValues!![displayedValueIndex]
                } else {
                    formatNumber(selectorIndex)
                }
            }
        cache.put(selectorIndex, scrollSelectorValue)
    }

    private fun formatNumber(value: Int): String {
        return if (mFormatter != null) mFormatter!!.format(value) else formatNumberWithLocale(value)
    }

    /**
     * Updates the view of this NumberPicker. If displayValues were specified in
     * the string corresponding to the index specified by the current value will
     * be returned. Otherwise, the formatter specified in [.setFormatter]
     * will be used to format the number.
     */
    private fun updateInputTextView() {
        /*
         * If we don't have displayed values then use the current number else
         * find the correct value in the displayed values for the current
         * number.
         */
        val text = if (mDisplayedValues == null) formatNumber(mValue) else mDisplayedValues!![mValue - mMinValue]
        if (TextUtils.isEmpty(text)) {
            return
        }
        val beforeText: CharSequence = mSelectedText.text
        if (text == beforeText.toString()) {
            return
        }
        mSelectedText.setText(text)
    }

    /**
     * Notifies the listener, if registered, of a change of the value of this
     * NumberPicker.
     */
    private fun notifyChange(
        previous: Int,
        current: Int
    ) {
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener!!.onValueChange(this, previous, current)
        }
    }

    /**
     * Posts a command for changing the current value by one.
     *
     * @param increment Whether to increment or decrement the value.
     */
    private fun postChangeCurrentByOneFromLongPress(
        increment: Boolean,
        delayMillis: Long = ViewConfiguration.getLongPressTimeout().toLong()
    ) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = ChangeCurrentByOneFromLongPressCommand()
        } else {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
        mChangeCurrentByOneFromLongPressCommand!!.setStep(increment)
        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis)
    }

    /**
     * Removes the command for changing the current value by one.
     */
    private fun removeChangeCurrentByOneFromLongPress() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
    }

    /**
     * Removes all pending callback from the message queue.
     */
    private fun removeAllCallbacks() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand)
        }
    }

    /**
     * Ensures that the scroll wheel is adjusted i.e. there is no offset and the
     * middle element is in the middle of the widget.
     */
    private fun ensureScrollWheelAdjusted() {
        // adjust to the closest value
        var delta = mInitialScrollOffset - mCurrentScrollOffset
        if (delta == 0) {
            return
        }
        if (abs(delta) > mSelectorElementSize / 2) {
            delta += if (delta > 0) -mSelectorElementSize else mSelectorElementSize
        }
        if (isHorizontalMode) {
            mPreviousScrollerX = 0
            mAdjustScroller.startScroll(0, 0, delta, 0, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
        } else {
            mPreviousScrollerY = 0
            mAdjustScroller.startScroll(0, 0, 0, delta, SELECTOR_ADJUSTMENT_DURATION_MILLIS)
        }
        invalidate()
    }

    /**
     * Command for changing the current value from a long press by one.
     */
    internal inner class ChangeCurrentByOneFromLongPressCommand : Runnable {
        private var mIncrement = false

        fun setStep(increment: Boolean) {
            mIncrement = increment
        }

        override fun run() {
            changeValueByOne(mIncrement)
            postDelayed(this, DEFAULT_LONG_PRESS_UPDATE_INTERVAL)
        }
    }

    private fun formatNumberWithLocale(value: Int): String {
        return mNumberFormatter.format(value.toLong())
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }

    private fun spToPx(sp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            sp,
            resources.displayMetrics,
        )
    }

    private fun pxToSp(px: Float): Float {
        return px / resources.displayMetrics.scaledDensity
    }

    private fun stringToFormatter(formatter: String?): Formatter? {
        return if (TextUtils.isEmpty(formatter)) {
            null
        } else {
            object : Formatter {
                override fun format(value: Int): String {
                    return String.format(Locale.getDefault(), formatter!!, value)
                }
            }
        }
    }

    private fun setWidthAndHeight() {
        if (isHorizontalMode) {
            mMinHeight = SIZE_UNSPECIFIED
            mMaxHeight = dpToPx(DEFAULT_MIN_WIDTH.toFloat()).toInt()
            mMinWidth = dpToPx(DEFAULT_MAX_HEIGHT.toFloat()).toInt()
            mMaxWidth = SIZE_UNSPECIFIED
        } else {
            mMinHeight = SIZE_UNSPECIFIED
            mMaxHeight = dpToPx(DEFAULT_MAX_HEIGHT.toFloat()).toInt()
            mMinWidth = dpToPx(DEFAULT_MIN_WIDTH.toFloat()).toInt()
            mMaxWidth = SIZE_UNSPECIFIED
        }
    }

    fun setDividerColor(
        @ColorInt color: Int
    ) {
        mDividerColor = color
        mDividerDrawable = ColorDrawable(color)
    }

    override fun setOrientation(
        @Orientation orientation: Int
    ) {
        mOrientation = orientation
        setWidthAndHeight()
        requestLayout()
    }

    private fun setWheelItemCount(count: Int) {
        require(count >= 1) { "Wheel item count must be >= 1" }
        mRealWheelItemCount = count
        mWheelItemCount = count.coerceAtLeast(DEFAULT_WHEEL_ITEM_COUNT)
        mWheelMiddleItemIndex = mWheelItemCount / 2
        selectorIndices = IntArray(mWheelItemCount)
    }

    fun setSelectedTextColor(
        @ColorInt color: Int
    ) {
        mSelectedTextColor = color
        mSelectedText.setTextColor(mSelectedTextColor)
    }

    private fun setSelectedTextSize(textSize: Float) {
        mSelectedTextSize = textSize
        mSelectedText.textSize = pxToSp(mSelectedTextSize)
    }

    private fun setSelectedTypeface(typeface: Typeface?) {
        mSelectedTypeface = typeface
        if (mSelectedTypeface != null) {
            mSelectorWheelPaint.typeface = mSelectedTypeface
        } else if (mTypeface != null) {
            mSelectorWheelPaint.typeface = mTypeface
        } else {
            mSelectorWheelPaint.typeface = Typeface.MONOSPACE
        }
    }

    private fun setTypeface(typeface: Typeface?) {
        mTypeface = typeface
        if (mTypeface != null) {
            mSelectedText.typeface = mTypeface
            setSelectedTypeface(mSelectedTypeface)
        } else {
            mSelectedText.typeface = Typeface.MONOSPACE
        }
    }

    private val isHorizontalMode: Boolean
        get() = orientation == HORIZONTAL
    private val isAscendingOrder: Boolean
        get() = order == ASCENDING

    override fun getOrientation(): Int {
        return mOrientation
    }

    companion object {
        private const val SELECTOR_MIDDLE_ITEM_INDEX = 3 / 2
        const val VERTICAL = LinearLayout.VERTICAL
        const val HORIZONTAL = LinearLayout.HORIZONTAL
        const val ASCENDING = 0
        private const val CENTER = 1
        const val SIDE_LINES = 0
        const val UNDERLINE = 1

        /**
         * The default update interval during long press.
         */
        private const val DEFAULT_LONG_PRESS_UPDATE_INTERVAL: Long = 300

        /**
         * The default coefficient to adjust (divide) the max fling velocity.
         */
        private const val DEFAULT_MAX_FLING_VELOCITY_COEFFICIENT = 8

        /**
         * The the duration for adjusting the selector wheel.
         */
        private const val SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800

        /**
         * The duration of scrolling while snapping to a given position.
         */
        private const val SNAP_SCROLL_DURATION = 300

        /**
         * The default strength of fading edge while drawing the selector.
         */
        private const val DEFAULT_FADING_EDGE_STRENGTH = 0.9f

        /**
         * The default unscaled height of the divider.
         */
        private const val UNSCALED_DEFAULT_DIVIDER_THICKNESS = 2

        /**
         * The default unscaled distance between the dividers.
         */
        private const val UNSCALED_DEFAULT_DIVIDER_DISTANCE = 48

        /**
         * Constant for unspecified size.
         */
        private const val SIZE_UNSPECIFIED = -1

        /**
         * The default color of divider.
         */
        private const val DEFAULT_DIVIDER_COLOR = -0x1000000

        /**
         * The default max value of this widget.
         */
        private const val DEFAULT_MAX_VALUE = 100

        /**
         * The default min value of this widget.
         */
        private const val DEFAULT_MIN_VALUE = 1

        /**
         * The default wheel item count of this widget.
         */
        private const val DEFAULT_WHEEL_ITEM_COUNT = 3

        /**
         * The default max height of this widget.
         */
        private const val DEFAULT_MAX_HEIGHT = 180

        /**
         * The default min width of this widget.
         */
        private const val DEFAULT_MIN_WIDTH = 64

        /**
         * The default align of text.
         */
        private const val DEFAULT_TEXT_ALIGN = CENTER

        /**
         * The default color of text.
         */
        private const val DEFAULT_TEXT_COLOR = -0x1000000

        /**
         * The default size of text.
         */
        private const val DEFAULT_TEXT_SIZE = 25f

        /**
         * The default line spacing multiplier of text.
         */
        private const val DEFAULT_LINE_SPACING_MULTIPLIER = 1f

        /**
         * Utility to reconcile a desired size and state, with constraints imposed
         * by a MeasureSpec.  Will take the desired size, unless a different size
         * is imposed by the constraints.  The returned value is a compound integer,
         * with the resolved size in the [.MEASURED_SIZE_MASK] bits and
         * optionally the bit [.MEASURED_STATE_TOO_SMALL] set if the resulting
         * size is smaller than the size the view wants to be.
         *
         * @param size        How big the view wants to be
         * @param measureSpec Constraints imposed by the parent
         * @return Size information bit mask as defined by
         * [.MEASURED_SIZE_MASK] and [.MEASURED_STATE_TOO_SMALL].
         */
        fun resolveSizeAndState(
            size: Int,
            measureSpec: Int,
            childMeasuredState: Int
        ): Int {
            var result = size
            val specMode = MeasureSpec.getMode(measureSpec)
            val specSize = MeasureSpec.getSize(measureSpec)
            when (specMode) {
                MeasureSpec.UNSPECIFIED -> {}
                MeasureSpec.AT_MOST ->
                    if (specSize < size) {
                        result = specSize or MEASURED_STATE_TOO_SMALL
                    }

                MeasureSpec.EXACTLY -> result = specSize
            }
            return result or (childMeasuredState and MEASURED_STATE_MASK)
        }
    }
}