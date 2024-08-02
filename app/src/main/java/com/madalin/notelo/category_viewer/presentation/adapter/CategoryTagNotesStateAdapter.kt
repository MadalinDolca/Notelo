package com.madalin.notelo.category_viewer.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.madalin.notelo.category_viewer.presentation.TaggedNotesFragment

/**
 * Adapter that returns a [TaggedNotesFragment] when [ViewPager2] is used.
 */
class CategoryTagNotesStateAdapter(
    ownerFragment: Fragment,
    private var fragmentsList: List<TaggedNotesFragment>
) : FragmentStateAdapter(ownerFragment) {
    override fun getItemCount() = fragmentsList.size
    override fun createFragment(position: Int) = fragmentsList[position]
}