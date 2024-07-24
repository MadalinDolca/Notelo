package com.madalin.notelo.core.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.madalin.notelo.R
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.databinding.ActivityMainBinding
import com.madalin.notelo.core.presentation.user.UserData
import com.madalin.notelo.core.domain.util.EdgeToEdge.edgeToEdge
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModel()
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = ActivityMainBinding.inflate(layoutInflater) // binds this activity's views
        setContentView(binding.root) // setContentView(R.layout.activity_main)
        edgeToEdge(this)

        navController = (supportFragmentManager.findFragmentById(binding.mainActivityFragmentContainerView.id) as NavHostFragment).navController

        // navigates to MainFragment if the user has singed in, otherwise to SignInFragment (default)
        if (UserData.isUserSignedIn()) {
            navController.navigate(R.id.mainFragment)
            UserData.startListeningForUserData(this)
        }

        setupObservers()
    }

    private fun setupObservers() {
        // pop-up message observer
        viewModel.popupMessageLiveData.observe(this) {
            PopupBanner.make(this, it.first, getString(it.second)).show()
        }

        // application login state observer
        /*ApplicationState.isUserSignedIn.observe(this) {
            if (it) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.mainFragment, false)
                    .build()

                navController.navigate(R.id.mainFragment, null, navOptions)
            }
        }*/
    }
}