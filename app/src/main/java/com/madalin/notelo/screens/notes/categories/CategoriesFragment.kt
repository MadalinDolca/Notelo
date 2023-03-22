package com.madalin.notelo.screens.notes.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.GridLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.components.PopupBanner
import com.madalin.notelo.components.categoryproperties.CategoryPropertiesDialog
import com.madalin.notelo.databinding.FragmentCategoriesBinding
import com.madalin.notelo.util.GridSpacingItemDecoration

class CategoriesFragment : Fragment() {
    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var viewModel: CategoriesViewModel
    private lateinit var categoriesAdapter: CategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoriesAdapter = CategoriesAdapter(context)
        viewModel = ViewModelProvider(this).get() // gets the associated ViewModels

        // checks if the user's categories have been fetched and gets them otherwise
        if (viewModel.categoriesListLiveData.value == null) {
            viewModel.getCategoriesFromFirestore()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCategoriesBinding.inflate(inflater, container, false) // inflate the layout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // recycler views preparations
        with(binding) {
            recyclerViewCategories.layoutManager = GridLayoutManager(context, 2)
            recyclerViewCategories.addItemDecoration(GridSpacingItemDecoration(2, resources.getDimensionPixelSize(R.dimen.margin_element), false))
            recyclerViewCategories.adapter = categoriesAdapter
        }

        // user's categories fetching observer
        viewModel.categoriesListLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                categoriesAdapter.setCategoriesList(it) // updates the categories from the adapter
                categoriesAdapter.notifyDataSetChanged()
            } else {
                PopupBanner.make(context, PopupBanner.TYPE_FAILURE, getString(R.string.something_went_wrong_please_try_again)).show()
            }
        }

        // user's categories failed fetching observer
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(context, PopupBanner.TYPE_FAILURE, it).show()
        }

        // swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getCategoriesFromFirestore()
            binding.swipeRefreshLayout.isRefreshing = false // hide the refresh indicator when the data has been fetched
        }

        // FAB that triggers the category creation dialog
        binding.floatingActionButton.setOnClickListener {
            CategoryPropertiesDialog(requireContext(), CategoryPropertiesDialog.MODE_CREATE).show()
        }
    }
}