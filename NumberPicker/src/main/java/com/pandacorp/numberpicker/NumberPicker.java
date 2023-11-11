package com.pandacorp.numberpicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.text.NumberFormat;
import java.util.Locale;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * A widget that enables the user to select a number from a predefined range.
 */
public class NumberPicker extends LinearLayout {

    @Retention(SOURCE)
    @IntDef({VERTICAL, HORIZONTAL})
    public @interface Orientation {
    }

    public static final int VERTICAL = LinearLayout.VERTICAL;
    public static final int HORIZONTAL = LinearLayout.HORIZONTAL;

    public static final int ASCENDING = 0;

    public static final int CENTER = 1;

    public static final int SIDE_LINES = 0;
    public static final int UNDERLINE = 1;

    /**
     * The default update interval during long press.
     */
    private static final long DEFAULT_LONG_PRESS_UPDATE_INTERVAL = 300;

    /**
     * The default coefficient to adjust (divide) the max fling velocity.
     */
    private static final int DEFAULT_MAX_FLING_VELOCITY_COEFFICIENT = 8;

    /**
     * The the duration for adjusting the selector wheel.
     */
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS = 800;

    /**
     * The duration of scrolling while snapping to a given position.
     */
    private static final int SNAP_SCROLL_DURATION = 300;

    /**
     * The default strength of fading edge while drawing the selector.
     */
    private static final float DEFAULT_FADING_EDGE_STRENGTH = 0.9f;

    /**
     * The default unscaled height of the divider.
     */
    private static final int UNSCALED_DEFAULT_DIVIDER_THICKNESS = 2;

    /**
     * The default unscaled distance between the dividers.
     */
    private static final int UNSCALED_DEFAULT_DIVIDER_DISTANCE = 48;

    /**
     * Constant for unspecified size.
     */
    private static final int SIZE_UNSPECIFIED = -1;

    /**
     * The default color of divider.
     */
    private static final int DEFAULT_DIVIDER_COLOR = 0xFF000000;

    /**
     * The default max value of this widget.
     */
    private static final int DEFAULT_MAX_VALUE = 100;

    /**
     * The default min value of this widget.
     */
    private static final int DEFAULT_MIN_VALUE = 1;

    /**
     * The default wheel item count of this widget.
     */
    private static final int DEFAULT_WHEEL_ITEM_COUNT = 3;

    /**
     * The default max height of this widget.
     */
    private static final int DEFAULT_MAX_HEIGHT = 180;

    /**
     * The default min width of this widget.
     */
    private static final int DEFAULT_MIN_WIDTH = 64;

    /**
     * The default align of text.
     */
    private static final int DEFAULT_TEXT_ALIGN = CENTER;

    /**
     * The default color of text.
     */
    private static final int DEFAULT_TEXT_COLOR = 0xFF000000;

    /**
     * The default size of text.
     */
    private static final float DEFAULT_TEXT_SIZE = 25f;

    /**
     * The default line spacing multiplier of text.
     */
    private static final float DEFAULT_LINE_SPACING_MULTIPLIER = 1f;

    /**
     * The text for showing the current value.
     */
    private final EditText mSelectedText;

    /**
     * The center X position of the selected text.
     */
    private float mSelectedTextCenterX;

    /**
     * The center Y position of the selected text.
     */
    private float mSelectedTextCenterY;

    /**
     * The min height of this widget.
     */
    private int mMinHeight;

    /**
     * The max height of this widget.
     */
    private int mMaxHeight;

    /**
     * The max width of this widget.
     */
    private int mMinWidth;

    /**
     * The max width of this widget.
     */
    private int mMaxWidth;

    /**
     * Flag whether to compute the max width.
     */
    private final boolean mComputeMaxWidth;

    /**
     * The align of the selected text.
     */
    private int mSelectedTextAlign = DEFAULT_TEXT_ALIGN;

    /**
     * The color of the selected text.
     */
    private int mSelectedTextColor = DEFAULT_TEXT_COLOR;

    /**
     * The size of the selected text.
     */
    private float mSelectedTextSize = DEFAULT_TEXT_SIZE;

    /**
     * Flag whether the selected text should strikethroughed.
     */
    private boolean mSelectedTextStrikeThru;

    /**
     * Flag whether the selected text should underlined.
     */
    private boolean mSelectedTextUnderline;

    /**
     * The typeface of the selected text.
     */
    private Typeface mSelectedTypeface;

    /**
     * The align of the text.
     */
    private int mTextAlign = DEFAULT_TEXT_ALIGN;

    /**
     * The color of the text.
     */
    private int mTextColor = DEFAULT_TEXT_COLOR;

    /**
     * The size of the text.
     */
    private float mTextSize = DEFAULT_TEXT_SIZE;

    /**
     * Flag whether the text should strikethroughed.
     */
    private boolean mTextStrikeThru;

    /**
     * Flag whether the text should underlined.
     */
    private boolean mTextUnderline;

    /**
     * The typeface of the text.
     */
    private Typeface mTypeface;

    /**
     * The height of the gap between text elements if the selector wheel.
     */
    private int mSelectorTextGapHeight;

    /**
     * The values to be displayed instead the indices.
     */
    private String[] mDisplayedValues;

    /**
     * Lower value of the range of numbers allowed for the NumberPicker
     */
    private int mMinValue = DEFAULT_MIN_VALUE;

    /**
     * Upper value of the range of numbers allowed for the NumberPicker
     */
    private int mMaxValue = DEFAULT_MAX_VALUE;

    /**
     * Current value of this NumberPicker
     */
    private int mValue;

    /**
     * Listener to be notified upon current value click.
     */
    private OnClickListener mOnClickListener;

    /**
     * Listener to be notified upon current value change.
     */
    private OnValueChangeListener mOnValueChangeListener;

    /**
     * Formatter for for displaying the current value.
     */
    private Formatter mFormatter;

    /**
     * Cache for the string representation of selector indices.
     */
    private final SparseArray<String> mSelectorIndexToStringCache = new SparseArray<>();

    /**
     * The number of items show in the selector wheel.
     */
    private int mWheelItemCount = DEFAULT_WHEEL_ITEM_COUNT;

    /**
     * The real number of items show in the selector wheel.
     */
    private int mRealWheelItemCount = DEFAULT_WHEEL_ITEM_COUNT;

    /**
     * The index of the middle selector item.
     */
    private int mWheelMiddleItemIndex = mWheelItemCount / 2;

    /**
     * The selector indices whose value are show by the selector.
     */
    private int[] mSelectorIndices = new int[mWheelItemCount];

    /**
     * The {@link Paint} for drawing the selector.
     */
    private final Paint mSelectorWheelPaint;

    /**
     * The size of a selector element (text + gap).
     */
    private int mSelectorElementSize;

    /**
     * The initial offset of the scroll selector.
     */
    private int mInitialScrollOffset = Integer.MIN_VALUE;

    /**
     * The current offset of the scroll selector.
     */
    private int mCurrentScrollOffset;

    /**
     * The {@link Scroller} responsible for flinging the selector.
     */
    private final Scroller mFlingScroller;

    /**
     * The {@link Scroller} responsible for adjusting the selector.
     */
    private final Scroller mAdjustScroller;

    /**
     * The previous X coordinate while scrolling the selector.
     */
    private int mPreviousScrollerX;

    /**
     * The previous Y coordinate while scrolling the selector.
     */
    private int mPreviousScrollerY;

    /**
     * Handle to the reusable command for changing the current value from long press by one.
     */
    private ChangeCurrentByOneFromLongPressCommand mChangeCurrentByOneFromLongPressCommand;

    /**
     * The X position of the last down event.
     */
    private float mLastDownEventX;

    /**
     * The Y position of the last down event.
     */
    private float mLastDownEventY;

    /**
     * The X position of the last down or move event.
     */
    private float mLastDownOrMoveEventX;

