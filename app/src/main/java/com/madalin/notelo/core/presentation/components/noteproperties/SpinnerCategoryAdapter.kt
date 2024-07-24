package com.madalin.notelo.core.presentation.components.noteproperties

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.madalin.notelo.databinding.LayoutSpinnerItemBinding
import com.madalin.notelo.core.domain.model.Category

/**
 * Adapter used for the categories shown in the Category Spinner.
 */
class SpinnerCategoryAdapter : BaseAdapter() {
    private lateinit var binding: LayoutSpinnerItemBinding
    private var categoriesList = mutableListOf<Category>() // list to store the categories

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (convertView == null) {
            binding = LayoutSpinnerItemBinding.inflate(LayoutInflater.from(parent?.context))
            binding.root.tag = binding
        } else {
            binding = convertView.tag as LayoutSpinnerItemBinding
        }

        // sets the data of the spinner item
        binding.textViewCategoryName.text = categoriesList[position].name

        return binding.root
    }

    override fun getItem(position: Int) = categoriesList[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = categoriesList.size

    /**
     * Updates the current [categoriesList] with the data from [newList].
     */
    fun setCategoriesList(newList: List<Category>) {
        categoriesList.clear()
        categoriesList.addAll(newList)
    }
}