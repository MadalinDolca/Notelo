package com.madalin.notelo.content.presentation.categories_list

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.madalin.notelo.content.presentation.categories_list.util.DynamicColor.getDynamicColor
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.databinding.LayoutCategoryCardBinding

class CategoriesAdapter(
    private val onOpenCategoryClick: (Category) -> Unit,
    private val onOpenCategoryPropertiesClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    private var categoriesList = mutableListOf<Category>()

    /**
     * Describes the view of an item and the metadata about its place in the [RecyclerView].
     */
    inner class CategoriesViewHolder(val binding: LayoutCategoryCardBinding) : ViewHolder(binding.root)

    /**
     * Returns a new [CategoriesViewHolder] with a binder inflated with the collection's XML layout
     * when the [RecyclerView] needs it.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val binding = LayoutCategoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriesViewHolder(binding)
    }

    /**
     * Binds the data to the item view at the specified position when called by [RecyclerView].
     */
    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val currentCategory = categoriesList[position]
        with(holder) {
            binding.textViewCategoryName.text = currentCategory.name

            // if the category has a color specified then it will be used to set the colors of the layout
            if (currentCategory.color != null) {
                val backgroundColor = Color.parseColor(currentCategory.color)
                binding.root.setCardBackgroundColor(backgroundColor) // sets the background color
                binding.textViewCategoryName.setTextColor(getDynamicColor(backgroundColor)) // sets the text color
                binding.imageViewCategoryImage.setColorFilter(getDynamicColor(backgroundColor, 0.2f))
            }

            binding.root.setOnClickListener {
                onOpenCategoryClick(currentCategory)
            }

            binding.root.setOnLongClickListener {
                onOpenCategoryPropertiesClick(currentCategory)
                return@setOnLongClickListener true
            }
        }
    }

    /**
     * Returns the number of categories from the list.
     * @return [categoriesList] size
     */
    override fun getItemCount() = categoriesList.size

    /**
     * Updates the current [categoriesList] with the data from [newCategoryList].
     */
    fun setCategoriesList(newCategoryList: List<Category>) {
        categoriesList.clear()
        categoriesList.addAll(newCategoryList)
    }
}