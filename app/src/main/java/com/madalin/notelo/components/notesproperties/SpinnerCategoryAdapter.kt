package com.madalin.notelo.components.notesproperties

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.madalin.notelo.databinding.LayoutSpinnerItemBinding
import com.madalin.notelo.models.Category

/**
 * Adapter used for the categories shown in the Category Spinner.
 */
class SpinnerCategoryAdapter : BaseAdapter() {
    private lateinit var binding: LayoutSpinnerItemBinding
    private var categoriesList = mutableListOf<Category>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (convertView == null) {
            binding = LayoutSpinnerItemBinding.inflate(LayoutInflater.from(parent?.context))
            binding.root.tag = binding
        } else {
            binding = convertView.tag as LayoutSpinnerItemBinding
        }

        // sets the data
        binding.textViewCategoryName.text = categoriesList[position].name

        return binding.root
    }

    override fun getItem(position: Int) = categoriesList[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getCount() = categoriesList.size

    /**
     * Updates the current [categoriesList] with the data from the specified list.
     * @param newList list that contains the new data
     */
    fun setCategoriesList(newList: List<Category>) {
        categoriesList.clear()
        categoriesList.addAll(newList)
    }
}