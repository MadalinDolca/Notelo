package com.madalin.notelo.category_viewer.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.madalin.notelo.R
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.presentation.util.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.core.presentation.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.core.presentation.components.LayoutMessage
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.databinding.FragmentCategoryViewerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoryViewerFragment : Fragment() {
    private val viewModel: CategoryViewerViewModel by viewModel()
    private lateinit var binding: FragmentCategoryViewerBinding
    private lateinit var stateAdapter: CategoryNotesStateAdapter
    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // obtains the category's data and sets it to the ViewModel only if it hasn't been set before
        if (viewModel.category == null) {
            val args: CategoryViewerFragmentArgs by navArgs()
            viewModel.category = args.categoryData
        }

        binding = FragmentCategoryViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.textViewCategoryName, SPACING_PADDING, DIRECTION_TOP)
        edgeToEdge(activity, binding.viewPager2, SPACING_PADDING, DIRECTION_BOTTOM)

        binding.textViewCategoryName.text = viewModel.category?.name // set title

        // if the map of notes mapped by tags is null it fetches the notes and tags by category
        if (viewModel.notesByTagLiveData.value == null) {
            viewModel.getNotesByCategoryFromFirestore()
            viewModel.getTagsByCategoryFromFirestore()
        }

        /*viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                // Disable SwipeRefreshLayout while the user is scrolling the ViewPager
                binding.swipeRefreshLayout.isEnabled = position == 0 && positionOffset == 0f
            }
        })*/

        //binding.swipeRefreshLayout.setDistanceToTriggerSync((400 * resources.displayMetrics.density).toInt())

        setupObservers()
        setupListeners()
    }

    /**
     * Adapter to return a [Fragment] when [ViewPager2] is used.
     */
    inner class CategoryNotesStateAdapter(
        ownerFragment: Fragment,
        private var fragmentsList: List<TaggedNotesFragment>
    ) : FragmentStateAdapter(ownerFragment) {
        override fun getItemCount() = fragmentsList.size
        override fun createFragment(position: Int) = fragmentsList[position]
    }

    private fun setupObservers() {
        // grouped notes by tag observer
        viewModel.notesByTagLiveData.observe(viewLifecycleOwner) { notesByTagList ->
            val fragmentsList = mutableListOf<TaggedNotesFragment>() // list of fragments that contain notes grouped by tags

            // creates a Fragment for every Tag and passes its corresponding data
            for (noteMap in notesByTagList) {
                fragmentsList.add(TaggedNotesFragment.newInstance(noteMap.key, noteMap.value))
            }

            // when requested, this adapter returns a TagNoteFragment representing the list of notes from a category tag
            stateAdapter = CategoryNotesStateAdapter(this, fragmentsList)
            viewPager2 = binding.viewPager2
            viewPager2.adapter = stateAdapter // sets the adapter

            // hides the tabLayout if there is only the default tag or if there are no tags in the category
            if (notesByTagList.size < 3) {
                binding.tabLayoutTags.visibility = View.GONE
            } else {
                binding.tabLayoutTags.visibility = View.VISIBLE
            }

            // shows a message if there are no notes in the category
            if (notesByTagList.isEmpty()) {
                LayoutMessage.make(context, binding.container, binding.tabLayoutTags)
                    .setContent(R.raw.lottie_empty, getString(R.string.you_don_t_have_any_notes_in_this_category))
                    .show()

                binding.viewPager2.visibility = View.GONE // hides the ViewPager
            }

            // links the TabLayout to the ViewPager2 and sets the configuration
            val keysList = notesByTagList.keys.toList() // hold every key (Tag)
            TabLayoutMediator(binding.tabLayoutTags, viewPager2) { tab, position ->
                tab.text = keysList[position].name
            }.attach()
        }

        // popup message observer
        viewModel.popupMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(activity, it.first, it.second).show()
        }
    }

    private fun setupListeners() {
        // reload grouped notes on swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getNotesByCategoryFromFirestore()
            viewModel.getTagsByCategoryFromFirestore()
            binding.swipeRefreshLayout.isRefreshing = false // hide the refresh indicator when the data has been fetched
        }

        // show category options on click
        binding.imageViewMore.setOnClickListener {
            TODO()
        }
    }
}