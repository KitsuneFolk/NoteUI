package com.pandacorp.searchbar.searchview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.Px
import androidx.annotation.StyleRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout.AttachedBehavior
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.TextViewCompat
import androidx.customview.view.AbsSavedState
import com.google.android.material.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.color.MaterialColors
import com.google.android.material.elevation.ElevationOverlayProvider
import com.google.android.material.internal.ClippableRoundedCornerLayout
import com.google.android.material.internal.ContextUtils
import com.google.android.material.internal.FadeThroughDrawable
import com.google.android.material.internal.ThemeEnforcement
import com.google.android.material.internal.ToolbarUtils
import com.google.android.material.internal.TouchObserverFrameLayout
import com.google.android.material.internal.ViewUtils
import com.google.android.material.internal.ViewUtils.RelativePadding
import com.google.android.material.shape.MaterialShapeUtils
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.pandacorp.searchbar.SearchBar
import com.pandacorp.searchbar.left
import com.pandacorp.searchbar.right
import com.pandacorp.searchbar.top

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

@SuppressLint("RestrictedApi", "PrivateResource")
class SearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.materialSearchViewStyle
) : FrameLayout(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr),
    AttachedBehavior {
    val scrim: View
    val rootView: ClippableRoundedCornerLayout
    private val backgroundView: View?
    private val statusBarSpacer: View
    val headerContainer: FrameLayout
    val toolbarContainer: FrameLayout
    val toolbar: MaterialToolbar
    val dummyToolbar: Toolbar
    val searchPrefix: TextView

    /**
     * Returns the main [EditText] which can be used for hint and search text.
     */
    val editText: EditText
    val clearButton: ImageButton
    val divider: View
    val contentContainer: TouchObserverFrameLayout
    private val layoutInflated: Boolean
    private val searchViewAnimationHelper: SearchViewAnimationHelper
    private val elevationOverlayProvider: ElevationOverlayProvider?
    private val transitionListeners: MutableSet<TransitionListener> = LinkedHashSet()

    /**
     * Returns whether the navigation icon should be animated from the [SearchBar] to [SearchView].
     */
    val isAnimatedNavigationIcon: Boolean

    /**
     * Returns whether the menu items should be animated from the [SearchBar] to [SearchView].
     */
    val isMenuItemsAnimated: Boolean
    private val autoShowKeyboard: Boolean
    private var searchBar: SearchBar? = null

    @Suppress("DEPRECATION")
    private var softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
    private var useWindowInsetsController = false
    private var statusBarSpacerEnabledOverride = false
    private var currentTransitionState = TransitionState.HIDDEN
    private var childImportantForAccessibilityMap: MutableMap<View, Int>? = null

    init {
        // Ensure we are using the correctly themed context rather than the context that was passed in.
        val themedContext = getContext()
        val a =
            ThemeEnforcement.obtainStyledAttributes(
                themedContext,
                attrs,
                R.styleable.SearchView,
                defStyleAttr,
                DEF_STYLE_RES,
            )
        val headerLayoutResId = a.getResourceId(R.styleable.SearchView_headerLayout, -1)
        val textAppearanceResId = a.getResourceId(R.styleable.SearchView_android_textAppearance, -1)
        val text = a.getString(R.styleable.SearchView_android_text)
        val hint = a.getString(R.styleable.SearchView_android_hint)
        val searchPrefixText = a.getString(R.styleable.SearchView_searchPrefixText)
        val useDrawerArrowDrawable = a.getBoolean(R.styleable.SearchView_useDrawerArrowDrawable, false)
        isAnimatedNavigationIcon = a.getBoolean(R.styleable.SearchView_animateNavigationIcon, true)
        isMenuItemsAnimated = a.getBoolean(R.styleable.SearchView_animateMenuItems, true)
        val hideNavigationIcon = a.getBoolean(R.styleable.SearchView_hideNavigationIcon, false)
        autoShowKeyboard = a.getBoolean(R.styleable.SearchView_autoShowKeyboard, true)
        a.recycle()
        LayoutInflater.from(themedContext).inflate(R.layout.mtrl_search_view, this)
        layoutInflated = true
        scrim = findViewById(R.id.open_search_view_scrim)
        rootView = findViewById(R.id.open_search_view_root)
        backgroundView = findViewById(R.id.open_search_view_background)
        statusBarSpacer = findViewById(R.id.open_search_view_status_bar_spacer)
        headerContainer = findViewById(R.id.open_search_view_header_container)
        toolbarContainer = findViewById(R.id.open_search_view_toolbar_container)
        toolbar = findViewById(R.id.open_search_view_toolbar)
        dummyToolbar = findViewById(R.id.open_search_view_dummy_toolbar)
        searchPrefix = findViewById(R.id.open_search_view_search_prefix)
        editText = findViewById(R.id.open_search_view_edit_text)
        clearButton = findViewById(R.id.open_search_view_clear_button)
        divider = findViewById(R.id.open_search_view_divider)
        contentContainer = findViewById(R.id.open_search_view_content_container)
        searchViewAnimationHelper = SearchViewAnimationHelper(this)
        elevationOverlayProvider = ElevationOverlayProvider(themedContext)
        setUpRootView()
        setUpBackgroundViewElevationOverlay()
        setUpHeaderLayout(headerLayoutResId)
        setSearchPrefixText(searchPrefixText)
        setUpEditText(textAppearanceResId, text, hint)
        setUpBackButton(useDrawerArrowDrawable, hideNavigationIcon)
        setUpClearButton()
        setUpContentOnTouchListener()
        setUpInsetListeners()
    }

    override fun addView(
        child: View,
        index: Int,
        params: ViewGroup.LayoutParams
    ) {
        if (layoutInflated) {
            contentContainer.addView(child, index, params)
        } else {
            super.addView(child, index, params)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        updateSoftInputMode()
    }

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)
        setUpBackgroundViewElevationOverlay(elevation)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        MaterialShapeUtils.setParentAbsoluteElevation(this)
    }

    override fun getBehavior(): CoordinatorLayout.Behavior<SearchView> {
        return Behavior()
    }

    private val activityWindow: Window?
        get() {
            val activity = ContextUtils.getActivity(context)
            return activity?.window
        }

    @SuppressLint("ClickableViewAccessibility") // Will be handled by accessibility delegate.
    private fun setUpRootView() {
        rootView.setOnTouchListener { _: View?, _: MotionEvent? -> true }
    }

    private fun setUpBackgroundViewElevationOverlay() {
        setUpBackgroundViewElevationOverlay(overlayElevation)
    }

    private fun setUpBackgroundViewElevationOverlay(elevation: Float) {
        if (elevationOverlayProvider == null || backgroundView == null) {
            return
        }
        val tv = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, tv, true)
        val backgroundColor = elevationOverlayProvider.compositeOverlayIfNeeded(tv.data, elevation)
        backgroundView.setBackgroundColor(backgroundColor)
    }

    private val overlayElevation: Float
        get() =
            if (searchBar != null) {
                searchBar!!.compatElevation
            } else {
                resources.getDimension(R.dimen.m3_searchview_elevation)
            }

    private fun setUpHeaderLayout(headerLayoutResId: Int) {
        if (headerLayoutResId != -1) {
            val headerView = LayoutInflater.from(context).inflate(headerLayoutResId, headerContainer, false)
            addHeaderView(headerView)
        }
    }

    private fun setUpEditText(
        @StyleRes textAppearanceResId: Int,
        text: String?,
        hint: String?
    ) {
        if (textAppearanceResId != -1) {
            TextViewCompat.setTextAppearance(editText, textAppearanceResId)
        }
        editText.setText(text)
        editText.hint = hint
    }

    private fun setUpBackButton(
        useDrawerArrowDrawable: Boolean,
        hideNavigationIcon: Boolean
    ) {
        if (hideNavigationIcon) {
            toolbar.navigationIcon = null
            return
        }
        toolbar.setNavigationOnClickListener { hide() }
        if (useDrawerArrowDrawable) {
            val drawerArrowDrawable = DrawerArrowDrawable(context)
            drawerArrowDrawable.color = MaterialColors.getColor(this, R.attr.colorOnSurface)
            toolbar.navigationIcon = drawerArrowDrawable
        }
    }

    private fun setUpClearButton() {
        clearButton.setOnClickListener {
            editText.setText("")
            requestFocusAndShowKeyboardIfNeeded()
        }
        editText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                    clearButton.visibility = if (s.isNotEmpty()) VISIBLE else GONE
                }

                override fun afterTextChanged(s: Editable) {}
            },
        )
    }

    @SuppressLint("ClickableViewAccessibility") // Will be handled by accessibility delegate.
    private fun setUpContentOnTouchListener() {
        contentContainer.setOnTouchListener { _: View?, _: MotionEvent? ->
            if (isAdjustNothingSoftInputMode) {
                clearFocusAndHideKeyboard()
            }
            false
        }
    }

    private fun setUpStatusBarSpacer(
        @Px height: Int
    ) {
        if (statusBarSpacer.layoutParams.height != height) {
            statusBarSpacer.layoutParams.height = height
            statusBarSpacer.requestLayout()
        }
    }

    @get:Px
    private val statusBarHeight: Int
        get() {
            @SuppressLint(
                "DiscouragedApi",
                "InternalInsetResource",
            )
            val resourceId = // Used for initial value. A WindowInsetsListener will apply correct insets later.
                resources.getIdentifier("status_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else {
                0
            }
        }

    /**
     * Note: DrawerArrowDrawable supports RTL, so there is no need to update the navigation icon for
     * RTL if it is a DrawerArrowDrawable.
     */
    private fun updateNavigationIconIfNeeded() {
        if (isNavigationIconDrawerArrowDrawable(toolbar)) {
            return
        }
        val navigationIcon = R.drawable.ic_arrow_back_black_24
        if (searchBar == null) {
            toolbar.setNavigationIcon(navigationIcon)
        } else {
            val navigationIconDrawable =
                DrawableCompat.wrap(
                    AppCompatResources.getDrawable(context, navigationIcon)!!.mutate(),
                )
            if (toolbar.navigationIconTint != null) {
                DrawableCompat.setTint(navigationIconDrawable, toolbar.navigationIconTint!!)
            }
            toolbar.navigationIcon =
                FadeThroughDrawable(
                    searchBar!!.navigationIcon!!,
                    navigationIconDrawable,
                )
            updateNavigationIconProgressIfNeeded()
        }
    }

    private fun isNavigationIconDrawerArrowDrawable(toolbar: Toolbar): Boolean {
        return DrawableCompat.unwrap<Drawable>(toolbar.navigationIcon!!) is DrawerArrowDrawable
    }

    /**
     * Listens to [WindowInsetsCompat] and adjusts layouts accordingly.
     *
     * **NOTE**: window insets are only delivered if no other layout consumed them before. E.g.:
     *
     *  by declaring `fitsSystemWindows=true`
     *  by consuming insets via specific consume-methods (e.g [WindowInsetsCompat.consumeSystemWindowInsets])
     *
     */
    private fun setUpInsetListeners() {
        setUpToolbarInsetListener()
        setUpDividerInsetListener()
        setUpStatusBarSpacerInsetListener()
    }

    private fun setUpToolbarInsetListener() {
        ViewUtils.doOnApplyWindowInsets(
            toolbar,
        ) { _: View?, insets: WindowInsetsCompat, initialPadding: RelativePadding ->
            val isRtl = ViewUtils.isLayoutRtl(toolbar)
            val paddingLeft = if (isRtl) initialPadding.end else initialPadding.start
            val paddingRight = if (isRtl) initialPadding.start else initialPadding.end
            toolbar.setPadding(
                paddingLeft + insets.left(),
                initialPadding.top,
                paddingRight + insets.right(),
                initialPadding.bottom,
            )
            insets
        }
    }

    private fun setUpStatusBarSpacerInsetListener() {
        // Set an initial height based on the default system value to support pre-L behavior.
        setUpStatusBarSpacer(statusBarHeight)

        // Listen to system window insets on L+ and adjusts status bar height based on the top inset.
        ViewCompat.setOnApplyWindowInsetsListener(
            statusBarSpacer,
        ) { _: View?, insets: WindowInsetsCompat ->
            val systemWindowInsetTop = insets.top()
            setUpStatusBarSpacer(systemWindowInsetTop)
            if (!statusBarSpacerEnabledOverride) {
                setStatusBarSpacerEnabledInternal(systemWindowInsetTop > 0)
            }
            insets
        }
    }

    private fun setUpDividerInsetListener() {
        val layoutParams = divider.layoutParams as MarginLayoutParams
        val leftMargin = layoutParams.leftMargin
        val rightMargin = layoutParams.rightMargin
        ViewCompat.setOnApplyWindowInsetsListener(
            divider,
        ) { _: View?, insets: WindowInsetsCompat ->
            layoutParams.leftMargin = leftMargin + insets.left()
            layoutParams.rightMargin = rightMargin + insets.right()
            insets
        }
    }

    val isSetupWithSearchBar: Boolean
        /**
         * Returns whether or not this [SearchView] is set up with an [SearchBar].
         */
        get() = searchBar != null

    /**
     * Sets up this [SearchView] with an [SearchBar], which will result in the
     * [SearchView] being shown when the [SearchBar] is clicked. This behavior will be set up
     * automatically if the [SearchBar] and [SearchView] are in a
     * [CoordinatorLayout] and the [SearchView] is anchored to the [SearchBar].
     */
    fun setupWithSearchBar(searchBar: SearchBar?) {
        this.searchBar = searchBar
        searchViewAnimationHelper.setSearchBar(searchBar)
        searchBar?.setOnClickListener {
            if (!searchBar.isCountModeEnabled) {
                show()
            }
        }
        updateNavigationIconIfNeeded()
        setUpBackgroundViewElevationOverlay()
    }

    fun setAnimationDuration(
        showDurationMs: Long,
        hideDurationMs: Long
    ) {
        searchViewAnimationHelper.setAnimationDuration(showDurationMs, hideDurationMs)
    }

    /**
     * Add a header view to this [SearchView], which will be placed above the search text area.
     *
     * Note: due to complications with the expand/collapse animation, a header view is intended to
     * be used with a standalone [SearchView] which slides up/down instead of morphing from an
     * [SearchBar].
     */
    private fun addHeaderView(headerView: View) {
        headerContainer.addView(headerView)
        headerContainer.visibility = VISIBLE
    }

    /**
     * Adds a listener to handle [SearchView] transitions such as showing and closing.
     */
    fun addTransitionListener(transitionListener: TransitionListener) {
        transitionListeners.add(transitionListener)
    }

    /**
     * Sets the search prefix text.
     */
    private fun setSearchPrefixText(searchPrefixText: CharSequence?) {
        searchPrefix.text = searchPrefixText
        searchPrefix.visibility =
            if (TextUtils.isEmpty(searchPrefixText)) GONE else VISIBLE
    }

    @get:SuppressLint("KotlinPropertyAccess")
    @set:SuppressLint("KotlinPropertyAccess")
    var text: Editable?
        /**
         * Returns the text of main [EditText], which usually represents the search text.
         */
        get() = editText.text

        /**
         * Sets the text of main [EditText].
         */
        set(text) {
            editText.text = text
        }

    /**
     * Sets the text of main [EditText].
     */
    private fun setText(text: CharSequence?) {
        editText.setText(text)
    }

    /**
     * Sets the soft input mode for this [SearchView]. This is important because the [SearchView]
     * will use this to determine whether the keyboard should be shown/hidden at the same
     * time as the expand/collapse animation, or if the keyboard should be staggered with the
     * animation to avoid glitchiness due to a resize of the screen. This will be set automatically by
     * the [SearchView] during initial render but make sure to invoke this if you are changing
     * the soft input mode at runtime.
     */
    private fun updateSoftInputMode() {
        val window = activityWindow
        if (window != null) {
            softInputMode = window.attributes.softInputMode
        }
    }

    private fun setStatusBarSpacerEnabledInternal(enabled: Boolean) {
        statusBarSpacer.visibility = if (enabled) VISIBLE else GONE
    }

    fun setTransitionState(state: TransitionState) {
        if (currentTransitionState == state) {
            return
        }
        val previousState = currentTransitionState
        currentTransitionState = state
        val listeners: Set<TransitionListener> = LinkedHashSet(transitionListeners)
        for (listener in listeners) {
            listener.onStateChanged(this, previousState, state)
        }
    }

    /**
     * Shows the [SearchView] with an animation.
     *
     *
     * Note: the show animation will not be started if the [SearchView] is currently shown or
     * showing.
     */
    fun show() {
        if (currentTransitionState == TransitionState.SHOWN || currentTransitionState == TransitionState.SHOWING) {
            return
        }
        searchViewAnimationHelper.show()
        setModalForAccessibility(true)
    }

    /**
     * Hides the [SearchView] with an animation.
     *
     *
     * Note: the hide animation will not be started if the [SearchView] is currently hidden
     * or hiding.
     */
    fun hide() {
        if (currentTransitionState == TransitionState.HIDDEN || currentTransitionState == TransitionState.HIDING) {
            return
        }
        searchViewAnimationHelper.hide()
        setModalForAccessibility(false)
    }

    /**
     * Updates the visibility of the [SearchView] without an animation.
     */
    private fun setVisible(visible: Boolean) {
        val wasVisible = rootView.visibility == VISIBLE
        rootView.visibility = if (visible) VISIBLE else GONE
        updateNavigationIconProgressIfNeeded()
        if (wasVisible != visible) {
            setModalForAccessibility(visible)
        }
        setTransitionState(if (visible) TransitionState.SHOWN else TransitionState.HIDDEN)
    }

    private fun updateNavigationIconProgressIfNeeded() {
        val backButton = ToolbarUtils.getNavigationIconButton(toolbar) ?: return
        val progress = if (rootView.visibility == VISIBLE) 1 else 0
        val drawable = DrawableCompat.unwrap<Drawable>(backButton.drawable)
        if (drawable is DrawerArrowDrawable) {
            drawable.progress = progress.toFloat()
        }
        if (drawable is FadeThroughDrawable) {
            drawable.setProgress(progress.toFloat())
        }
    }

    /**
     * Requests focus on the main [EditText] and shows the soft keyboard if automatic showing of
     * the keyboard is enabled.
     */
    fun requestFocusAndShowKeyboardIfNeeded() {
        if (autoShowKeyboard) {
            requestFocusAndShowKeyboard()
        }
    }

    /**
     * Requests focus on the main [EditText] and shows the soft keyboard.
     */
    private fun requestFocusAndShowKeyboard() {
        // Without a delay requesting focus on edit text fails when talkback is active.
        editText.postDelayed(
            {
                if (editText.requestFocus()) {
                    // Workaround for talkback issue when clear button is clicked
                    editText.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                }
                ViewUtils.showKeyboard(editText, useWindowInsetsController)
            },
            TALKBACK_FOCUS_CHANGE_DELAY_MS,
        )
    }

    /**
     * Clears focus on the main [EditText] and hides the soft keyboard.
     */
    fun clearFocusAndHideKeyboard() {
        editText.post {
            editText.clearFocus()
            if (searchBar != null) {
                searchBar!!.requestFocus()
            }
            ViewUtils.hideKeyboard(editText, useWindowInsetsController)
        }
    }

    val isAdjustNothingSoftInputMode: Boolean
        get() = softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING

    /**
     * Sets whether the [SearchView] is modal for accessibility, i.e., whether views that are
     * not nested within the [SearchView] are important for accessibility.
     */
    private fun setModalForAccessibility(isSearchViewModal: Boolean) {
        val rootView = getRootView() as ViewGroup
        if (isSearchViewModal) {
            childImportantForAccessibilityMap = HashMap(rootView.childCount)
        }
        updateChildImportantForAccessibility(rootView, isSearchViewModal)
        if (!isSearchViewModal) {
            // When SearchView is not modal, reset the important for accessibility map.
            childImportantForAccessibilityMap = null
        }
    }

    @SuppressLint("InlinedApi") // View Compat will handle the differences.
    private fun updateChildImportantForAccessibility(parent: ViewGroup, isSearchViewModal: Boolean) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (child === this) {
                continue
            }
            if (child.findViewById<View?>(rootView.id) != null) {
                // If this child node contains SearchView, look at this node's children instead.
                updateChildImportantForAccessibility(child as ViewGroup, isSearchViewModal)
                continue
            }
            if (!isSearchViewModal) {
                if (childImportantForAccessibilityMap != null &&
                    childImportantForAccessibilityMap!!.containsKey(child)
                ) {
                    // Restores the original important for accessibility value of the child view.
                    ViewCompat.setImportantForAccessibility(
                        child,
                        childImportantForAccessibilityMap!![child]!!,
                    )
                }
            } else {
                // Saves the important for accessibility value of the child view.
                childImportantForAccessibilityMap!![child] = child.importantForAccessibility
                ViewCompat.setImportantForAccessibility(
                    child,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS,
                )
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val savedState = SavedState(super.onSaveInstanceState())
        val text: CharSequence? = text
        savedState.text = text?.toString()
        savedState.visibility = rootView.visibility
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        super.onRestoreInstanceState(state.superState)
        setText(state.text)
        setVisible(state.visibility == VISIBLE)
    }

    /**
     * Enum that defines the possible transition states of an [SearchView].
     */
    enum class TransitionState {
        HIDING,
        HIDDEN,
        SHOWING,
        SHOWN
    }

    /**
     * Callback interface that provides important transition events for a [SearchView].
     */
    fun interface TransitionListener {
        /**
         * Called when the given [SearchView] transition state has changed.
         */
        fun onStateChanged(
            searchView: SearchView,
            previousState: TransitionState,
            newState: TransitionState
        )
    }

    /**
     * Behavior that sets up an [SearchView] with an [SearchBar].
     */
    class Behavior : CoordinatorLayout.Behavior<SearchView>() {
        override fun onDependentViewChanged(
            parent: CoordinatorLayout,
            child: SearchView,
            dependency: View
        ): Boolean {
            if (!child.isSetupWithSearchBar && dependency is SearchBar) {
                child.setupWithSearchBar(dependency)
            }
            return false
        }
    }

    internal class SavedState(superState: Parcelable?) : AbsSavedState(superState!!) {
        var text: String? = null
        var visibility = 0

        override fun writeToParcel(
            dest: Parcel,
            flags: Int
        ) {
            super.writeToParcel(dest, flags)
            dest.writeString(text)
            dest.writeInt(visibility)
        }
    }

    companion object {
        private const val TALKBACK_FOCUS_CHANGE_DELAY_MS: Long = 100
        private val DEF_STYLE_RES = R.style.Widget_Material3_SearchView
    }
}