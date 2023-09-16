package com.pandacorp.noteui.presentation.ui.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.fragula2.animation.SwipeController
import com.fragula2.utils.findSwipeController
import com.pandacorp.noteui.app.databinding.ActivityMainBinding
import com.pandacorp.noteui.presentation.utils.helpers.PreferenceHandler
import com.pandacorp.noteui.presentation.utils.views.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {
    private var fragulaNavController: NavController? = null
    private var swipeController: SwipeController? = null

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        Thread.setDefaultUncaughtExceptionHandler { _, throwable -> throw (throwable) } // Throw uncaught exceptions
        PreferenceHandler.setLanguage(this)
        super.onCreate(savedInstanceState)
        PreferenceHandler.setTheme(this)
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

    private fun initViews() {
        binding.fragulaNavHostFragment.getFragment<NavHostFragment>().apply {
            swipeController = findSwipeController()
            fragulaNavController = navController
        }
    }
}