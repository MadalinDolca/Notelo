package com.madalin.notelo.content.presentation.categories_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.repository.FirebaseContentRepository
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner

// Store and manage UI-related data in a lifecycle-conscious way
class CategoriesViewModel(
    private val globalDriver: GlobalDriver,
    private val repository: FirebaseContentRepository
) : ViewModel() {
    private val currentUser = globalDriver.currentUser
    private val categoriesList = mutableListOf<Category>() // list to store user's categories

    // data holders to observe
    private val _categoriesListLiveData by lazy { MutableLiveData<MutableList<Category>>() }
    val categoriesListLiveData: LiveData<MutableList<Category>> get() = _categoriesListLiveData

    init {
        getCategoriesFromFirestore()
    }

    /**
     * Obtains the categories associated with the current user ID, updates the data holders and
     * starts listening for changes.
     */
    fun getCategoriesFromFirestore() {
        val userId = currentUser.value?.id
        if (userId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_get_the_categories_because_the_user_id_is_null)
            return
        }

        repository.getCategoriesByUserIdListener(
            userId,
            onSuccess = {
                categoriesList.clear() // clears the current list
                categoriesList.add(Category(id = Category.ID_UNCATEGORIZED, name = Category.NAME_UNCATEGORIZED))  // category used for uncategorized notes
                categoriesList.addAll(it)

                _categoriesListLiveData.value = categoriesList // sets the value and dispatches it to the active observers
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: R.string.could_not_get_the_categories)
            }
        )
    }
}