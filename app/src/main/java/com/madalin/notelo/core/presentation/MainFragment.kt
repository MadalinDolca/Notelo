package com.madalin.notelo.core.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.databinding.FragmentMainBinding
import com.madalin.notelo.core.domain.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.core.domain.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.domain.util.EdgeToEdge.edgeToEdge

/**
 * Fragment used to display notes/categories, add and discovery fragments with a bottom navigation bar.
 */
class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.bottomNavigationView, SPACING_MARGIN, DIRECTION_BOTTOM)

        navController = (childFragmentManager.findFragmentById(binding.mainFragmentFragmentContainerView.id) as NavHostFragment).navController
        binding.bottomNavigationView.setupWithNavController(navController) // sets the BottomNavigationView's NavController

        binding.buttonSignOut.setOnClickListener {
            Firebase.auth.signOut()
            // findNavController().navigate(R.id.action_mainFragment_to_signInFragment)
        }
    }
}