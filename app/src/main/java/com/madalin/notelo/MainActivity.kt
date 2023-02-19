package com.madalin.notelo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.Collection.USERS
import com.madalin.notelo.authentication.LoginActivity
import com.madalin.notelo.databinding.ActivityMainBinding
import com.madalin.notelo.user.User
import com.madalin.notelo.user.UserData.getUserData
import com.madalin.notelo.utilities.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.utilities.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.utilities.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.utilities.EdgeToEdge.edgeToEdge
import com.madalin.notelo.utilities.PopupBanner

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // binds this activity's views
        setContentView(binding.root) // setContentView(R.layout.activity_main)
        edgeToEdge(this, binding.coordinatorLayoutContainer, SPACING_PADDING, DIRECTION_TOP)
        edgeToEdge(this, binding.coordinatorLayoutContainer, SPACING_PADDING, DIRECTION_BOTTOM)

        getUserData(this@MainActivity)

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