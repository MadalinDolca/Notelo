package com.madalin.notelo.category_viewer.presentation

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import com.madalin.notelo.R
import com.madalin.notelo.category_viewer.presentation.adapter.CategoryTagNotesStateAdapter
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.presentation.components.LayoutMessage
import com.madalin.notelo.core.presentation.components.category_properties.CategoryPropertiesBottomSheetDialog
import com.madalin.notelo.core.presentation.util.DynamicColor
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.presentation.util.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.core.presentation.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.databinding.FragmentCategoryViewerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoryViewerFragment : Fragment() {
    private val viewModel: CategoryViewerViewModel by viewModel()
    private lateinit var binding: FragmentCategoryViewerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCategoryViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.layoutHeader, SPACING_PADDING, DIRECTION_TOP)
        edgeToEdge(activity, binding.viewPager2, SPACING_PADDING, DIRECTION_BOTTOM)

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // category observer
        viewModel.categoryState.observe(viewLifecycleOwner) {
            setViewerData(it)
        }

        // notes grouped by tags observer
        viewModel.tagNotesMapState.observe(viewLifecycleOwner) {
            buildTagNotesPager(it)
        }
    }

    private fun setupListeners() {
        // back button listener
        binding.imageButtonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // shows category properties on click if the category is not "uncategorized"
        if (!Category.isUncategorized(viewModel.categoryId)) {
            binding.imageButtonProperties.setOnClickListener {
                val context = context ?: return@setOnClickListener
                CategoryPropertiesBottomSheetDialog(context, viewModel.categoryId).show()
            }
        } else {
            binding.imageButtonProperties.visibility = View.GONE // hides the button if the category is "uncategorized"
        }
    }

    /**
     * Sets the data of the category viewer based on this [category].
     */
    private fun setViewerData(category: Category) {
        // category name
        binding.textViewCategoryName.text = category.name

        // tab layout text and background selected color
        val color = category.color
        if (color != null) {
            val backgroundColor = Color.parseColor(color)
            val textColor = DynamicColor.getDynamicColor(backgroundColor)
            // todo fix selected tag text color
            //binding.tabLayoutTags.setTabTextColors(resources.getColor(R.color.text_on_primary), textColor)
            binding.tabLayoutTags.setSelectedTabIndicatorColor(Color.parseColor(color))
        }
    }

    /**
     * Builds a ViewPager based on the given [tagNotesMap].
     */
    private fun buildTagNotesPager(tagNotesMap: Map<Tag, List<Note>>) {
        val fragmentsList = mutableListOf<TaggedNotesFragment>() // list of fragments that contains notes grouped by tags

        // creates a Fragment for every Tag and passes it
        tagNotesMap.forEach {
            fragmentsList.add(TaggedNotesFragment.newInstance(it.key, it.value))
        }

        // sets the adapter
        // when requested, this adapter returns a TagNoteFragment representing the list of notes from a category tag
        binding.viewPager2.adapter = CategoryTagNotesStateAdapter(this, fragmentsList)

        // hides the tabLayout if there is only the default tag or if there are no tags in the category
        if (tagNotesMap.size < 2) {
            binding.tabLayoutTags.visibility = View.GONE
        } else {
            binding.tabLayoutTags.visibility = View.VISIBLE
        }

        // shows a message if there are no notes in the category
        if (tagNotesMap.isEmpty()) {
            LayoutMessage.make(context, binding.container, binding.tabLayoutTags)
                .setContent(R.raw.lottie_empty, getString(R.string.you_don_t_have_any_notes_in_this_category))
                .show()

            binding.viewPager2.visibility = View.GONE // hides the ViewPager
        }

        // links the TabLayout to the ViewPager2 and sets the configuration
        val tags = tagNotesMap.keys.toList()
        TabLayoutMediator(binding.tabLayoutTags, binding.viewPager2) { tab, position ->
            tab.text = tags[position].name
        }.attach()
    }
}