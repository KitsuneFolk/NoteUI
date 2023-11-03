package com.pandacorp.noteui.presentation.ui.activity

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.dolatkia.animatedThemeManager.AppTheme
import com.dolatkia.animatedThemeManager.ThemeActivity
import com.fragula2.animation.SwipeController
import com.fragula2.utils.findSwipeController
import com.pandacorp.noteui.app.R
import com.pandacorp.noteui.app.databinding.ActivityMainBinding
import com.pandacorp.noteui.presentation.ui.screen.MainScreen
import com.pandacorp.noteui.presentation.utils.helpers.PreferenceHandler
import com.pandacorp.noteui.presentation.utils.themes.Theme
import com.pandacorp.noteui.presentation.utils.themes.ViewHelper
import com.pandacorp.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ThemeActivity() {
    private var fragulaNavController: NavController? = null
    private var swipeController: SwipeController? = null

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!
    var mainScreen: MainScreen? = null

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
    }

    override fun getStartTheme(): AppTheme {
        return ViewHelper.currentTheme
    }

    private fun initViews() {
        binding.fragulaNavHostFragment.getFragment<NavHostFragment>().apply {
            swipeController = findSwipeController()
            fragulaNavController = navController
            val swipeBackFragment = childFragmentManager.fragments.first()
            swipeBackFragment.childFragmentManager.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentViewCreated(
                        fm: FragmentManager,
                        f: Fragment,
                        v: View,
                        savedInstanceState: Bundle?
                    ) {
                        super.onFragmentViewCreated(fm, f, v, savedInstanceState)
                        if (f is MainScreen) {
                            mainScreen = f
                        }
                    }
                },
                false,
            )
        }
    }
}