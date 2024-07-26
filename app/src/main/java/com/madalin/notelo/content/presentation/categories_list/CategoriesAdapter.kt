package com.madalin.notelo.content.presentation.categories_list

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.content.presentation.categories_list.util.DynamicColor.getDynamicColor
import com.madalin.notelo.core.presentation.components.CategoryPropertiesDialog
import com.madalin.notelo.databinding.LayoutCategoryCardBinding
import com.madalin.notelo.home.presentation.HomeFragmentDirections

class CategoriesAdapter(
    var context: Context?,
    private val navController: NavController
) : RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    private var categoriesList = mutableListOf<Category>()

    /**
     * Describes the view of an item and the metadata about its place in the [RecyclerView].
     */
    inner class CategoriesViewHolder(val binding: LayoutCategoryCardBinding) : ViewHolder(binding.root)

    /**
     * Returns a new [CategoriesViewHolder] with a binder inflated with the collection's XML layout when the [RecyclerView] needs it.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val binding = LayoutCategoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriesViewHolder(binding)
    }

    /**
     * Binds the data to the item view at the specified position when called by [RecyclerView].
     */
    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        with(holder) {
            val thisCategory = categoriesList[position] //with(categoriesList[position]) {
            binding.textViewCategoryName.text = thisCategory.name

            // if the category has a color specified then it will be used to set the colors of the layout
            if (thisCategory.color != null) {
                val backgroundColor = Color.parseColor(thisCategory.color)
                binding.root.setCardBackgroundColor(backgroundColor) // sets the background color
                binding.textViewCategoryName.setTextColor(getDynamicColor(backgroundColor)) // sets the text color
                binding.imageViewCategoryImage.setColorFilter(getDynamicColor(backgroundColor, 0.2f))
            }

            // opens the fragment containing the notes from the selected category by giving the needed data
            binding.root.setOnClickListener {
                val action = HomeFragmentDirections.actionGlobalCategoryViewerFragment(thisCategory)
                navController.navigate(action)
            }

            // opens the modification dialog on long click if the category is not the uncategorized one
            if (position != 0) {
                binding.root.setOnLongClickListener {
                    val context = context ?: return@setOnLongClickListener true
                    CategoryPropertiesDialog(context, CategoryPropertiesDialog.MODE_UPDATE, thisCategory).show()
                    return@setOnLongClickListener true
                }
            }
        }
    }

    /**
     * Returns the number of categories from the list.
     * @return [categoriesList] size
     */
    override fun getItemCount() = categoriesList.size

    /**
     * Updates the current [categoriesList] with the data from the specified list.
     * @param newCategoryList list that contains the new data
     */
    fun setCategoriesList(newCategoryList: List<Category>) {
        categoriesList.clear()
        categoriesList.addAll(newCategoryList)
    }
}