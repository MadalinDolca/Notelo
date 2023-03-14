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
    private val categoriesListLiveData = MutableLiveData<MutableList<Category>>()
    private val errorMessageLiveData = MutableLiveData<String>()

    // public getters to return data holders
    val getCategoriesListLiveData: LiveData<MutableList<Category>> get() = categoriesListLiveData
    val getErrorMessageLiveData: LiveData<String> get() = errorMessageLiveData

    /**
     * Queries the database to find the user's categories and adds them to [categoriesList].
     * The [categoriesList] is set as the value of [categoriesListLiveData] and gets dispatched to the active observers.
     */
    fun getCategoriesFromFirestore() {
        categoriesList.clear() // clears the current list

        // category used for uncategorized notes
        categoriesList.add(Category(id = Category.CATEGORY_ID_UNCATEGORIZED, name = ApplicationClass.context.getString(R.string.uncategorized)))

        // categories from Firestore
        firestore.collection(DBCollection.CATEGORIES)
            .whereEqualTo("userId", UserData.id)
            .get()
            .addOnSuccessListener { snapshots ->
                //collectionsList.addAll(snapshots.toObjects(Category::class.java))
                for (snapshot in snapshots) {
                    val category = snapshot.toObject<Category>()
                    category.id = snapshot.id
                    categoriesList.add(category)
                }

                categoriesListLiveData.value = categoriesList // sets the value and dispatches it to the active observers
            }
            .addOnFailureListener {
                errorMessageLiveData.value = it.message.toString()
            }
    }
}