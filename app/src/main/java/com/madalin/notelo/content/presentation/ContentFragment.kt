package com.madalin.notelo.content.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.madalin.notelo.R
import com.madalin.notelo.databinding.FragmentNotesAndCategoriesBinding
import com.madalin.notelo.core.domain.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.domain.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.domain.util.EdgeToEdge.edgeToEdge

class ContentFragment : Fragment() {
    private val viewModel: ContentViewModel by viewModels()
    private lateinit var binding: FragmentNotesAndCategoriesBinding
    private lateinit var stateAdapter: NotesAndCategoriesStateAdapter
    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNotesAndCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.tabLayout, SPACING_MARGIN, DIRECTION_TOP)

        // the view pager will contain a fragment for listing every note and one for categories
        stateAdapter = NotesAndCategoriesStateAdapter(this, viewModel.fragmentsList)
        viewPager2 = binding.viewPager2
        viewPager2.adapter = stateAdapter

        // links the TabLayout to the ViewPager2 and sets the configuration
        TabLayoutMediator(binding.tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.all_notes)
                1 -> tab.text = getString(R.string.categories)
            }
        }.attach()
    }

    /**
     * Adapter to return a [Fragment] when [ViewPager2] is used.
     */
    inner class NotesAndCategoriesStateAdapter(
        ownerFragment: Fragment,
        private var fragmetsList: List<Fragment>
    ) : FragmentStateAdapter(ownerFragment) {
        override fun getItemCount() = fragmetsList.size
        override fun createFragment(position: Int) = fragmetsList[position]
    }
}