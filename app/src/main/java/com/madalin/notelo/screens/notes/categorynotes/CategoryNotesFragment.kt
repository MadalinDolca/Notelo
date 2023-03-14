package com.madalin.notelo.screens.notes.categorynotes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.madalin.notelo.components.PopupBanner
import com.madalin.notelo.databinding.FragmentCategoryNotesBinding
import com.madalin.notelo.screens.notes.categorynotes.tagnotes.TagNotesFragment

class CategoryNotesFragment : Fragment() {
    private val args: CategoryNotesFragmentArgs by navArgs() // received data from another fragment
    private val viewModel: CategoryNotesViewModel by viewModels()

    private lateinit var binding: FragmentCategoryNotesBinding
    private lateinit var stateAdapter: CategoryNotesStateAdapter
    private lateinit var viewPager2: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // if the map of notes mapped by tags is null it fetches the notes and tags by category
        if (viewModel.notesByTagLiveData.value == null) {
            viewModel.getNotesByCategoryFromFirestore(args.categoryData.id)
            viewModel.getTagsByCategoryFromFirestore(args.categoryData.id)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCategoryNotesBinding.inflate(inflater, container, false)
        binding.textViewCategoryName.text = args.categoryData.name // sets the category title

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // grouped notes observer
        viewModel.notesByTagLiveData.observe(viewLifecycleOwner) { notesByTag ->
            // creates a list of fragments with the notes grouped by tags
            val keysList = notesByTag.keys.toList() // hold every key (Tag)
            val fragmentsList = mutableListOf<TagNotesFragment>()

            for (notes in notesByTag) {
                fragmentsList.add(TagNotesFragment.newInstance(notes.key, notes.value))
            }

            // when requested, this adapter returns a TagNoteFragment representing the list of notes from a category tag
            stateAdapter = CategoryNotesStateAdapter(this, fragmentsList)
            viewPager2 = binding.viewPager2
            viewPager2.adapter = stateAdapter // sets the adapter

            // hides the tabLayout if there are no tags in the category
            if (notesByTag.size < 2) {
                binding.tabLayoutTags.visibility = View.GONE
            } else {
                binding.tabLayoutTags.visibility = View.VISIBLE
            }

            // links the TabLayout to the ViewPager2
            TabLayoutMediator(binding.tabLayoutTags, viewPager2) { tab, position ->
                tab.text = keysList[position].name
            }.attach()
        }

        // error message observer
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(context, PopupBanner.TYPE_FAILURE, it.toString()).show()
        }
    }

    /**
     * Adapter to return a [Fragment] when [ViewPager2] is used.
     */
    inner class CategoryNotesStateAdapter(
        var ownerFragment: Fragment,
        var fragmentsList: MutableList<TagNotesFragment>
    ) : FragmentStateAdapter(ownerFragment) {

        override fun getItemCount() = fragmentsList.size

        override fun createFragment(position: Int) = fragmentsList[position]
    }
}