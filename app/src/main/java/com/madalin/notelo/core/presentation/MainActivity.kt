package com.madalin.notelo.core.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.madalin.notelo.R
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.core.presentation.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.core.presentation.util.ThemeState
import com.madalin.notelo.databinding.ActivityMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeState.setModeFromPreferences(this)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater) // binds this activity's views
        setContentView(binding.root) // setContentView(R.layout.activity_main)
        edgeToEdge(this)

        navController = (supportFragmentManager.findFragmentById(binding.mainActivityFragmentContainerView.id) as NavHostFragment).navController

        setupObservers()
    }

    private fun setupObservers() {
        // sign in state observer
        viewModel.isSignedIn.observe(this) {
            if (it) {
                // navigates to MainFragment if the user has singed in
                navController.navigate(R.id.homeFragment)
            } else {
                // otherwise to SignInFragment if elsewhere
                if (navController.currentDestination?.id != R.id.signInFragment) {
                    navController.navigate(R.id.signInFragment)
                }
            }
        }

        // pop-up message observer
        viewModel.popupBannerMessage.observe(this) {
            PopupBanner.make(this, it.first, it.second.asString(this)).show()
        }
    }
}