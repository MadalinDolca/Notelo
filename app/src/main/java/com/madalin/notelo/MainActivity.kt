package com.madalin.notelo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.Collection.USERS
import com.madalin.notelo.authentication.LoginActivity
import com.madalin.notelo.databinding.ActivityMainBinding
import com.madalin.notelo.models.User
import com.madalin.notelo.notes.NotesFragment
import com.madalin.notelo.prayers.PrayersFragment
import com.madalin.notelo.recipes.RecipesFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var auth = Firebase.auth
    private var firestore = Firebase.firestore

    companion object {
        /**
         * Holds the data of the currently logged user.
         */
        lateinit var user: User

        // identifiers for the fragments used in this activity
        const val TAG_NOTES = "notes"
        const val TAG_RECIPES = "recipes"
        const val TAG_PRAYERS = "prayers"
    }

    private var fragmentManager = supportFragmentManager // manager for interacting with the fragments associated with this activity
    private var activeFragment: Fragment? = null // the active Fragment of this Activity

    // instances of the MainActivity's fragments
    private var notesFragment = NotesFragment.newInstance()
    private var recipesFragment = RecipesFragment.newInstance()
    private var prayersFragment = PrayersFragment.newInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root) // setContentView(R.layout.activity_main)

        val navigationController = findNavController(R.id.fragmentContainerView)
        binding.bottomNavigationView.setupWithNavController(navigationController)

        getUserData()
        //showFragment(notesFragment, TAG_NOTES, 0) // shows the "Notes" fragment upon Activity start

        // listens for navigation item selection
        /*binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_notes -> {
                    showFragment(notesFragment, TAG_NOTES, 0) // shows the "Notes" fragment
                    return@setOnItemSelectedListener true // marks the navigation bar's button as checked
                }

                R.id.item_recipes -> {
                    showFragment(recipesFragment, TAG_RECIPES, 1)
                    return@setOnItemSelectedListener true
                }

                R.id.item_prayers -> {
                    showFragment(prayersFragment, TAG_PRAYERS, 2)
                    return@setOnItemSelectedListener true
                }
            }

            false // if none of the items were selected, unchecks everyone of them
        }*/

        binding.buttonSignOut.setOnClickListener {
            Firebase.auth.signOut()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        }
    }

    /**
     * Replaces [MainActivity]'s FrameLayout with the specified [Fragment].
     * @param selectedFragment the [Fragment] to display
     * @param tag [Fragment]'s identifier
     * @param position the ID of the selected item in BottomNavigationView
     */
    /*private fun showFragment(selectedFragment: Fragment, tag: String, position: Int) {
        if (fragmentManager.isDestroyed) {
            fragmentManager = supportFragmentManager
        }

        if (selectedFragment.isAdded) { // if the selected fragment is already added to this activity
            fragmentManager.beginTransaction()
                .hide(activeFragment!!) // hides the active fragment
                .show(selectedFragment) // shows the selected fragment
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        } else { // if the selected fragment in not added to this activity
            if (activeFragment != null) { // if there is an active fragment
                fragmentManager.beginTransaction()
                    .add(binding.frameLayoutFragment.id, selectedFragment, tag) // adds the selected fragment to the activity
                    .hide(activeFragment!!) // hides the previous fragment
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            } else { // if there is NO active fragment
                fragmentManager.beginTransaction()
                    .add(binding.frameLayoutFragment.id, selectedFragment, tag)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
        }

        binding.bottomNavigationView.menu.getItem(position).isChecked = true // sets the navigation bar's button as checked
        activeFragment = selectedFragment // sets the newly active fragment
    }*/

    /**
     * If the user is logged in, gets the current user's data from Firestore every time the data
     * changes and stores them in [user].
     */
    private fun getUserData() {
        auth.currentUser?.let { it ->
            firestore.collection(USERS).document(it.uid)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        user = snapshot.toObject<User>()!! // converts the data snapshot to User
                    }
                }
        }
    }
}