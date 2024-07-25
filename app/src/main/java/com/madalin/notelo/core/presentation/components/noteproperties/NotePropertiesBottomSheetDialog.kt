package com.madalin.notelo.core.presentation.components.noteproperties

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.domain.repository.FirebaseContentRepository
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.databinding.LayoutNotePropertiesBottomsheetdialogBinding
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Class used to manage a given note through a [BottomSheetDialog].
 */
class NotePropertiesBottomSheetDialog(
    private var ownerContext: Context,
    private var selectedNote: Note
) : BottomSheetDialog(ownerContext), KoinComponent {
    lateinit var binding: LayoutNotePropertiesBottomsheetdialogBinding

    private val globalDriver: GlobalDriver by inject()
    private val repository: FirebaseContentRepository by inject()

    private var categoryAdapter = SpinnerCategoryAdapter()
    private var categoriesList = mutableListOf<Category>()
    private var tagsList = mutableListOf<Tag>() // list to store the selected category's tags
    private val selectedTags = mutableListOf<Tag>() // list to store the selected tags by the user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutNotePropertiesBottomsheetdialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED // fully opened dialog

        // chooses the radio button to check
        if (selectedNote.visible == Note.VISIBLE_PRIVATE) {
            binding.radioButtonPrivate.isChecked = true
        } else {
            binding.radioButtonPublic.isChecked = true
        }

        // categories spinner
        binding.spinnerCategory.adapter = categoryAdapter // sets the adapter
        populateCategoriesSpinner()
        binding.spinnerCategory.onItemSelectedListener = itemSelectedListener() // sets the item selection listener

        // listeners
        binding.buttonTags.setOnClickListener { showTagsDialog() }
        binding.buttonSave.setOnClickListener { updateNote() }
        binding.buttonDelete.setOnClickListener { deleteNoteAndClose() }
    }

    /**
     * Queries the database for the categories list and adds all entries to [categoriesList]. The
     * [categoriesList] is later used to update the [categoryAdapter].
     */
    private fun populateCategoriesSpinner() {
        val userId = globalDriver.currentUser.value?.id
        if (userId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_get_the_categories_because_the_user_id_is_null)
            return
        }

        repository.getCategoriesByUserId(
            userId,
            onSuccess = { categories ->
                categoriesList.clear()
                // adds an "uncategorized" entry; used when the user wants to remove the note from any category
                categoriesList.add(Category(id = Category.ID_UNCATEGORIZED, name = Category.NAME_UNCATEGORIZED))
                categoriesList.addAll(categories) // adds the obtained categories

                categoryAdapter.setCategoriesList(categoriesList)
                categoryAdapter.notifyDataSetChanged()

                // gets the position of the note's category in the list and sets the spinner's selection
                val position = categoriesList.indexOfFirst { it.id == selectedNote.categoryId }
                binding.spinnerCategory.setSelection(position)
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: R.string.could_not_get_the_categories)
            }
        )
    }

    /**
     * Queries the database for the tags associated with the given [category] and adds all entries
     * to [tagsList]. Hides the tags selection button if the [category] doesn't have tags.
     * @param category used to query the database
     */
    fun getCategoryTags(category: Category) {
        val categoryId = category.id
        if (categoryId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_get_the_tags_because_the_category_id_is_null)
            return
        }

        repository.getTagsByCategoryId(
            categoryId,
            onSuccess = { tags ->
                tagsList.clear()
                tagsList.addAll(tags)

                // if the category doesn't have any tags, hides the "Tags button"
                if (tagsList.isEmpty()) {
                    binding.textViewTags.visibility = View.GONE
                    binding.buttonTags.visibility = View.GONE
                } else {
                    binding.textViewTags.visibility = View.VISIBLE
                    binding.buttonTags.visibility = View.VISIBLE
                }
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: context.getString(R.string.could_not_get_the_tags_for_this_category))
            }
        )
    }

    /**
     * Returns an [AdapterView.OnItemSelectedListener] object that gets the selected [Category]
     * from [categoryAdapter] and calls [getCategoryTags] using that [Category] when
     * an item is selected.
     */
    private fun itemSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val selectedItem: Category = categoryAdapter.getItem(position)
            getCategoryTags(selectedItem)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    /**
     * Prepares the tag list, initializes the [AlertDialog] of the tags and shows it.
     */
    private fun showTagsDialog() {
        val checkedItems = BooleanArray(tagsList.size) { false } // array to mark the existent tags of the note

        // determines which tags the current note has and marks them
        for (i in tagsList.indices) {
            if (selectedNote.tags.contains(tagsList[i].id)) {
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
     * Updates the [selectedNote]'s data with the selected data of this dialog.
     */
    private fun updateNote() {
        val noteId = selectedNote.id
        if (noteId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_update_the_note_because_the_note_id_is_null)
            return
        }

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

        // update the note in the database
        repository.updateNote(
            noteId, newData,
            onSuccess = {
                // update the local note
                selectedNote.apply {
                    categoryId = newCategoryId
                    tags.clear()
                    tags.addAll(selectedTags.map { it.id })
                    visible = newVisibility
                }

                globalDriver.showPopupBanner(PopupBanner.TYPE_SUCCESS, context.getString(R.string.note_updated_successfully))
                this@NotePropertiesBottomSheetDialog.dismiss() // dismiss the dialog
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: context.getString(R.string.could_not_update_the_note))
            }
        )
    }

    /**
     * Deletes the [selectedNote] from the database and closes the dialog.
     */
    private fun deleteNoteAndClose() {
        val noteId = selectedNote.id
        if (noteId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_delete_the_note_because_the_note_id_is_null)
            return
        }

        repository.deleteNote(
            noteId,
            onSuccess = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_SUCCESS, context.getString(R.string.note_deleted_successfully))
                this@NotePropertiesBottomSheetDialog.dismiss() // dismiss the dialog
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: context.getString(R.string.could_not_delete_the_note))
            }
        )
    }
}