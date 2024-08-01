package com.madalin.notelo.content.presentation.categories_list

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CategoriesViewModel(
    private val globalDriver: GlobalDriver,
    private val localRepository: LocalContentRepository
) : ViewModel() {
    private var _categoriesListState = MutableLiveData(listOf(Category.subUncategorized()))
    val categoriesListState: LiveData<List<Category>> get() = _categoriesListState

    init {
        getAndObserveUserCategories()
    }

    /**
     * Obtains the user categories and starts listening for changes.
     */
    fun getAndObserveUserCategories() {
        viewModelScope.launch {
            localRepository.getCategoriesObserver()
                .catch {
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_FAILURE,
                        it.message ?: R.string.could_not_get_the_categories
                    )
                    Log.d("CategoriesViewModel", "Could not get the categories: ${it.message}")
                }
                .collect {
                    val categories = mutableListOf(Category.subUncategorized()) // first category is "Uncategorized"
                    categories.addAll(1, it)
                    _categoriesListState.postValue(categories)
                }
        }
    }
}