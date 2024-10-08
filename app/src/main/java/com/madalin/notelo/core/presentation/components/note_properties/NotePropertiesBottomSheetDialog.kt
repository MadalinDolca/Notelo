package com.madalin.notelo.core.presentation.components.note_properties

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.repository.remote.FirebaseContentRepository
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.GetCategoriesResult
import com.madalin.notelo.core.domain.result.GetNoteResult
import com.madalin.notelo.core.domain.result.GetTagsResult
import com.madalin.notelo.core.domain.result.MoveNoteResult
import com.madalin.notelo.core.domain.result.TagsReplaceResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.databinding.LayoutNotePropertiesBottomsheetdialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * [BottomSheetDialog] used to manage the properties of the note that has this [noteId].
 *
 * @param ownerContext Caller's context
 * @param noteId ID of the note to manage
 */
class NotePropertiesBottomSheetDialog(
    private var ownerContext: Context,
    private var noteId: String
) : BottomSheetDialog(ownerContext), KoinComponent {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    lateinit var binding: LayoutNotePropertiesBottomsheetdialogBinding

    private val globalDriver: GlobalDriver by inject()
    private val localRepository: LocalContentRepository by inject()
    private val firebaseRepository: FirebaseContentRepository by inject()

    private var categoryAdapter = SpinnerCategoryAdapter()
    private var spinnerCategoryTags = mutableListOf<Tag>() // list to store the tags of the spinner selected category
    private val dialogSelectedTags = mutableListOf<Tag>() // list to store the selected tags in the dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutNotePropertiesBottomsheetdialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED // fully opened dialog

        getNoteData()
    }

    /**
     * Obtains the data of the note with the given [noteId] and initializes the dialog.
     */
    private fun getNoteData() {
        scope.launch(Dispatchers.IO) {
            val result = localRepository.getNoteWithCategoryAndTagsByNoteId(noteId)
            when (result) {
                is GetNoteResult.Success -> initializeDialog(result.note)

                is GetNoteResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_get_the_note_data
                )
            }
        }
    }

    /**
     * Initializes the dialog with the given [note] data.
     */
    private fun initializeDialog(note: Note) {
        // adds the tags that the note already has to avoid their deletion on update when the
        // tags selection dialog hasn't been invoked
        dialogSelectedTags.addAll(note.tags)

        // chooses what visibility radio button to check
        if (!note.public) {
            binding.radioButtonPrivate.isChecked = true
        } else {
            binding.radioButtonPublic.isChecked = true
        }

        // categories spinner
        binding.spinnerCategory.adapter = categoryAdapter
        populateCategoriesSpinner(note.categoryId)
        binding.spinnerCategory.onItemSelectedListener = itemSelectedListener() // sets the item selection listener

        // listeners
        binding.buttonSelectTags.setOnClickListener { showTagsSelectionDialog(note) }
        binding.buttonSave.setOnClickListener { updateNoteAndDismiss(note) }
        binding.buttonDelete.setOnClickListener { deleteNoteAndDismiss(note) }
    }

    /**
     * Obtains the categories from the database and updates the [categoryAdapter]. Sets the category
     * spinner position to the [noteCategoryId].
     */
    private fun populateCategoriesSpinner(noteCategoryId: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = localRepository.getCategories()
            when (result) {
                is GetCategoriesResult.Success -> {
                    // adds the "uncategorized" entry and the obtained categories
                    val categoriesList = mutableListOf<Category>()
                    categoriesList.add(Category.subUncategorized())
                    categoriesList.addAll(result.categories)

                    // updates the spinner adapter
                    withContext(Dispatchers.Main) {
                        categoryAdapter.setCategoriesList(categoriesList)
                        categoryAdapter.notifyDataSetChanged()
                    }

                    // gets the position of the note's category in the list and sets the spinner's selection
                    val position = categoriesList.indexOfFirst { it.id == noteCategoryId }
                    binding.spinnerCategory.setSelection(position)
                }

                is GetCategoriesResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_get_the_categories
                )
            }
        }
    }

    /**
     * Returns an [AdapterView.OnItemSelectedListener] object that gets the selected category from
     * [categoryAdapter] and calls [getCategoryTags] using that category when an item is selected.
     */
    private fun itemSelectedListener() = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val selectedItem: Category = categoryAdapter.getItem(position)
            getCategoryTags(selectedItem)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {}
    }

    /**
     * Obtains the tags associated with the given [category] and adds all entries to [spinnerCategoryTags].
     * Hides the tags selection button if the [category] doesn't have tags.
     */
    fun getCategoryTags(category: Category) {
        // hides the "Select tags button" and cancels fetching if the category is "uncategorized"
        if (category.id == Category.ID_UNCATEGORIZED) {
            binding.textViewTags.visibility = View.GONE
            binding.buttonSelectTags.visibility = View.GONE
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val result = localRepository.getTagsByCategoryId(category.id)
            when (result) {
                is GetTagsResult.Success -> {
                    // clears the list and adds the obtained tags
                    spinnerCategoryTags.clear()
                    spinnerCategoryTags.addAll(result.tags)

                    // if the category doesn't have any tags, hides the "Select tags button"
                    withContext(Dispatchers.Main) {
                        if (spinnerCategoryTags.isEmpty()) {
                            binding.textViewTags.visibility = View.GONE
                            binding.buttonSelectTags.visibility = View.GONE
                        } else {
                            binding.textViewTags.visibility = View.VISIBLE
                            binding.buttonSelectTags.visibility = View.VISIBLE
                        }
                    }
                }

                is GetTagsResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_get_the_tags_for_this_category)
                )
            }
        }
    }

    /**
     * Prepares the tags list of this [note], initializes the tags [AlertDialog] and shows it.
     */
    private fun showTagsSelectionDialog(note: Note) {
        val dialogCheckedTags = BooleanArray(spinnerCategoryTags.size) { false } // array to mark the existent tags of the note

        // determines which tags the current note has and marks them
        for (i in spinnerCategoryTags.indices) {
            if (note.tags.contains(spinnerCategoryTags[i])) {
                dialogCheckedTags[i] = true
            }
        }

        val builder = AlertDialog.Builder(context, R.style.AlertDialogStyle)
        builder.setTitle(ownerContext.getString(R.string.select_tags))

        // passes the name of the tags to the check list
        builder.setMultiChoiceItems(spinnerCategoryTags.map { it.name }.toTypedArray(), dialogCheckedTags) { dialog, which, isChecked ->
            dialogCheckedTags[which] = isChecked // checks the tags that the note already has
        }

        // positive button - adds the selected tags to the selectedItems list
        builder.setPositiveButton(context.getString(R.string.confirm)) { dialog, which ->
            dialogSelectedTags.clear()

            for (i in spinnerCategoryTags.indices) {
                if (dialogCheckedTags[i]) {
                    dialogSelectedTags.add(spinnerCategoryTags[i])
                }
            }
        }

        // negative button - cancels the dialog
        builder.setNegativeButton(context.getString(R.string.cancel), null)

        // shows the dialog
        builder.create().show()
    }

    /**
     * Updates given [note] with the selected data from this dialog and closes the dialog.
     */
    private fun updateNoteAndDismiss(note: Note) {
        val categoryFromSpinner = categoryAdapter.getItem(binding.spinnerCategory.selectedItemPosition)
        val visibilityFromRadioButton = !binding.radioButtonPrivate.isChecked

        lifecycleScope.launch(Dispatchers.IO) {
            // if the visibility has been changed, the note will be updated as well
            if (note.public != visibilityFromRadioButton) {
                updateNoteVisibility(note.id, visibilityFromRadioButton)
            }

            // if the category remains the same, only the tags will be replaced
            if (note.categoryId == categoryFromSpinner.id) {
                replaceNoteTags(note, dialogSelectedTags)
            } else {
                // the category has been changed, the tags will be replaced as well
                moveNoteToCategoryWithTags(note, categoryFromSpinner, dialogSelectedTags)
            }
        }
    }

    /**
     * Updates the visibility of the note with the given [noteId] to [isPublic] in both the local
     * and remote database.
     */
    private suspend fun updateNoteVisibility(noteId: String, isPublic: Boolean) {
        val result = localRepository.updateNoteVisibility(noteId, isPublic)
        when (result) {
            UpdateResult.Success -> updateNoteVisibilityRemote(noteId, isPublic)

            is UpdateResult.Error -> globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                result.message ?: R.string.could_not_change_the_note_visibility
            )
        }
    }

    /**
     * Updates the visibility of the note with the given [noteId] to [isPublic] in the remote database.
     */
    private suspend fun updateNoteVisibilityRemote(noteId: String, isPublic: Boolean) {
        val newData = mapOf("public" to isPublic)

        scope.launch {
            val result = firebaseRepository.updateNote(noteId, newData)
            when (result) {
                UpdateResult.Success -> {}
                is UpdateResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_change_the_note_visibility
                )
            }
        }
    }

    /**
     * Replaces the tags of the given [note] with the given [tags] in the database.
     */
    private suspend fun replaceNoteTags(note: Note, tags: List<Tag>) {
        val result = localRepository.replaceNoteTags(note, tags)
        when (result) {
            TagsReplaceResult.Success -> {
                globalDriver.showPopupBanner(
                    PopupBanner.TYPE_SUCCESS,
                    context.getString(R.string.note_updated_successfully)
                )
                this@NotePropertiesBottomSheetDialog.dismiss()
            }

            is TagsReplaceResult.Error -> globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                result.message ?: context.getString(R.string.could_not_update_the_tags)
            )
        }
    }

    /**
     * Moves the given [note] to the given [category] with the given [tags] in the database.
     */
    private suspend fun moveNoteToCategoryWithTags(note: Note, category: Category, tags: List<Tag>) {
        val result = localRepository.moveNoteToCategoryWithTags(note, category, tags)
        when (result) {
            MoveNoteResult.Success -> {
                globalDriver.showPopupBanner(
                    PopupBanner.TYPE_SUCCESS,
                    context.getString(R.string.note_updated_successfully)
                )
                this@NotePropertiesBottomSheetDialog.dismiss()
            }

            is MoveNoteResult.Error -> globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                result.message ?: context.getString(R.string.could_not_update_the_note)
            )
        }
    }

    /**
     * Deletes the given [note] from both the local and remote database and closes the dialog.
     */
    private fun deleteNoteAndDismiss(note: Note) {
        lifecycleScope.launch(Dispatchers.IO) {
            val result = localRepository.deleteNoteAndRelatedData(note)
            when (result) {
                DeleteResult.Success -> {
                    deleteNoteRemote(note.id)
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_SUCCESS,
                        context.getString(R.string.note_deleted_successfully)
                    )
                    this@NotePropertiesBottomSheetDialog.dismiss()
                }

                is DeleteResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_delete_the_note)
                )
            }
        }
    }

    /**
     * Deletes the note with the given [noteId] from the remote database.
     */
    private fun deleteNoteRemote(noteId: String) {
        scope.launch(Dispatchers.IO) {
            val result = firebaseRepository.deleteNote(noteId)
            when (result) {
                DeleteResult.Success -> {}
                is DeleteResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_delete_backed_up_note
                )
            }
        }
    }
}