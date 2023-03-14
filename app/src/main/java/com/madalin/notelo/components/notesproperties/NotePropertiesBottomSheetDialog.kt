package com.madalin.notelo.components.notesproperties

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.R
import com.madalin.notelo.databinding.LayoutNotePropertiesBottomsheetdialogBinding
import com.madalin.notelo.models.Category
import com.madalin.notelo.models.Note
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.DBCollection

class NotePropertiesBottomSheetDialog(
    var ownerContext: Context,
    var note: Note
) : BottomSheetDialog(ownerContext) {
    lateinit var binding: LayoutNotePropertiesBottomsheetdialogBinding

    var adapter = SpinnerCategoryAdapter()
    var firestore = Firebase.firestore
    var categoriesList = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutNotePropertiesBottomsheetdialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //behavior.peekHeight = ViewGroup.LayoutParams.WRAP_CONTENT
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // chooses the radio button to check
        if (note.visible == Note.PRIVATE) {
            binding.radioButtonPrivate.isChecked = true
        } else {
            binding.radioButtonPublic.isChecked = true
        }

        // categories spinner adapter
        binding.spinnerCategory.adapter = adapter
        getCategoriesFromFirestore()

        binding.buttonSave.setOnClickListener {
            //firestore.collection(DBCollection.NOTES)
        }
    }

    /**
     * Queries the database for the categories list and adds all entries to [categoriesList]. The
     * [categoriesList] is later used to update the [SpinnerCategoryAdapter].
     */
    fun getCategoriesFromFirestore() {
        firestore.collection(DBCollection.CATEGORIES)
            .whereEqualTo("userId", UserData.id)
            .get()
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    val category = snapshot.toObject<Category>()
                    category.id = snapshot.id
                    categoriesList.add(category)
                }

                adapter.setCategoriesList(categoriesList)
                adapter.notifyDataSetChanged()
            }
    }

    fun getCategoryTagsFromFirestore() {
        firestore.collection(DBCollection.TAGS)
            .whereEqualTo("userId", UserData.id)
        //.whereEqualTo("categoryId", binding.spinnerCategory)
    }
}