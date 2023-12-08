package com.madalin.notelo.feature.notesandcategories.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.model.Category
import com.madalin.notelo.repository.FirebaseContentRepository
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.DBCollection

// Store and manage UI-related data in a lifecycle-conscious way
class CategoriesViewModel(
    private val repository: FirebaseContentRepository
) : ViewModel() {
    private val categoriesList = mutableListOf<Category>() // list to store user's categories

    // data holders to observe
    private val _categoriesListLiveData by lazy { MutableLiveData<MutableList<Category>>() }
    val categoriesListLiveData: LiveData<MutableList<Category>> get() = _categoriesListLiveData

    private val _popupMessageLiveData = MutableLiveData<Pair<Int, String>>()
    val popupMessageLiveData: LiveData<Pair<Int, String>> get() = _popupMessageLiveData

    /**
     * Obtains the categories associated with the given [userId], updates the data holders and
     * starts listening for changes.
     */
    fun getCategoriesFromFirestore(userId: String) {
        repository.getCategoriesByUserIdListener(userId,
            onSuccess = {
                categoriesList.clear() // clears the current list
                categoriesList.add(Category(id = Category.ID_UNCATEGORIZED, name = Category.NAME_UNCATEGORIZED))  // category used for uncategorized notes
                categoriesList.addAll(it)

                _categoriesListLiveData.value = categoriesList // sets the value and dispatches it to the active observers
            },
            onFailure = {
                it?.let { _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, it) }
            })
    }
}