    /**
     * The Y position of the last down or move event.
     */
    private float mLastDownOrMoveEventY;

    /**
     * Determines speed during touch scrolling.
     */
    private VelocityTracker mVelocityTracker;

    /**
     * @see ViewConfiguration#getScaledTouchSlop()
     */
    private final int mTouchSlop;

    /**
     * @see ViewConfiguration#getScaledMinimumFlingVelocity()
     */
    private final int mMinimumFlingVelocity;

    /**
     * @see ViewConfiguration#getScaledMaximumFlingVelocity()
     */
    private final int mMaximumFlingVelocity;

    /**
     * Flag whether the selector should wrap around.
     */
    private boolean mWrapSelectorWheel;

    /**
     * User choice on whether the selector wheel should be wrapped.
     */
    private boolean mWrapSelectorWheelPreferred = true;

    /**
     * Divider for showing item to be selected while scrolling
     */
    private Drawable mDividerDrawable;

    /**
     * The color of the divider.
     */
    private int mDividerColor = DEFAULT_DIVIDER_COLOR;

    /**
     * The distance between the two dividers.
     */
    private final int mDividerDistance;

    /**
     * The thickness of the divider.
     */
    private final int mDividerLength;

    /**
     * The thickness of the divider.
     */
    private final int mDividerThickness;

    /**
     * The top of the top divider.
     */
    private int mTopDividerTop;

    /**
     * The bottom of the bottom divider.
     */
    private int mBottomDividerBottom;

    /**
     * The left of the top divider.
     */
    private int mLeftDividerLeft;

    /**
     * The right of the right divider.
     */
    private int mRightDividerRight;

    /**
     * The type of the divider.
     */
    private final int mDividerType;

    /**
     * The current scroll state of the number picker.
     */
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    /**
     * The keycode of the last handled DPAD down event.
     */
    private int mLastHandledDownDpadKeyCode = -1;

    /**
     * Flag whether the selector wheel should hidden until the picker has focus.
     */
    private final boolean mHideWheelUntilFocused;

    /**
     * The orientation of this widget.
     */
    private int mOrientation;

    /**
     * The order of this widget.
     */
    private final int mOrder;

    /**
     * Flag whether the fading edge should enabled.
     */
    private boolean mFadingEdgeEnabled = true;

    /**
     * The strength of fading edge while drawing the selector.
     */
    private float mFadingEdgeStrength = DEFAULT_FADING_EDGE_STRENGTH;

    /**
     * Flag whether the scroller should enabled.
     */
    private boolean mScrollerEnabled = true;

    /**
     * The line spacing multiplier of the text.
     */
    private float mLineSpacingMultiplier = DEFAULT_LINE_SPACING_MULTIPLIER;

    /**
     * Flag whether the accessibility description enabled.
     */
    private final boolean mAccessibilityDescriptionEnabled;

    /**
     * The number formatter for current locale.
     */
    private NumberFormat mNumberFormatter;

    /**
     * Interface to listen for changes of the current value.
     */
    public interface OnValueChangeListener {

        /**
         * Called upon a change of the current value.
         *
         * @param picker The NumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        void onValueChange(NumberPicker picker, int oldVal, int newVal);
    }

    /**
     * The amount of space between items.
     */
    private final int mItemSpacing;

    /**
     * Interface to listen for the picker scroll state.
     */
    public interface OnScrollListener {

        /**
         * The view is not scrolling.
         */
        int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and his finger is still on the screen.
         */
        int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and performed a fling.
         */
        int SCROLL_STATE_FLING = 2;

    }

    /**
     * Interface used to format current value into a string for presentation.
     */
    public interface Formatter {

        /**
         * Formats a string representation of the current value.
         *
         * @param value The currently selected value.
         * @return A formatted string representation.
         */
        String format(int value);
    }

    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     */
    public NumberPicker(Context context) {
        this(context, null);
    }

    /**
     * Create a new number picker.
     *
     * @param context The application environment.
     * @param attrs   A collection of attributes.
     */
    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Create a new number picker
     *
     * @param context  the application environment.
     * @param attrs    a collection of attributes.
     * @param defStyle The default style to apply to this view.
     */
    public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mNumberFormatter = NumberFormat.getInstance();

        final TypedArray attributes = context.obtainStyledAttributes(attrs,
                R.styleable.NumberPicker, defStyle, 0);

