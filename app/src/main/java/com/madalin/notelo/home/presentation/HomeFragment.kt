package com.madalin.notelo.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.core.presentation.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.presentation.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.databinding.FragmentHomeBinding

/**
 * Fragment used to display notes/categories, add and discovery fragments with a bottom navigation bar.
 */
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.bottomNavigationView, SPACING_MARGIN, DIRECTION_BOTTOM)

        navController = (childFragmentManager.findFragmentById(binding.mainFragmentFragmentContainerView.id) as NavHostFragment).navController
        binding.bottomNavigationView.setupWithNavController(navController) // sets the BottomNavigationView's NavController
    }
}