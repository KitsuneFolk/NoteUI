package com.pandacorp.noteui.presentation.ui.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.fragula2.animation.SwipeController
import com.fragula2.utils.findSwipeController
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.ActivityMainBinding
import com.pandacorp.noteui.presentation.utils.helpers.PreferenceHandler
import com.pandacorp.noteui.presentation.utils.themes.Theme
import com.pandacorp.noteui.presentation.utils.themes.ViewHelper
import com.pandacorp.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ThemeActivity() {
    private var fragulaNavController: NavController? = null
    private var swipeController: SwipeController? = null
    var navHostFragment: NavHostFragment? = null

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable -> throw (throwable) } // Throw uncaught exceptions
        PreferenceHandler.setLanguage(this)
        setTheme(R.style.DarkTheme)
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    override fun onDestroy() {
        _binding = null
        fragulaNavController = null
        swipeController = null
        super.onDestroy()
    }

    override fun syncTheme(appTheme: AppTheme) {
        appTheme as Theme
        appTheme.changeStatusbarColor(this)
        appTheme.changeNavigationBarColor(this)
        window.decorView.background = ColorDrawable(appTheme.getColorBackground(this))
    }

    override fun getStartTheme(): AppTheme {
        return ViewHelper.currentTheme
    }

    private fun initViews() {
        binding.fragulaNavHostFragment.getFragment<NavHostFragment>().apply {
            swipeController = findSwipeController()
            fragulaNavController = navController
            navHostFragment = this
        }
    }
}