        final Drawable selectionDivider = attributes.getDrawable(
                R.styleable.NumberPicker_np_divider);
        if (selectionDivider != null) {
            selectionDivider.setCallback(this);
            if (selectionDivider.isStateful()) {
                selectionDivider.setState(getDrawableState());
            }
            mDividerDrawable = selectionDivider;
        } else {
            mDividerColor = attributes.getColor(R.styleable.NumberPicker_np_dividerColor,
                    mDividerColor);
            setDividerColor(mDividerColor);
        }

        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        final int defDividerDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_DEFAULT_DIVIDER_DISTANCE, displayMetrics);
        final int defDividerThickness = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                UNSCALED_DEFAULT_DIVIDER_THICKNESS, displayMetrics);
        mDividerDistance = attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_dividerDistance, defDividerDistance);
        mDividerLength = attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_dividerLength, 0);
        mDividerThickness = attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_dividerThickness, defDividerThickness);
        mDividerType = attributes.getInt(R.styleable.NumberPicker_np_dividerType, SIDE_LINES);

        mOrder = attributes.getInt(R.styleable.NumberPicker_np_order, ASCENDING);
        mOrientation = attributes.getInt(R.styleable.NumberPicker_np_orientation, VERTICAL);

        final float width = attributes.getDimensionPixelSize(R.styleable.NumberPicker_np_width,
                SIZE_UNSPECIFIED);
        final float height = attributes.getDimensionPixelSize(R.styleable.NumberPicker_np_height,
                SIZE_UNSPECIFIED);

        setWidthAndHeight();

        mComputeMaxWidth = true;

        mValue = attributes.getInt(R.styleable.NumberPicker_np_value, mValue);
        mMaxValue = attributes.getInt(R.styleable.NumberPicker_np_max, mMaxValue);
        mMinValue = attributes.getInt(R.styleable.NumberPicker_np_min, mMinValue);

        mSelectedTextAlign = attributes.getInt(R.styleable.NumberPicker_np_selectedTextAlign,
                mSelectedTextAlign);
        mSelectedTextColor = attributes.getColor(R.styleable.NumberPicker_np_selectedTextColor,
                mSelectedTextColor);
        mSelectedTextSize = attributes.getDimension(R.styleable.NumberPicker_np_selectedTextSize,
                spToPx(mSelectedTextSize));
        mSelectedTextStrikeThru = attributes.getBoolean(
                R.styleable.NumberPicker_np_selectedTextStrikeThru, mSelectedTextStrikeThru);
        mSelectedTextUnderline = attributes.getBoolean(
                R.styleable.NumberPicker_np_selectedTextUnderline, mSelectedTextUnderline);
        mSelectedTypeface = Typeface.create(attributes.getString(
                R.styleable.NumberPicker_np_selectedTypeface), Typeface.NORMAL);
        mTextAlign = attributes.getInt(R.styleable.NumberPicker_np_textAlign, mTextAlign);
        mTextColor = attributes.getColor(R.styleable.NumberPicker_np_textColor, mTextColor);
        mTextSize = attributes.getDimension(R.styleable.NumberPicker_np_textSize,
                spToPx(mTextSize));
        mTextStrikeThru = attributes.getBoolean(
                R.styleable.NumberPicker_np_textStrikeThru, mTextStrikeThru);
        mTextUnderline = attributes.getBoolean(
                R.styleable.NumberPicker_np_textUnderline, mTextUnderline);
        mTypeface = Typeface.create(attributes.getString(R.styleable.NumberPicker_np_typeface),
                Typeface.NORMAL);
        mFormatter = stringToFormatter(attributes.getString(R.styleable.NumberPicker_np_formatter));
        mFadingEdgeEnabled = attributes.getBoolean(R.styleable.NumberPicker_np_fadingEdgeEnabled,
                mFadingEdgeEnabled);
        mFadingEdgeStrength = attributes.getFloat(R.styleable.NumberPicker_np_fadingEdgeStrength,
                mFadingEdgeStrength);
        mScrollerEnabled = attributes.getBoolean(R.styleable.NumberPicker_np_scrollerEnabled,
                mScrollerEnabled);
        mWheelItemCount = attributes.getInt(R.styleable.NumberPicker_np_wheelItemCount,
                mWheelItemCount);
        mLineSpacingMultiplier = attributes.getFloat(
                R.styleable.NumberPicker_np_lineSpacingMultiplier, mLineSpacingMultiplier);
        int mMaxFlingVelocityCoefficient = attributes.getInt(
                R.styleable.NumberPicker_np_maxFlingVelocityCoefficient,
                DEFAULT_MAX_FLING_VELOCITY_COEFFICIENT);
        mHideWheelUntilFocused = attributes.getBoolean(
                R.styleable.NumberPicker_np_hideWheelUntilFocused, false);
        mAccessibilityDescriptionEnabled = attributes.getBoolean(
                R.styleable.NumberPicker_np_accessibilityDescriptionEnabled, true);
        mItemSpacing = attributes.getDimensionPixelSize(
                R.styleable.NumberPicker_np_itemSpacing, 0);
        // By default LinearLayout that we extend is not drawn. This is
        // its draw() method is not called but dispatchDraw() is called
        // directly (see ViewGroup.drawChild()). However, this class uses
        // the fading edge effect implemented by View and we need our
        // draw() method to be called. Therefore, we declare we will draw.
        setWillNotDraw(false);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.number_picker_material, this, true);

        // input text
        mSelectedText = findViewById(R.id.np__numberpicker_input);
        mSelectedText.setEnabled(false);
        mSelectedText.setFocusable(false);
        mSelectedText.setImeOptions(EditorInfo.IME_ACTION_NONE);

        // create the selector wheel paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        mSelectorWheelPaint = paint;

        setSelectedTextColor(mSelectedTextColor);
        setTextColor(mTextColor);
        setTextSize(mTextSize);
        setSelectedTextSize(mSelectedTextSize);
        setTypeface(mTypeface);
        setSelectedTypeface(mSelectedTypeface);
        setFormatter(mFormatter);
        updateInputTextView();

        setValue(mValue);
        setMaxValue(mMaxValue);
        setMinValue(mMinValue);

        setWheelItemCount(mWheelItemCount);

        mWrapSelectorWheel = attributes.getBoolean(R.styleable.NumberPicker_np_wrapSelectorWheel,
                mWrapSelectorWheel);
        setWrapSelectorWheel(mWrapSelectorWheel);

        if (width != SIZE_UNSPECIFIED && height != SIZE_UNSPECIFIED) {
            setScaleX(width / mMinWidth);
            setScaleY(height / mMaxHeight);
        } else if (width != SIZE_UNSPECIFIED) {
            final float scale = width / mMinWidth;
            setScaleX(scale);
            setScaleY(scale);
        } else if (height != SIZE_UNSPECIFIED) {
            final float scale = height / mMaxHeight;
            setScaleX(scale);
            setScaleY(scale);
        }

        // initialize constants
        ViewConfiguration mViewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = mViewConfiguration.getScaledTouchSlop();
        mMinimumFlingVelocity = mViewConfiguration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = mViewConfiguration.getScaledMaximumFlingVelocity()
                / mMaxFlingVelocityCoefficient;

        // create the fling and adjust scrollers
        mFlingScroller = new Scroller(context, null, true);
        mAdjustScroller = new Scroller(context, new DecelerateInterpolator(2.5f));

        // If not explicitly specified this view is important for accessibility.
        if (getImportantForAccessibility() == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Should be focusable by default, as the text view whose visibility changes is focusable
            if (getFocusable() == View.FOCUSABLE_AUTO) {
                setFocusable(View.FOCUSABLE);
                setFocusableInTouchMode(true);
            }
        }

        attributes.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int msrdWdth = getMeasuredWidth();
        final int msrdHght = getMeasuredHeight();

        // Input text centered horizontally.
        final int inptTxtMsrdWdth = mSelectedText.getMeasuredWidth();
        final int inptTxtMsrdHght = mSelectedText.getMeasuredHeight();
        final int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        final int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        final int inptTxtRight = inptTxtLeft + inptTxtMsrdWdth;
        final int inptTxtBottom = inptTxtTop + inptTxtMsrdHght;
        mSelectedText.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom);
        mSelectedTextCenterX = mSelectedText.getX() + mSelectedText.getMeasuredWidth() / 2f - 2f;
        mSelectedTextCenterY = mSelectedText.getY() + mSelectedText.getMeasuredHeight() / 2f - 5f;

        if (changed) {
            // need to do all this when we know our size
            initializeSelectorWheel();
            initializeFadingEdges();

            final int dividerDistance = 2 * mDividerThickness + mDividerDistance;
            if (isHorizontalMode()) {
                mLeftDividerLeft = (getWidth() - mDividerDistance) / 2 - mDividerThickness;
                mRightDividerRight = mLeftDividerLeft + dividerDistance;
                mBottomDividerBottom = getHeight();
            } else {
                mTopDividerTop = (getHeight() - mDividerDistance) / 2 - mDividerThickness;
                mBottomDividerBottom = mTopDividerTop + dividerDistance;
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try greedily to fit the max width and height.
        final int newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth);
        final int newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        // Flag if we are measured with width or height less than the respective min.
        final int widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth, getMeasuredWidth(),
                widthMeasureSpec);
        final int heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight, getMeasuredHeight(),
                heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    /**
     * Move to the final position of a scroller. Ensures to force finish the scroller
     * and if it is not at its final position a scroll of the selector wheel is
     * performed to fast forward to the final position.
     *
     * @param scroller The scroller to whose final position to get.
     * @return True of the a move was performed, i.e. the scroller was not in final position.
     */
    private boolean moveToFinalScrollerPosition(Scroller scroller) {
        scroller.forceFinished(true);
        if (isHorizontalMode()) {
            int amountToScroll = scroller.getFinalX() - scroller.getCurrX();
            int futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize;
            int overshootAdjustment = mInitialScrollOffset - futureScrollOffset;
            if (overshootAdjustment != 0) {
                if (Math.abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize;
                    } else {
                        overshootAdjustment += mSelectorElementSize;
                    }
                }
                amountToScroll += overshootAdjustment;
                scrollBy(amountToScroll, 0);
                return true;
            }
        } else {
            int amountToScroll = scroller.getFinalY() - scroller.getCurrY();
            int futureScrollOffset = (mCurrentScrollOffset + amountToScroll) % mSelectorElementSize;
            int overshootAdjustment = mInitialScrollOffset - futureScrollOffset;
            if (overshootAdjustment != 0) {
                if (Math.abs(overshootAdjustment) > mSelectorElementSize / 2) {
                    if (overshootAdjustment > 0) {
                        overshootAdjustment -= mSelectorElementSize;
                    } else {
                        overshootAdjustment += mSelectorElementSize;
                    }
                }
                amountToScroll += overshootAdjustment;
                scrollBy(0, amountToScroll);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action != MotionEvent.ACTION_DOWN) {
            return false;
        }

        removeAllCallbacks();
        // Make sure we support flinging inside scrollables.
        getParent().requestDisallowInterceptTouchEvent(true);

        if (isHorizontalMode()) {
            mLastDownOrMoveEventX = mLastDownEventX = event.getX();
            if (!mFlingScroller.isFinished()) {
                mFlingScroller.forceFinished(true);
                mAdjustScroller.forceFinished(true);
                onScrollerFinished(mFlingScroller);
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            } else if (!mAdjustScroller.isFinished()) {
                mFlingScroller.forceFinished(true);
                mAdjustScroller.forceFinished(true);
                onScrollerFinished(mAdjustScroller);
            } else if (mLastDownEventX >= mLeftDividerLeft
                    && mLastDownEventX <= mRightDividerRight) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(this);
                }
            } else if (mLastDownEventX < mLeftDividerLeft) {
                postChangeCurrentByOneFromLongPress(false);
            } else if (mLastDownEventX > mRightDividerRight) {
                postChangeCurrentByOneFromLongPress(true);
            }
        } else {
            mLastDownOrMoveEventY = mLastDownEventY = event.getY();
            if (!mFlingScroller.isFinished()) {
                mFlingScroller.forceFinished(true);
                mAdjustScroller.forceFinished(true);
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            } else if (!mAdjustScroller.isFinished()) {
                mFlingScroller.forceFinished(true);
                mAdjustScroller.forceFinished(true);
            } else if (mLastDownEventY >= mTopDividerTop
                    && mLastDownEventY <= mBottomDividerBottom) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(this);
                }
            } else if (mLastDownEventY < mTopDividerTop) {
                postChangeCurrentByOneFromLongPress(false);
            } else if (mLastDownEventY > mBottomDividerBottom) {
                postChangeCurrentByOneFromLongPress(true);
            }
        }
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        if (!isScrollerEnabled()) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                if (isHorizontalMode()) {
                    float currentMoveX = event.getX();
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        int deltaDownX = (int) Math.abs(currentMoveX - mLastDownEventX);
                        if (deltaDownX > mTouchSlop) {
                            removeAllCallbacks();
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                        }
                    } else {
                        int deltaMoveX = (int) ((currentMoveX - mLastDownOrMoveEventX));
                        scrollBy(deltaMoveX, 0);
                        invalidate();
                    }
                    mLastDownOrMoveEventX = currentMoveX;
                } else {
                    float currentMoveY = event.getY();
                    if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                        int deltaDownY = (int) Math.abs(currentMoveY - mLastDownEventY);
                        if (deltaDownY > mTouchSlop) {
                            removeAllCallbacks();
                            onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                        }
                    } else {
                        int deltaMoveY = (int) ((currentMoveY - mLastDownOrMoveEventY));
                        scrollBy(0, deltaMoveY);
                        invalidate();
                    }
                    mLastDownOrMoveEventY = currentMoveY;
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                removeChangeCurrentByOneFromLongPress();
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                if (isHorizontalMode()) {
                    int initialVelocity = (int) velocityTracker.getXVelocity();
                    if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity);
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                    } else {
                        int eventX = (int) event.getX();
                        int deltaMoveX = (int) Math.abs(eventX - mLastDownEventX);
                        if (deltaMoveX <= mTouchSlop) {
                            int selectorIndexOffset = (eventX / mSelectorElementSize)
                                    - mWheelMiddleItemIndex;
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true);
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false);
                            } else {
                                ensureScrollWheelAdjusted();
                            }
                        } else {
                            ensureScrollWheelAdjusted();
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                    }
                } else {
                    int initialVelocity = (int) velocityTracker.getYVelocity();
                    if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                        fling(initialVelocity);
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                    } else {
                        int eventY = (int) event.getY();
                        int deltaMoveY = (int) Math.abs(eventY - mLastDownEventY);
                        if (deltaMoveY <= mTouchSlop) {
                            int selectorIndexOffset = (eventY / mSelectorElementSize)
                                    - mWheelMiddleItemIndex;
                            if (selectorIndexOffset > 0) {
                                changeValueByOne(true);
                            } else if (selectorIndexOffset < 0) {
                                changeValueByOne(false);
                            } else {
                                ensureScrollWheelAdjusted();
                            }
                        } else {
                            ensureScrollWheelAdjusted();
                        }
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                    }
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            break;
        }
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        final int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                removeAllCallbacks();
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_UP:
                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN:
                        if (mWrapSelectorWheel || ((keyCode == KeyEvent.KEYCODE_DPAD_DOWN)
                                ? getValue() < getMaxValue() : getValue() > getMinValue())) {
                            requestFocus();
                            mLastHandledDownDpadKeyCode = keyCode;
                            removeAllCallbacks();
                            if (mFlingScroller.isFinished()) {
                                changeValueByOne(keyCode == KeyEvent.KEYCODE_DPAD_DOWN);
                            }
                            return true;
                        }
                        break;
                    case KeyEvent.ACTION_UP:
                        if (mLastHandledDownDpadKeyCode == keyCode) {
                            mLastHandledDownDpadKeyCode = -1;
                            return true;
                        }
                        break;
                }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTrackballEvent(MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                removeAllCallbacks();
                break;
        }
        return super.dispatchTrackballEvent(event);
    }

    @Override
    public void computeScroll() {
        if (!isScrollerEnabled()) {
            return;
        }

        Scroller scroller = mFlingScroller;
        if (scroller.isFinished()) {
            scroller = mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        if (isHorizontalMode()) {
            int currentScrollerX = scroller.getCurrX();
            if (mPreviousScrollerX == 0) {
                mPreviousScrollerX = scroller.getStartX();
            }
            scrollBy(currentScrollerX - mPreviousScrollerX, 0);
            mPreviousScrollerX = currentScrollerX;
        } else {
            int currentScrollerY = scroller.getCurrY();
            if (mPreviousScrollerY == 0) {
                mPreviousScrollerY = scroller.getStartY();
            }
            scrollBy(0, currentScrollerY - mPreviousScrollerY);
            mPreviousScrollerY = currentScrollerY;
        }
        if (scroller.isFinished()) {
            onScrollerFinished(scroller);
        } else {
            postInvalidate();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mSelectedText.setEnabled(enabled);
    }

    @Override
    public void scrollBy(int x, int y) {
        int SELECTOR_MIDDLE_ITEM_INDEX = 3/2;
        int mSelectorElementHeight = mSelectorElementSize;
        int[] selectorIndices = mSelectorIndices;
        int startScrollOffset = mCurrentScrollOffset;
        if (!mWrapSelectorWheel && y > 0
                && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
            mCurrentScrollOffset = mInitialScrollOffset;
            return;
        }
        if (!mWrapSelectorWheel && y < 0
                && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
            mCurrentScrollOffset = mInitialScrollOffset;
            return;
        }
        mCurrentScrollOffset += y;
        while (mCurrentScrollOffset - mInitialScrollOffset > mSelectorTextGapHeight) {
            mCurrentScrollOffset -= mSelectorElementHeight;
            decrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
            if (!mWrapSelectorWheel && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -mSelectorTextGapHeight) {
            mCurrentScrollOffset += mSelectorElementHeight;
            incrementSelectorIndices(selectorIndices);
            setValueInternal(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX], true);
            if (!mWrapSelectorWheel && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
        if (startScrollOffset != mCurrentScrollOffset) {
            onScrollChanged(0, mCurrentScrollOffset, 0, startScrollOffset);
        }
    }

    private int computeScrollOffset(boolean isHorizontalMode) {
        return isHorizontalMode ? mCurrentScrollOffset : 0;
    }

    private int computeScrollRange(boolean isHorizontalMode) {
        return isHorizontalMode ? (mMaxValue - mMinValue + 1) * mSelectorElementSize : 0;
    }

    private int computeScrollExtent(boolean isHorizontalMode) {
        return isHorizontalMode ? getWidth() : getHeight();
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return computeScrollOffset(isHorizontalMode());
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return computeScrollRange(isHorizontalMode());
    }

    @Override
    protected int computeHorizontalScrollExtent() {
        return computeScrollExtent(isHorizontalMode());
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return computeScrollOffset(!isHorizontalMode());
    }

    @Override
    protected int computeVerticalScrollRange() {
        return computeScrollRange(!isHorizontalMode());
    }

    @Override
    protected int computeVerticalScrollExtent() {
        return computeScrollExtent(isHorizontalMode());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mNumberFormatter = NumberFormat.getInstance();
    }

    /**
     * Set listener to be notified on click of the current value.
     *
     * @param onClickListener The listener.
     */
    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    /**
     * Sets the listener to be notified on change of the current value.
     *
     * @param onValueChangedListener The listener.
     */
    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        mOnValueChangeListener = onValueChangedListener;
    }

    /**
     * Set the formatter to be used for formatting the current value.
     * <p>
     * Note: If you have provided alternative values for the values this
     * formatter is never invoked.
     * </p>
     *
     * @param formatter The formatter object. If formatter is <code>null</code>,
     *                  {@link String#valueOf(int)} will be used.
     * @see #setDisplayedValues(String[])
     */
    public void setFormatter(Formatter formatter) {
        if (formatter == mFormatter) {
            return;
        }
        mFormatter = formatter;
        initializeSelectorWheelIndices();
        updateInputTextView();
    }

    public void setValue(int value) {
        setValueInternal(value, false);
    }

    private float getMaxTextSize() {
        return Math.max(mTextSize, mSelectedTextSize);
    }

    private float getPaintCenterY(Paint.FontMetrics fontMetrics) {
        if (fontMetrics == null) {
            return 0;
        }
        return Math.abs(fontMetrics.top + fontMetrics.bottom) / 2;
    }

    /**
     * Computes the max width if no such specified as an attribute.
     */
    private void tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return;
        }
        mSelectorWheelPaint.setTextSize(getMaxTextSize());
        int maxTextWidth = 0;
        if (mDisplayedValues == null) {
            float maxDigitWidth = 0;
            for (int i = 0; i <= 9; i++) {
                final float digitWidth = mSelectorWheelPaint.measureText(formatNumber(i));
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth;
                }
            }
            int numberOfDigits = 0;
            int current = mMaxValue;
            while (current > 0) {
                numberOfDigits++;
                current = current / 10;
            }
            maxTextWidth = (int) (numberOfDigits * maxDigitWidth);
        } else {
            for (String displayedValue : mDisplayedValues) {
                final float textWidth = mSelectorWheelPaint.measureText(displayedValue);
                if (textWidth > maxTextWidth) {
                    maxTextWidth = (int) textWidth;
                }
            }
        }
        maxTextWidth += mSelectedText.getPaddingLeft() + mSelectedText.getPaddingRight();
        if (mMaxWidth != maxTextWidth) {
            mMaxWidth = Math.max(maxTextWidth, mMinWidth);
            invalidate();
        }
    }

    /**
     * Sets whether the selector wheel shown during flinging/scrolling should
     * wrap around the {@link NumberPicker#getMinValue()} and
     * {@link NumberPicker#getMaxValue()} values.
     * <p>
     * By default if the range (max - min) is more than the number of items shown
     * on the selector wheel the selector wheel wrapping is enabled.
     * </p>
     * <p>
     * <strong>Note:</strong> If the number of items, i.e. the range (
     * {@link #getMaxValue()} - {@link #getMinValue()}) is less than
     * the number of items shown on the selector wheel, the selector wheel will
     * not wrap. Hence, in such a case calling this method is a NOP.
     * </p>
     *
     * @param wrapSelectorWheel Whether to wrap.
     */
    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        mWrapSelectorWheelPreferred = wrapSelectorWheel;
        updateWrapSelectorWheel();
    }

    /**
     * Whether or not the selector wheel should be wrapped is determined by user choice and whether
     * the choice is allowed. The former comes from {@link #setWrapSelectorWheel(boolean)}, the
     * latter is calculated based on min & max value set vs selector's visual length. Therefore,
     * this method should be called any time any of the 3 values (i.e. user choice, min and max
     * value) gets updated.
     */
    private void updateWrapSelectorWheel() {
        mWrapSelectorWheel = isWrappingAllowed() && mWrapSelectorWheelPreferred;
    }

    private boolean isWrappingAllowed() {
        return mMaxValue - mMinValue >= mSelectorIndices.length - 1;
    }

    /**
     * Returns the value of the picker.
     *
     * @return The value.
     */
    public int getValue() {
        return mValue;
    }

    /**
     * Returns the min value of the picker.
     *
     * @return The min value
     */
    public int getMinValue() {
        return mMinValue;
    }

    /**
     * Sets the min value of the picker.
     *
     * @param minValue The min value inclusive.
     *                 <strong>Note:</strong> The length of the displayed values array
     *                 set via {@link #setDisplayedValues(String[])} must be equal to the
     *                 range of selectable numbers which is equal to
     *                 {@link #getMaxValue()} - {@link #getMinValue()} + 1.
     */
    public void setMinValue(int minValue) {
        mMinValue = minValue;
        if (mMinValue > mValue) {
            mValue = mMinValue;
        }

        updateWrapSelectorWheel();
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
        invalidate();
    }

    /**
     * Returns the max value of the picker.
     *
     * @return The max value.
     */
    public int getMaxValue() {
        return mMaxValue;
    }

    /**
     * Sets the max value of the picker.
     *
     * @param maxValue The max value inclusive.
     *                 <strong>Note:</strong> The length of the displayed values array
     *                 set via {@link #setDisplayedValues(String[])} must be equal to the
     *                 range of selectable numbers which is equal to
     *                 {@link #getMaxValue()} - {@link #getMinValue()} + 1.
     */
    public void setMaxValue(int maxValue) {
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
        mMaxValue = maxValue;
        if (mMaxValue < mValue) {
            mValue = mMaxValue;
        }

        updateWrapSelectorWheel();
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
        invalidate();
    }

    /**
     * Sets the values to be displayed.
     *
     * @param displayedValues The displayed values.
     *                        <strong>Note:</strong> The length of the displayed values array
     *                        must be equal to the range of selectable numbers which is equal to
     *                        {@link #getMaxValue()} - {@link #getMinValue()} + 1.
     */
    public void setDisplayedValues(String[] displayedValues) {
        if (mDisplayedValues == displayedValues) {
            return;
        }
        mDisplayedValues = displayedValues;
        if (mDisplayedValues != null) {
            // Allow text entry rather than strictly numeric entry.
            mSelectedText.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        } else {
            mSelectedText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
        }
        updateInputTextView();
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
    }

    private float getFadingEdgeStrength(boolean isHorizontalMode) {
        return isHorizontalMode && mFadingEdgeEnabled ? mFadingEdgeStrength : 0;
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return getFadingEdgeStrength(!isHorizontalMode());
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return getFadingEdgeStrength(!isHorizontalMode());
    }

    @Override
    protected float getLeftFadingEdgeStrength() {
        return getFadingEdgeStrength(isHorizontalMode());
    }

    @Override
    protected float getRightFadingEdgeStrength() {
        return getFadingEdgeStrength(isHorizontalMode());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAllCallbacks();
    }

    @CallSuper
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mDividerDrawable != null && mDividerDrawable.isStateful()
                && mDividerDrawable.setState(getDrawableState())) {
            invalidateDrawable(mDividerDrawable);
        }
    }

    @CallSuper
    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (mDividerDrawable != null) {
            mDividerDrawable.jumpToCurrentState();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // save canvas
        canvas.save();

        final boolean showSelectorWheel = !mHideWheelUntilFocused || hasFocus();
        float x, y;
        if (isHorizontalMode()) {
            x = mCurrentScrollOffset;
            y = mSelectedText.getBaseline() + mSelectedText.getTop();
            if (mRealWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(mLeftDividerLeft, 0, mRightDividerRight, getBottom());
            }
        } else {
            x = (getRight() - getLeft()) / 2f;
            y = mCurrentScrollOffset;
            if (mRealWheelItemCount < DEFAULT_WHEEL_ITEM_COUNT) {
                canvas.clipRect(0, mTopDividerTop, getRight(), mBottomDividerBottom);
            }
        }

        // draw the selector wheel
        int[] selectorIndices = getSelectorIndices();
        for (int i = 0; i < selectorIndices.length; i++) {
            if (i == mWheelMiddleItemIndex) {
                mSelectorWheelPaint.setTextAlign(Paint.Align.values()[mSelectedTextAlign]);
                mSelectorWheelPaint.setTextSize(mSelectedTextSize);
                mSelectorWheelPaint.setColor(mSelectedTextColor);
                mSelectorWheelPaint.setStrikeThruText(mSelectedTextStrikeThru);
                mSelectorWheelPaint.setUnderlineText(mSelectedTextUnderline);
                mSelectorWheelPaint.setTypeface(mSelectedTypeface);
            } else {
                mSelectorWheelPaint.setTextAlign(Paint.Align.values()[mTextAlign]);
                mSelectorWheelPaint.setTextSize(mTextSize);
                mSelectorWheelPaint.setColor(mTextColor);
                mSelectorWheelPaint.setStrikeThruText(mTextStrikeThru);
                mSelectorWheelPaint.setUnderlineText(mTextUnderline);
                mSelectorWheelPaint.setTypeface(mTypeface);
            }

            int selectorIndex = selectorIndices[isAscendingOrder()
                    ? i : selectorIndices.length - i - 1];
            String scrollSelectorValue = mSelectorIndexToStringCache.get(selectorIndex);
            if (scrollSelectorValue == null) {
                continue;
            }
            // Do not draw the middle item if input is visible since the input
            // is shown only if the wheel is static and it covers the middle
            // item. Otherwise, if the user starts editing the text via the
            // IME he may see a dimmed version of the old value intermixed
            // with the new one.
            if ((showSelectorWheel && i != mWheelMiddleItemIndex)
                    || (i == mWheelMiddleItemIndex && mSelectedText.getVisibility() != VISIBLE)) {
                float textY = y;
                if (!isHorizontalMode()) {
                    textY += getPaintCenterY(mSelectorWheelPaint.getFontMetrics());
                }

                int xOffset = 0;
                int yOffset = 0;

                if (i != mWheelMiddleItemIndex && mItemSpacing != 0) {
                    if (isHorizontalMode()) {
                        if (i > mWheelMiddleItemIndex) {
                            xOffset = mItemSpacing;
                        } else {
                            xOffset = -mItemSpacing;
                        }
                    } else {
                        if (i > mWheelMiddleItemIndex) {
                            yOffset = mItemSpacing;
                        } else {
                            yOffset = -mItemSpacing;
                        }
                    }
                }

                drawText(scrollSelectorValue, x + xOffset, textY + yOffset, mSelectorWheelPaint, canvas);
            }

            if (isHorizontalMode()) {
                x += mSelectorElementSize;
            } else {
                y += mSelectorElementSize;
            }
        }

        // restore canvas
        canvas.restore();

        // draw the dividers
        if (showSelectorWheel && mDividerDrawable != null) {
            if (isHorizontalMode())
                drawHorizontalDividers(canvas);
            else
                drawVerticalDividers(canvas);
        }
    }

    private void drawHorizontalDividers(Canvas canvas) {
        switch (mDividerType) {
            case SIDE_LINES:
                final int top;
                final int bottom;
                if (mDividerLength > 0 && mDividerLength <= mMaxHeight) {
                    top = (mMaxHeight - mDividerLength) / 2;
                    bottom = top + mDividerLength;
                } else {
                    top = 0;
                    bottom = getBottom();
                }
                // draw the left divider
                final int leftOfLeftDivider = mLeftDividerLeft;
                final int rightOfLeftDivider = leftOfLeftDivider + mDividerThickness;
                mDividerDrawable.setBounds(leftOfLeftDivider, top, rightOfLeftDivider, bottom);
                mDividerDrawable.draw(canvas);
                // draw the right divider
                final int rightOfRightDivider = mRightDividerRight;
                final int leftOfRightDivider = rightOfRightDivider - mDividerThickness;
                mDividerDrawable.setBounds(leftOfRightDivider, top, rightOfRightDivider, bottom);
                mDividerDrawable.draw(canvas);
                break;
            case UNDERLINE:
                final int left;
                final int right;
                if (mDividerLength > 0 && mDividerLength <= mMaxWidth) {
                    left = (mMaxWidth - mDividerLength) / 2;
                    right = left + mDividerLength;
                } else {
                    left = mLeftDividerLeft;
                    right = mRightDividerRight;
                }
                final int bottomOfUnderlineDivider = mBottomDividerBottom;
                final int topOfUnderlineDivider = bottomOfUnderlineDivider - mDividerThickness;
                mDividerDrawable.setBounds(
                        left,
                        topOfUnderlineDivider,
                        right,
                        bottomOfUnderlineDivider
                );
                mDividerDrawable.draw(canvas);
                break;
        }
    }

    private void drawVerticalDividers(Canvas canvas) {
        final int left;
        final int right;
        if (mDividerLength > 0 && mDividerLength <= mMaxWidth) {
            left = (mMaxWidth - mDividerLength) / 2;
            right = left + mDividerLength;
        } else {
            left = 0;
            right = getRight();
        }
        switch (mDividerType) {
            case SIDE_LINES:
                // draw the top divider
                final int topOfTopDivider = mTopDividerTop;
                final int bottomOfTopDivider = topOfTopDivider + mDividerThickness;
                mDividerDrawable.setBounds(left, topOfTopDivider, right, bottomOfTopDivider);
                mDividerDrawable.draw(canvas);
                // draw the bottom divider
                final int bottomOfBottomDivider = mBottomDividerBottom;
                final int topOfBottomDivider = bottomOfBottomDivider - mDividerThickness;
                mDividerDrawable.setBounds(
                        left,
                        topOfBottomDivider,
                        right,
                        bottomOfBottomDivider);
                mDividerDrawable.draw(canvas);
                break;
            case UNDERLINE:
                final int bottomOfUnderlineDivider = mBottomDividerBottom;
                final int topOfUnderlineDivider = bottomOfUnderlineDivider - mDividerThickness;
                mDividerDrawable.setBounds(
                        left,
                        topOfUnderlineDivider,
                        right,
                        bottomOfUnderlineDivider
                );
                mDividerDrawable.draw(canvas);
                break;
        }
    }

    private void drawText(String text, float x, float y, Paint paint, Canvas canvas) {
        if (text.contains("\n")) {
            final String[] lines = text.split("\n");
            final float height = Math.abs(paint.descent() + paint.ascent())
                    * mLineSpacingMultiplier;
            final float diff = (lines.length - 1) * height / 2;
            y -= diff;
            for (String line : lines) {
                canvas.drawText(line, x, y, paint);
                y += height;
            }
        } else {
            canvas.drawText(text, x, y, paint);
        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(NumberPicker.class.getName());
        event.setScrollable(isScrollerEnabled());
        final int scroll = (mMinValue + mValue) * mSelectorElementSize;
        final int maxScroll = (mMaxValue - mMinValue) * mSelectorElementSize;
        if (isHorizontalMode()) {
            event.setScrollX(scroll);
            event.setMaxScrollX(maxScroll);
        } else {
            event.setScrollY(scroll);
            event.setMaxScrollY(maxScroll);
        }
    }

    /**
     * Makes a measure spec that tries greedily to use the max value.
     *
     * @param measureSpec The measure spec.
     * @param maxSize     The max value for the size.
     * @return A measure spec greedily imposing the max size.
     */
    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec;
        }
        final int size = MeasureSpec.getSize(measureSpec);
        final int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return measureSpec;
            case MeasureSpec.AT_MOST:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), MeasureSpec.EXACTLY);
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY);
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
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
    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize,
                                                     int measureSpec) {
        if (minSize != SIZE_UNSPECIFIED) {
            final int desiredWidth = Math.max(minSize, measuredSize);
            return resolveSizeAndState(desiredWidth, measureSpec, 0);
        } else {
            return measuredSize;
        }
    }

    /**
     * Utility to reconcile a desired size and state, with constraints imposed
     * by a MeasureSpec.  Will take the desired size, unless a different size
     * is imposed by the constraints.  The returned value is a compound integer,
     * with the resolved size in the {@link #MEASURED_SIZE_MASK} bits and
     * optionally the bit {@link #MEASURED_STATE_TOO_SMALL} set if the resulting
     * size is smaller than the size the view wants to be.
     *
     * @param size        How big the view wants to be
     * @param measureSpec Constraints imposed by the parent
     * @return Size information bit mask as defined by
     * {@link #MEASURED_SIZE_MASK} and {@link #MEASURED_STATE_TOO_SMALL}.
     */
    public static int resolveSizeAndState(int size, int measureSpec, int childMeasuredState) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:
                break;
            case MeasureSpec.AT_MOST:
                if (specSize < size) {
                    result = specSize | MEASURED_STATE_TOO_SMALL;
                }
                break;
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result | (childMeasuredState & MEASURED_STATE_MASK);
    }

    /**
     * Resets the selector indices and clear the cached string representation of
     * these indices.
     */
    private void initializeSelectorWheelIndices() {
        mSelectorIndexToStringCache.clear();
        int[] selectorIndices = getSelectorIndices();
        int current = getValue();
        for (int i = 0; i < selectorIndices.length; i++) {
            int selectorIndex = current + (i - mWheelMiddleItemIndex);
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            selectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(selectorIndices[i]);
        }
    }

    /**
     * Sets the current value of this NumberPicker.
     *
     * @param current      The new value of the NumberPicker.
     * @param notifyChange Whether to notify if the current value changed.
     */
    private void setValueInternal(int current, boolean notifyChange) {
        if (mValue == current) {
            return;
        }
        // Wrap around the values if we go past the start or end
        if (mWrapSelectorWheel) {
            current = getWrappedSelectorIndex(current);
        } else {
            current = Math.max(current, mMinValue);
            current = Math.min(current, mMaxValue);
        }
        int previous = mValue;
        mValue = current;
        // If we're flinging, we'll update the text view at the end when it becomes visible
        if (mScrollState != OnScrollListener.SCROLL_STATE_FLING) {
            updateInputTextView();
        }
        if (notifyChange) {
            notifyChange(previous, current);
        }
        initializeSelectorWheelIndices();
        updateAccessibilityDescription();
        invalidate();
    }

    /**
     * Updates the accessibility values of the view,
     * to the currently selected value
     */
    private void updateAccessibilityDescription() {
        if (!mAccessibilityDescriptionEnabled) {
            return;
        }

        this.setContentDescription(String.valueOf(getValue()));
    }

    /**
     * Changes the current value by one which is increment or
     * decrement based on the passes argument.
     * decrement the current value.
     *
     * @param increment True to increment, false to decrement.
     */
    private void changeValueByOne(boolean increment) {
        if (!moveToFinalScrollerPosition(mFlingScroller)) {
            moveToFinalScrollerPosition(mAdjustScroller);
        }
        smoothScroll(increment, 1);
    }

    /**
     * Starts a smooth scroll
     *
     * @param increment True to increment, false to decrement.
     * @param steps     The steps to scroll.
     */
    public void smoothScroll(boolean increment, int steps) {
        final int diffSteps = (increment ? -mSelectorElementSize : mSelectorElementSize) * steps;
        if (isHorizontalMode()) {
            mPreviousScrollerX = 0;
            mFlingScroller.startScroll(0, 0, diffSteps, 0, SNAP_SCROLL_DURATION);
        } else {
            mPreviousScrollerY = 0;
            mFlingScroller.startScroll(0, 0, 0, diffSteps, SNAP_SCROLL_DURATION);
        }
        invalidate();
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = getSelectorIndices();
        int totalTextSize = (int) ((selectorIndices.length - 1) * mTextSize + mSelectedTextSize);
        float textGapCount = selectorIndices.length;
        if (isHorizontalMode()) {
            float totalTextGapWidth = (getRight() - getLeft()) - totalTextSize;
            int mSelectorTextGapWidth = (int) (totalTextGapWidth / textGapCount);
            mSelectorElementSize = (int) getMaxTextSize() + mSelectorTextGapWidth;
            mInitialScrollOffset = (int) (mSelectedTextCenterX - mSelectorElementSize * mWheelMiddleItemIndex);
        } else {
            float totalTextGapHeight = (getBottom() - getTop()) - totalTextSize;
            mSelectorTextGapHeight = (int) (totalTextGapHeight / textGapCount);
            mSelectorElementSize = (int) getMaxTextSize() + mSelectorTextGapHeight;
            mInitialScrollOffset = (int) (mSelectedTextCenterY - mSelectorElementSize * mWheelMiddleItemIndex);
        }
        mCurrentScrollOffset = mInitialScrollOffset;
        updateInputTextView();
    }

    private void initializeFadingEdges() {
        if (isHorizontalMode()) {
            setHorizontalFadingEdgeEnabled(true);
            setVerticalFadingEdgeEnabled(false);
            setFadingEdgeLength((getRight() - getLeft() - (int) mTextSize) / 2);
        } else {
            setHorizontalFadingEdgeEnabled(false);
            setVerticalFadingEdgeEnabled(true);
            setFadingEdgeLength((getBottom() - getTop() - (int) mTextSize) / 2);
        }
    }

    /**
     * Callback invoked upon completion of a given <code>scroller</code>.
     */
    private void onScrollerFinished(Scroller scroller) {
        if (scroller == mFlingScroller) {
            ensureScrollWheelAdjusted();
            updateInputTextView();
            onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
        } else if (mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
            updateInputTextView();
        }
    }

    /**
     * Handles transition to a given <code>scrollState</code>
     */
    private void onScrollStateChange(int scrollState) {
        if (mScrollState == scrollState) {
            return;
        }
        mScrollState = scrollState;
    }

    /**
     * Flings the selector with the given <code>velocity</code>.
     */
    private void fling(int velocity) {
        if (isHorizontalMode()) {
            mPreviousScrollerX = 0;
            if (velocity > 0) {
                mFlingScroller.fling(0, 0, velocity, 0, 0, Integer.MAX_VALUE, 0, 0);
            } else {
                mFlingScroller.fling(Integer.MAX_VALUE, 0, velocity, 0, 0, Integer.MAX_VALUE, 0, 0);
            }
        } else {
            mPreviousScrollerY = 0;
            if (velocity > 0) {
                mFlingScroller.fling(0, 0, 0, velocity, 0, 0, 0, Integer.MAX_VALUE);
            } else {
                mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocity, 0, 0, 0, Integer.MAX_VALUE);
            }
        }

        invalidate();
    }

    /**
     * @return The wrapped index <code>selectorIndex</code> value.
     */
    private int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > mMaxValue) {
            return mMinValue + (selectorIndex - mMaxValue) % (mMaxValue - mMinValue) - 1;
        } else if (selectorIndex < mMinValue) {
            return mMaxValue - (mMinValue - selectorIndex) % (mMaxValue - mMinValue) + 1;
        }
        return selectorIndex;
    }

    private int[] getSelectorIndices() {
        return mSelectorIndices;
    }

    /**
     * Increments the <code>selectorIndices</code> whose string representations
     * will be displayed in the selector.
     */
    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
            nextScrollSelectorIndex = mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    /**
     * Decrements the <code>selectorIndices</code> whose string representations
     * will be displayed in the selector.
     */
    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
            nextScrollSelectorIndex = mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }

    /**
     * Ensures we have a cached string representation of the given <code>
     * selectorIndex</code> to avoid multiple instantiations of the same string.
     */
    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = mSelectorIndexToStringCache;
        String scrollSelectorValue = cache.get(selectorIndex);
        if (scrollSelectorValue != null) {
            return;
        }
        if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
            scrollSelectorValue = "";
        } else {
            if (mDisplayedValues != null) {
                int displayedValueIndex = selectorIndex - mMinValue;
                if (displayedValueIndex >= mDisplayedValues.length) {
                    cache.remove(selectorIndex);
                    return;
                }
                scrollSelectorValue = mDisplayedValues[displayedValueIndex];
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
        }
        cache.put(selectorIndex, scrollSelectorValue);
    }

    private String formatNumber(int value) {
        return (mFormatter != null) ? mFormatter.format(value) : formatNumberWithLocale(value);
    }

    /**
     * Updates the view of this NumberPicker. If displayValues were specified in
     * the string corresponding to the index specified by the current value will
     * be returned. Otherwise, the formatter specified in {@link #setFormatter}
     * will be used to format the number.
     */
    private void updateInputTextView() {
        /*
         * If we don't have displayed values then use the current number else
         * find the correct value in the displayed values for the current
         * number.
         */
        String text = (mDisplayedValues == null) ? formatNumber(mValue)
                : mDisplayedValues[mValue - mMinValue];
        if (TextUtils.isEmpty(text)) {
            return;
        }

        CharSequence beforeText = mSelectedText.getText();
        if (text.equals(beforeText.toString())) {
            return;
        }

        mSelectedText.setText(text);
    }

    /**
     * Notifies the listener, if registered, of a change of the value of this
     * NumberPicker.
     */
    private void notifyChange(int previous, int current) {
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChange(this, previous, current);
        }
    }

    /**
     * Posts a command for changing the current value by one.
     *
     * @param increment Whether to increment or decrement the value.
     */
    private void postChangeCurrentByOneFromLongPress(boolean increment, long delayMillis) {
        if (mChangeCurrentByOneFromLongPressCommand == null) {
            mChangeCurrentByOneFromLongPressCommand = new ChangeCurrentByOneFromLongPressCommand();
        } else {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
        mChangeCurrentByOneFromLongPressCommand.setStep(increment);
        postDelayed(mChangeCurrentByOneFromLongPressCommand, delayMillis);
    }

    /**
     * Posts a command for changing the current value by one.
     *
     * @param increment Whether to increment or decrement the value.
     */
    private void postChangeCurrentByOneFromLongPress(boolean increment) {
        postChangeCurrentByOneFromLongPress(increment, ViewConfiguration.getLongPressTimeout());
    }

    /**
     * Removes the command for changing the current value by one.
     */
    private void removeChangeCurrentByOneFromLongPress() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
    }

    /**
     * Removes all pending callback from the message queue.
     */
    private void removeAllCallbacks() {
        if (mChangeCurrentByOneFromLongPressCommand != null) {
            removeCallbacks(mChangeCurrentByOneFromLongPressCommand);
        }
    }

    /**
     * Ensures that the scroll wheel is adjusted i.e. there is no offset and the
     * middle element is in the middle of the widget.
     */
    private void ensureScrollWheelAdjusted() {
        // adjust to the closest value
        int delta = mInitialScrollOffset - mCurrentScrollOffset;
        if (delta == 0) {
            return;
        }

        if (Math.abs(delta) > mSelectorElementSize / 2) {
            delta += (delta > 0) ? -mSelectorElementSize : mSelectorElementSize;
        }
        if (isHorizontalMode()) {
            mPreviousScrollerX = 0;
            mAdjustScroller.startScroll(0, 0, delta, 0, SELECTOR_ADJUSTMENT_DURATION_MILLIS);
        } else {
            mPreviousScrollerY = 0;
            mAdjustScroller.startScroll(0, 0, 0, delta, SELECTOR_ADJUSTMENT_DURATION_MILLIS);
        }
        invalidate();
    }

    /**
     * Command for changing the current value from a long press by one.
     */
    class ChangeCurrentByOneFromLongPressCommand implements Runnable {
        private boolean mIncrement;

        private void setStep(boolean increment) {
            mIncrement = increment;
        }

        @Override
        public void run() {
            changeValueByOne(mIncrement);
            postDelayed(this, DEFAULT_LONG_PRESS_UPDATE_INTERVAL);
        }
    }

    private String formatNumberWithLocale(int value) {
        return mNumberFormatter.format(value);
    }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private float spToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }

    private float pxToSp(float px) {
        return px / getResources().getDisplayMetrics().scaledDensity;
    }

    private Formatter stringToFormatter(final String formatter) {
        if (TextUtils.isEmpty(formatter)) {
            return null;
        }

        return new Formatter() {
            @Override
            public String format(int i) {
                return String.format(Locale.getDefault(), formatter, i);
            }
        };
    }

    private void setWidthAndHeight() {
        if (isHorizontalMode()) {
            mMinHeight = SIZE_UNSPECIFIED;
            mMaxHeight = (int) dpToPx(DEFAULT_MIN_WIDTH);
            mMinWidth = (int) dpToPx(DEFAULT_MAX_HEIGHT);
            mMaxWidth = SIZE_UNSPECIFIED;
        } else {
            mMinHeight = SIZE_UNSPECIFIED;
            mMaxHeight = (int) dpToPx(DEFAULT_MAX_HEIGHT);
            mMinWidth = (int) dpToPx(DEFAULT_MIN_WIDTH);
            mMaxWidth = SIZE_UNSPECIFIED;
        }
    }

    public void setDividerColor(@ColorInt int color) {
        mDividerColor = color;
        mDividerDrawable = new ColorDrawable(color);
    }

    public void setOrientation(@Orientation int orientation) {
        mOrientation = orientation;
        setWidthAndHeight();
        requestLayout();
    }

    public void setWheelItemCount(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Wheel item count must be >= 1");
        }
        mRealWheelItemCount = count;
        mWheelItemCount = Math.max(count, DEFAULT_WHEEL_ITEM_COUNT);
        mWheelMiddleItemIndex = mWheelItemCount / 2;
        mSelectorIndices = new int[mWheelItemCount];
    }

    public void setSelectedTextColor(@ColorInt int color) {
        mSelectedTextColor = color;
        mSelectedText.setTextColor(mSelectedTextColor);
    }

    public void setSelectedTextSize(float textSize) {
        mSelectedTextSize = textSize;
        mSelectedText.setTextSize(pxToSp(mSelectedTextSize));
    }

    public void setSelectedTypeface(Typeface typeface) {
        mSelectedTypeface = typeface;
        if (mSelectedTypeface != null) {
            mSelectorWheelPaint.setTypeface(mSelectedTypeface);
        } else if (mTypeface != null) {
            mSelectorWheelPaint.setTypeface(mTypeface);
        } else {
            mSelectorWheelPaint.setTypeface(Typeface.MONOSPACE);
        }
    }

    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
        mSelectorWheelPaint.setColor(mTextColor);
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
        mSelectorWheelPaint.setTextSize(mTextSize);
    }

    public void setTextSize(@DimenRes int dimenId) {
        setTextSize(getResources().getDimension(dimenId));
    }

    public void setTypeface(Typeface typeface) {
        mTypeface = typeface;
        if (mTypeface != null) {
            mSelectedText.setTypeface(mTypeface);
            setSelectedTypeface(mSelectedTypeface);
        } else {
            mSelectedText.setTypeface(Typeface.MONOSPACE);
        }
    }

    public boolean isHorizontalMode() {
        return getOrientation() == HORIZONTAL;
    }

    public boolean isAscendingOrder() {
        return getOrder() == ASCENDING;
    }

    public int getOrder() {
        return mOrder;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public boolean isScrollerEnabled() {
        return mScrollerEnabled;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public float getTextSize() {
        return spToPx(mTextSize);
    }
}
