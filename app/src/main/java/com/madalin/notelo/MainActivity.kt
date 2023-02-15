package com.madalin.notelo

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.madalin.notelo.models.User
import com.madalin.notelo.ui.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.ui.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.ui.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.ui.EdgeToEdge.edgeToEdge
import com.madalin.notelo.ui.PopupBanner

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navigationController: NavController
    private var auth = Firebase.auth
    private var firestore = Firebase.firestore

    companion object {
        /**
         * Holds the data of the currently logged user.
         */
        lateinit var user: User
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater) // binds this activity's views
        setContentView(binding.root) // setContentView(R.layout.activity_main)
        edgeToEdge(this, binding.coordinatorLayoutContainer, SPACING_PADDING, DIRECTION_TOP)
        edgeToEdge(this, binding.coordinatorLayoutContainer, SPACING_PADDING, DIRECTION_BOTTOM)

        getUserData()

        navigationController =
            (supportFragmentManager.findFragmentById(binding.fragmentContainerView.id) as NavHostFragment).navController // navigationController = findNavController(binding.fragmentContainerView.id)
        binding.bottomNavigationView.setupWithNavController(navigationController) // sets the BottomNavigationView's NavController

        binding.buttonSignOut.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * If the user is logged in, gets the current user's data from Firestore every time the data
     * changes and stores them in [user].
     */
    private fun getUserData() {
        auth.currentUser?.let { it ->
            firestore.collection(USERS).document(it.uid)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        PopupBanner.make(this@MainActivity, PopupBanner.TYPE_FAILURE, exception.message!!).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        user = snapshot.toObject<User>()!! // converts the data snapshot to User
                    }
                }
        }
    }
}