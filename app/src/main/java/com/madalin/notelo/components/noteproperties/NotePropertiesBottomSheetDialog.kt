package com.madalin.notelo.components.noteproperties

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
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

/**
 * Class used to manage a given note through a [BottomSheetDialog].
 */
class NotePropertiesBottomSheetDialog(
    var ownerContext: Context,
    var note: Note
) : BottomSheetDialog(ownerContext) {

    lateinit var binding: LayoutNotePropertiesBottomsheetdialogBinding
    var firestore = Firebase.firestore
    var categoryAdapter = SpinnerCategoryAdapter()
    var categoriesList = mutableListOf<Category>()
    var tagsList = mutableListOf<Tag>() // list to store the selected category's tags
    val selectedTags = mutableListOf<Tag>() // list to store the selected tags by the user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutNotePropertiesBottomsheetdialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED // fully opened dialog

        // chooses the radio button to check
        if (note.visible == Note.VISIBLE_PRIVATE) {
            binding.radioButtonPrivate.isChecked = true
        } else {
            binding.radioButtonPublic.isChecked = true
        }

        // categories spinner
        binding.spinnerCategory.adapter = categoryAdapter // sets the adapter
        getCategoriesFromFirestore()
        binding.spinnerCategory.onItemSelectedListener = itemSelectedListener() // sets the item selection listener

        // listeners
        binding.buttonTags.setOnClickListener { showTagsDialog() }
        binding.buttonSave.setOnClickListener { updateNote() }
        binding.buttonDelete.setOnClickListener { deleteNote() }
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
                // adds an "uncategorized" entry; used when the user wants to remove the note from any category
                categoriesList.add(Category(id = Category.ID_UNCATEGORIZED, name = Category.NAME_UNCATEGORIZED))

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
     * to [tagsList]. Hides the tags selection button if the [category] doesn't have tags.
     * @param category used to query the database
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

                // if the category doesn't have any tags, then the tags button is hidden
                if (tagsList.isEmpty()) {
                    binding.textViewTags.visibility = View.GONE
                    binding.buttonTags.visibility = View.GONE
                } else {
                    binding.textViewTags.visibility = View.VISIBLE
                    binding.buttonTags.visibility = View.VISIBLE
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

        }
    }

    /**
     * Prepares the tag list, initializes the [AlertDialog] of the tags and shows it.
     */
    private fun showTagsDialog() {
        val checkedItems = BooleanArray(tagsList.size) { false } // array to mark the existent tags of the note

        // determines which tags the current note has and marks them
        for (i in tagsList.indices) {
            if (note.tags.contains(tagsList[i].id)) {
                checkedItems[i] = true
            }
        }

        val builder = AlertDialog.Builder(context, R.style.AlertDialogStyle)
        builder.setTitle(ownerContext.getString(R.string.select_tags))

        // passes the name of the tags to the check list
        builder.setMultiChoiceItems(tagsList.map { it.name }.toTypedArray(), checkedItems) { dialog, which, isChecked ->
            checkedItems[which] = isChecked // checks the tags that the note already has
        }

        // positive button - adds the selected tags to the selectedItems list
        builder.setPositiveButton("OK") { dialog, which ->
            selectedTags.clear()

            for (i in tagsList.indices) {
                if (checkedItems[i]) {
                    selectedTags.add(tagsList[i])
                }
            }
        }

        // negative button - cancels the dialog
        builder.setNegativeButton(context.getString(R.string.cancel), null)

        // show the dialog
        builder.create().show()
    }

    /**
     * Updates the [note]'s data with the selected data of this dialog.
     */
    private fun updateNote() {
        val categoryFromSpinner = categoryAdapter.getItem(binding.spinnerCategory.selectedItemPosition)
        val newCategoryId = if (categoryFromSpinner.id != Category.ID_UNCATEGORIZED) categoryFromSpinner.id else Category.ID_UNCATEGORIZED
        val newVisibility = if (binding.radioButtonPrivate.isChecked) Note.VISIBLE_PRIVATE else Note.VISIBLE_PUBLIC // gets the visibility from the radio buttons

        // creates the new data object used to update the note
        val newData = mapOf(
            "categoryId" to newCategoryId,
            "tags" to selectedTags.map { it.id },
            "visible" to newVisibility,
            "updatedAt" to null
        )

        // updates the note in the database
        note.id?.let { noteId ->
            firestore.collection(DBCollection.NOTES).document(noteId)
                .update(newData)
                .addOnSuccessListener {
                    // updates the local note
                    note.apply {
                        categoryId = newCategoryId
                        tags.clear()
                        tags.addAll(selectedTags.map { it.id })
                        visible = newVisibility
                    }

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

    private fun deleteNote() {
        note.id?.let { noteId ->
            firestore.collection(DBCollection.NOTES)
                .document(noteId).delete()
                .addOnSuccessListener {
                    PopupBanner.make(
                        ownerContext,
                        PopupBanner.TYPE_SUCCESS,
                        ownerContext.getString(R.string.note_deleted_successfully)
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
}