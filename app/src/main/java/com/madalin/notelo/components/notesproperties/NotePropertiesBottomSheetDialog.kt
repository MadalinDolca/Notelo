package com.madalin.notelo.components.notesproperties

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.R
import com.madalin.notelo.components.PopupBanner
import com.madalin.notelo.databinding.LayoutNotePropertiesBottomsheetdialogBinding
import com.madalin.notelo.models.Category
import com.madalin.notelo.models.Note
import com.madalin.notelo.models.Tag
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.DBCollection

class NotePropertiesBottomSheetDialog(
    var ownerContext: Context,
    var note: Note
) : BottomSheetDialog(ownerContext) {
    lateinit var binding: LayoutNotePropertiesBottomsheetdialogBinding

    var firestore = Firebase.firestore
    var categoryAdapter = SpinnerCategoryAdapter()
    var tagsAdapter = SpinnerTagAdapter()
    var categoriesList = mutableListOf<Category>()
    var tagsList = mutableListOf<Tag>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutNotePropertiesBottomsheetdialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED // fully opened dialog

        // chooses the radio button to check
        if (note.visible == Note.PRIVATE) {
            binding.radioButtonPrivate.isChecked = true
        } else {
            binding.radioButtonPublic.isChecked = true
        }

        // categories spinner
        binding.spinnerCategory.adapter = categoryAdapter // sets the adapter
        getCategoriesFromFirestore()
        binding.spinnerCategory.onItemSelectedListener = itemSelectedListener() // sets the item selection listener

        // tags spinner
        binding.spinnerTags.adapter = tagsAdapter

        binding.buttonSave.setOnClickListener { updateNote() }
    }

    /**
     * Queries the database for the categories list and adds all entries to [categoriesList]. The
     * [categoriesList] is later used to update the [categoryAdapter].
     */
    private fun getCategoriesFromFirestore() {
        firestore.collection(DBCollection.CATEGORIES)
            .whereEqualTo("userId", UserData.id)
            .get()
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    val category = snapshot.toObject<Category>()
                    category.id = snapshot.id
                    categoriesList.add(category)
                }

                categoryAdapter.setCategoriesList(categoriesList)
                categoryAdapter.notifyDataSetChanged()

                // gets the position of the note's category in the list and sets the spinner's selection
                val position = categoriesList.indexOfFirst { it.id == note.categoryId }
                binding.spinnerCategory.setSelection(position)
            }
    }

    /**
     * Queries the database for the tags associated with the given [category] and adds all entries
     * to [tagsList]. The [tagsList] is later used to update the [tagsAdapter].
     */
    fun getCategoryTagsFromFirestore(category: Category) {
        firestore.collection(DBCollection.TAGS)
            .whereEqualTo("categoryId", category.id)
            .get()
            .addOnSuccessListener { snapshots ->
                tagsList.clear() // clears the current list

                for (snapshot in snapshots) {
                    val tag = snapshot.toObject<Tag>()
                    tag.id = snapshot.id
                    tagsList.add(tag)
                }

                // updates the adapter with the new list of tags
                tagsAdapter.setTagsList(tagsList)
                tagsAdapter.notifyDataSetChanged()

                // if the category doesn't have any tags, then the tags spinner is hidden
                if (tagsList.isEmpty()) {
                    binding.textViewTags.visibility = View.GONE
                    binding.spinnerTags.visibility = View.GONE
                } else {
                    binding.textViewTags.visibility = View.VISIBLE
                    binding.spinnerTags.visibility = View.VISIBLE
                }
            }
    }

    /**
     * Returns an [AdapterView.OnItemSelectedListener] object that gets the selected [Category]
     * from [categoryAdapter] and calls [getCategoryTagsFromFirestore] using that [Category] when
     * an item is selected.
     */
    private fun itemSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val selectedItem: Category = categoryAdapter.getItem(position)
            getCategoryTagsFromFirestore(selectedItem)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            TODO("Not yet implemented")
        }
    }

    /**
     * Updates the [note]'s data with the selected data of this dialog.
     */
    fun updateNote() {
        val category = categoryAdapter.getItem(binding.spinnerCategory.selectedItemPosition)
        //var tag = tagsAdapter.getItem(binding.spinnerTags.selectedItemPosition)
        val visible = if (binding.radioButtonPrivate.isChecked) { // gets the visibility
            Note.PRIVATE
        } else {
            Note.PUBLIC
        }

        val newData = mapOf(
            "categoryId" to category.id,
            "visible" to visible
        )

        firestore.collection(DBCollection.NOTES).document(note.id)
            .update(newData)
            .addOnSuccessListener {
                PopupBanner.make(
                    ownerContext,
                    PopupBanner.TYPE_SUCCESS,
                    ownerContext.getString(R.string.note_updated_successfully)
                ).show()

                this@NotePropertiesBottomSheetDialog.dismiss() // dismisses the dialog
            }
            .addOnFailureListener {
                PopupBanner.make(
                    ownerContext,
                    PopupBanner.TYPE_FAILURE,
                    ownerContext.getString(R.string.something_went_wrong_please_try_again)
                ).show()
            }
    }
}