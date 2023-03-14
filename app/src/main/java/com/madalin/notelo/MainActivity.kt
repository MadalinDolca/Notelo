package com.madalin.notelo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.screens.authentication.LoginActivity
import com.madalin.notelo.databinding.ActivityMainBinding
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.util.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.util.EdgeToEdge.edgeToEdge

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // binds this activity's views
        setContentView(binding.root) // setContentView(R.layout.activity_main)
        edgeToEdge(this, binding.coordinatorLayoutContainer, SPACING_PADDING, DIRECTION_TOP)
        edgeToEdge(this, binding.coordinatorLayoutContainer, SPACING_PADDING, DIRECTION_BOTTOM)

        navigationController = (supportFragmentManager.findFragmentById(binding.fragmentContainerView.id) as NavHostFragment).navController
        // navigationController = findNavController(binding.fragmentContainerView.id) //navigationController = findNavController(R.id.fragmentContainerView)
        binding.bottomNavigationView.setupWithNavController(navigationController) // sets the BottomNavigationView's NavController

        binding.buttonSignOut.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }


}