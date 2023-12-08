package com.madalin.notelo.feature.notesandcategories.categories

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
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.component.categoryproperties.CategoryPropertiesDialog
import com.madalin.notelo.databinding.FragmentCategoriesBinding
import com.madalin.notelo.feature.auth.signin.SignInViewModel
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.GridSpacingItemDecoration
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoriesFragment : Fragment() {
    private val viewModel: CategoriesViewModel by viewModel()
    private lateinit var binding: FragmentCategoriesBinding
    private lateinit var categoriesAdapter: CategoriesAdapter
    private lateinit var activityNavController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // checks if the user's categories have been fetched and gets them otherwise
        if (viewModel.categoriesListLiveData.value == null) {
            viewModel.getCategoriesFromFirestore(UserData.currentUser.id)
        }
    }

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
        viewModel.categoriesListLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                categoriesAdapter.setCategoriesList(it) // updates the categories from the adapter
                categoriesAdapter.notifyDataSetChanged()
            }
        }

        // error message observer
        viewModel.popupMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(activity, it.first, it.second).show()
        }
    }

    private fun setupListeners() {
        // obtains the categories on swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getCategoriesFromFirestore(UserData.currentUser.id)
            binding.swipeRefreshLayout.isRefreshing = false // hide the refresh indicator when the data has been fetched
        }

        // FAB that triggers the category creation dialog
        binding.floatingActionButton.setOnClickListener {
            val context = context ?: return@setOnClickListener
            CategoryPropertiesDialog(context, CategoryPropertiesDialog.MODE_CREATE).show()
        }
    }
}