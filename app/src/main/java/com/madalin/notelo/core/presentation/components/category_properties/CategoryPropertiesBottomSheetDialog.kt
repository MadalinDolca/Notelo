package com.madalin.notelo.core.presentation.components.category_properties

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult
import com.madalin.notelo.core.domain.validation.CategoryValidator
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.databinding.LayoutCategoryPropertiesBottomsheetdialogBinding
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Date

/**
 * [BottomSheetDialog] used to create or update a category. If [givenCategory] is `null`, the
 * creation mode will be used, otherwise the update mode.
 *
 * @param ownerContext context from where the dialog is called
 * @param givenCategory the category to update
 */
class CategoryPropertiesBottomSheetDialog(
    ownerContext: Context,
    private var givenCategory: Category? = null
) : BottomSheetDialog(ownerContext), KoinComponent {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    lateinit var binding: LayoutCategoryPropertiesBottomsheetdialogBinding

    private val globalDriver: GlobalDriver by inject()
    private val localRepository: LocalContentRepository by inject()

    private var newColor: String? = null // the new color of the category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutCategoryPropertiesBottomsheetdialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED // fully opened sheet

        initializeColorPicker()
        if (givenCategory == null) initializeCreateMode()
        else initializeUpdateMode()
    }

    /**
     * Initializes the color picker and its listener that updates [newColor].
     */
    private fun initializeColorPicker() {
        // attaches the brightness slider to the color picker
        binding.colorPickerView.attachBrightnessSlider(binding.brightnessSlider)

        // gets the selected color from the color picker
        binding.colorPickerView.setColorListener(ColorEnvelopeListener { selectedColor, fromUser ->
            binding.cardViewColor.setCardBackgroundColor(selectedColor.color)
            newColor = "#${selectedColor.hexCode}"
        })
    }

    /**
     * Initialized the creation mode of the dialog.
     * This will allow to user to **create** new categories.
     */
    private fun initializeCreateMode() {
        binding.buttonCreateUpdate.text = context.getString(R.string.create)
        binding.buttonCancelDelete.text = context.getString(R.string.cancel)
        binding.buttonManageTags.visibility = View.GONE // hides the manage tags button

        // create and cancel button listeners
        binding.buttonCreateUpdate.setOnClickListener { createCategory() }
        binding.buttonCancelDelete.setOnClickListener { this.dismiss() }
    }

    /**
     * Initializes the update mode of the dialog.
     * This will allow the user to **update** a category or to **delete** it.
     */
    private fun initializeUpdateMode() {
        val category = givenCategory ?: return

        binding.editTextCategoryName.setText(category.name) // adds the name to the EditText

        // if the category has a color, then the picker sets it as the initial color
        if (category.color != null) {
            binding.colorPickerView.setInitialColor(Color.parseColor(category.color))
        }

        // sets the text of the buttons
        binding.buttonCreateUpdate.text = context.getString(R.string.update)
        binding.buttonCancelDelete.text = context.getString(R.string.delete)

        // manage tags button listener
        binding.buttonManageTags.setOnClickListener { ManageTagsDialog(context, category).show() }

        // update and delete button listeners
        binding.buttonCreateUpdate.setOnClickListener { updateCategory() }
        binding.buttonCancelDelete.setOnClickListener { deleteCategory() }
    }

    /**
     * Creates a new category with the data provided in this dialog and stores it in the database.
     */
    private fun createCategory() {
        val name = binding.editTextCategoryName.text.toString()

        if (!validateName(name)) return

        // creates the new category object to store
        val newCategory = Category(
            name = name,
            color = newColor,
            createdAt = Date(),
            updatedAt = null
        )

        // adds the new category to the database
        scope.launch(Dispatchers.IO) {
            val result = localRepository.upsertCategory(newCategory)
            when (result) {
                UpsertResult.Success -> {
                    globalDriver.showPopupBanner(PopupBanner.TYPE_SUCCESS, context.getString(R.string.category_created_successfully))
                    this@CategoryPropertiesBottomSheetDialog.dismiss()
                }

                is UpsertResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_create_the_category)
                )
            }
        }
    }

    /**
     * Updates the given [givenCategory] with the data provided in this dialog.
     */
    private fun updateCategory() {
        val currentCategory = givenCategory
        if (currentCategory == null) {
            globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.could_not_update_the_category_because_the_category_is_null
            )
            return
        }

        val newName = binding.editTextCategoryName.text.toString()

        // validates the category name
        if (!validateName(newName)) return

        val updatedCategory = currentCategory.copy(name = newName, color = newColor)
        scope.launch(Dispatchers.IO) {
            val result = localRepository.updateCategory(updatedCategory)
            when (result) {
                UpdateResult.Success -> {
                    // applies the data to the local category
                    givenCategory?.apply {
                        name = newName
                        color = newColor
                    }

                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_SUCCESS,
                        context.getString(R.string.category_updated_successfully)
                    )
                    this@CategoryPropertiesBottomSheetDialog.dismiss()
                }

                is UpdateResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_update_the_category)
                )
            }
        }
    }

    /**
     * Deletes the current [givenCategory] from the database and closes the dialog.
     */
    private fun deleteCategory() {
        val currentCategory = givenCategory
        if (currentCategory == null) {
            globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.could_not_delete_the_category_because_the_category_is_null
            )
            return
        }

        scope.launch(Dispatchers.IO) {
            val result = localRepository.deleteCategoryAndRelatedData(currentCategory)
            when (result) {
                DeleteResult.Success -> {
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_SUCCESS,
                        context.getString(R.string.category_deleted_successfully)
                    )
                    this@CategoryPropertiesBottomSheetDialog.dismiss()
                }

                is DeleteResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_delete_the_category)
                )
            }
        }
    }

    /*    private fun showManageTagsDialog() {
            val dialogBinding = LayoutManageTagsDialogBinding.inflate(layoutInflater)
            val items = mutableListOf("Item 1", "Item 2", "Item 3")
            val adapter = ItemAdapter(items, { position ->
                // Edit item
                val newText = editTextInput.text.toString()
                if (newText.isNotEmpty()) {
                    items[position] = newText
                    adapter.notifyItemChanged(position)
                }
            }, { position ->
                // Delete item
                items.removeAt(position)
                adapter.notifyItemRemoved(position)
            })

            dialogBinding.recyclerViewTags.layoutManager = LinearLayoutManager(context)
            dialogBinding.recyclerViewTags.adapter = adapter

            AlertDialog.Builder(context)
                .setView(dialogBinding.root)
                .setPositiveButton("Add") { _, _ ->
                    val newItem = editTextInput.text.toString()
                    if (newItem.isNotEmpty()) {
                        items.add(newItem)
                        adapter.notifyItemInserted(items.size - 1)
                        editTextInput.text.clear()
                    }
                }
                .create()
                .show()
        }*/

    /**
     * Validates the given category [name] and returns `true` if valid, `false` otherwise.
     */
    private fun validateName(name: String): Boolean {
        val result = CategoryValidator.validateName(name)
        when (result) {
            CategoryValidator.NameResult.Valid -> return true
            CategoryValidator.NameResult.Empty -> {
                binding.editTextCategoryName.error = context.getString(R.string.category_name_can_not_be_empty)
                binding.editTextCategoryName.requestFocus()
                return false
            }

            CategoryValidator.NameResult.InvalidLength -> {
                binding.editTextCategoryName.error = context.getString(
                    R.string.category_name_must_be_between_x_and_y_characters,
                    CategoryValidator.MIN_CATEGORY_NAME_LENGTH, CategoryValidator.MAX_CATEGORY_NAME_LENGTH
                )
                binding.editTextCategoryName.requestFocus()
                return false
            }
        }
    }
}