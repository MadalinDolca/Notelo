package com.madalin.notelo.screens.notes.categoryviewer

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayoutMediator
import com.madalin.notelo.R
import com.madalin.notelo.components.LayoutMessage
import com.madalin.notelo.components.PopupBanner
import com.madalin.notelo.databinding.ActivityCategoryViewerBinding
import com.madalin.notelo.models.Category
import com.madalin.notelo.screens.notes.categoryviewer.tagnotes.TagNotesFragment
import com.madalin.notelo.util.EdgeToEdge
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.util.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.util.Extra

class CategoryViewerActivity : AppCompatActivity() {
    private val viewModel: CategoryViewerViewModel by viewModels()
    //private val args: CategoryViewerFragmentArgs by navArgs() // received data from another fragment

    private lateinit var binding: ActivityCategoryViewerBinding
    private lateinit var stateAdapter: CategoryNotesStateAdapter
    private lateinit var viewPager2: ViewPager2
    private lateinit var category: Category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdge(this, binding.textViewCategoryName, SPACING_PADDING, DIRECTION_TOP)
        edgeToEdge(this, binding.viewPager2, SPACING_PADDING, DIRECTION_BOTTOM)

        getIntentData()

        binding.textViewCategoryName.text = category.name

        // if the map of notes mapped by tags is null it fetches the notes and tags by category
        if (viewModel.notesByTagLiveData.value == null) {
            viewModel.getNotesByCategoryFromFirestore(category.id)
            viewModel.getTagsByCategoryFromFirestore(category.id)
        }

        // grouped notes observer
        viewModel.notesByTagLiveData.observe(this) { notesByTag ->
            // creates a list of fragments with the notes grouped by tags
            val fragmentsList = mutableListOf<TagNotesFragment>()

            // creates a fragment for every tag from the category and passes its corresponding data
            for (noteMap in notesByTag) {
                fragmentsList.add(TagNotesFragment.newInstance(noteMap.key, noteMap.value))
            }

            // when requested, this adapter returns a TagNoteFragment representing the list of notes from a category tag
            stateAdapter = CategoryNotesStateAdapter(supportFragmentManager, fragmentsList)
            viewPager2 = binding.viewPager2
            viewPager2.adapter = stateAdapter // sets the adapter

            // hides the tabLayout if there is only the default tag or if there are no tags in the category
            if (notesByTag.size < 3) {
                binding.tabLayoutTags.visibility = View.GONE
            } else {
                binding.tabLayoutTags.visibility = View.VISIBLE
            }

            // shows a message if there are no notes in the category
            if (notesByTag.isEmpty()) {
                LayoutMessage.make(this, binding.container, binding.tabLayoutTags)
                    .setContent(R.raw.lottie_empty, getString(R.string.you_don_t_have_any_notes_in_this_category))
                    .show()

                binding.viewPager2.visibility = View.GONE // hides the ViewPager
            }

            // links the TabLayout to the ViewPager2 and sets the configuration
            val keysList = notesByTag.keys.toList() // hold every key (Tag)
            TabLayoutMediator(binding.tabLayoutTags, viewPager2) { tab, position ->
                tab.text = keysList[position].name
            }.attach()
        }

        // error message observer
        viewModel.errorMessageLiveData.observe(this) {
            PopupBanner.make(this, PopupBanner.TYPE_FAILURE, it.toString()).show()
        }

        /*viewPager2.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                // Disable SwipeRefreshLayout while the user is scrolling the ViewPager
                binding.swipeRefreshLayout.isEnabled = position == 0 && positionOffset == 0f
            }
        })*/

        //binding.swipeRefreshLayout.setDistanceToTriggerSync((400 * resources.displayMetrics.density).toInt())

        // reload grouped notes on swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getNotesByCategoryFromFirestore(category.id)
            viewModel.getTagsByCategoryFromFirestore(category.id)
            binding.swipeRefreshLayout.isRefreshing = false // hide the refresh indicator when the data has been fetched
        }

        // show category options on click
        binding.imageViewMore.setOnClickListener {}
    }

    /**
     * Determines the received intent extra and sets the [category] data.
     */
    private fun getIntentData() {
        if (intent.hasExtra(Extra.CATERGORY)) {
            category = intent.getParcelableExtra(Extra.CATERGORY)!!
        } else { // no data provided
            category = Category()
        }
    }

    /**
     * Adapter to return a [Fragment] when [ViewPager2] is used.
     */
    inner class CategoryNotesStateAdapter(
        var fragmentManager: FragmentManager,
        var fragmentsList: MutableList<TagNotesFragment>
    ) : FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount() = fragmentsList.size

        override fun createFragment(position: Int) = fragmentsList[position]
    }
}