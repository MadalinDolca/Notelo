package com.madalin.notelo.content.presentation.categories_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.content.presentation.categories_list.util.GridSpacingItemDecoration
import com.madalin.notelo.core.presentation.components.category_properties.CategoryPropertiesBottomSheetDialog
import com.madalin.notelo.databinding.FragmentCategoriesBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoriesFragment : Fragment() {
    private val viewModel: CategoriesViewModel by viewModel()
    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var activityNavController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false) // inflate the layout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityNavController = (activity as AppCompatActivity).findNavController(R.id.mainActivityFragmentContainerView)
        categoriesAdapter = CategoriesAdapter(context, activityNavController)

        // recycler view preparations
        with(binding) {
            recyclerViewCategories.layoutManager = GridLayoutManager(context, 2)
            recyclerViewCategories.addItemDecoration(GridSpacingItemDecoration(2, resources.getDimensionPixelSize(R.dimen.margin_element), false))
            recyclerViewCategories.adapter = categoriesAdapter
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // categories list observer
        viewModel.categoriesListState.observe(viewLifecycleOwner) {
            if (it != null) {
                categoriesAdapter.setCategoriesList(it) // updates the categories from the adapter
                categoriesAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupListeners() {
        // obtains the categories on swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getAndObserveUserCategories()
            binding.swipeRefreshLayout.isRefreshing = false // hide the refresh indicator when the data has been fetched
        }

        // FAB that triggers the category creation dialog
        binding.floatingActionButton.setOnClickListener {
            val context = context ?: return@setOnClickListener
            CategoryPropertiesBottomSheetDialog(context).show()
        }
    }
}