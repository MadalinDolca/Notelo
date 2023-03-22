package com.madalin.notelo.screens.notes.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.ApplicationClass
import com.madalin.notelo.R
import com.madalin.notelo.models.Category
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.DBCollection

// Store and manage UI-related data in a lifecycle-conscious way
class CategoriesViewModel : ViewModel() {
    private val firestore = Firebase.firestore
    private val categoriesList = mutableListOf<Category>() // list to store user's categories

    // data holders to observe
    val categoriesListLiveData by lazy { MutableLiveData<MutableList<Category>>() }
    val errorMessageLiveData by lazy { MutableLiveData<String>() }

    /**
     * Queries the database to find the user's categories and adds them to [categoriesList].
     * The [categoriesList] is set as the value of [categoriesListLiveData] and gets dispatched to the active observers.
     */
    fun getCategoriesFromFirestore() {
        firestore.collection(DBCollection.CATEGORIES)
            .whereEqualTo("userId", UserData.id)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    errorMessageLiveData.value = error.message
                    return@addSnapshotListener
                }

                categoriesList.clear() // clears the current list
                categoriesList.add(Category(id = Category.ID_UNCATEGORIZED, name = Category.NAME_UNCATEGORIZED))  // category used for uncategorized notes

                for (snapshot in snapshots!!) {
                    val category = snapshot.toObject<Category>()
                    category.id = snapshot.id
                    categoriesList.add(category)
                }

                categoriesListLiveData.value = categoriesList // sets the value and dispatches it to the active observers
            }
    }
}