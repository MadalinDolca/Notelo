package com.madalin.notelo.screens.notes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.madalin.notelo.R
import com.madalin.notelo.databinding.FragmentNotesBinding
import com.madalin.notelo.screens.notes.allnotes.AllNotesFragment
import com.madalin.notelo.screens.notes.categories.CategoriesFragment

class NotesFragment : Fragment() {

    private lateinit var binding: FragmentNotesBinding
    private lateinit var stateAdapter: NotesAndCategoriesStateAdapter
    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // the pager will contain a fragment for every notes and one for categories
        val fragmentsList = listOf(AllNotesFragment(), CategoriesFragment())
        stateAdapter = NotesAndCategoriesStateAdapter(this, fragmentsList)
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
        var fragmetsList: List<Fragment>
    ) : FragmentStateAdapter(ownerFragment) {
        override fun getItemCount() = fragmetsList.size
        override fun createFragment(position: Int) = fragmetsList[position]
    }
}