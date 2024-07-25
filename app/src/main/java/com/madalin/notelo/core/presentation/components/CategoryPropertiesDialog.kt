package com.madalin.notelo.core.presentation.components

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.repository.FirebaseContentRepository
import com.madalin.notelo.core.domain.validation.CategoryValidator
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.CategoryPropertiesDialog.Companion.MODE_CREATE
import com.madalin.notelo.core.presentation.components.CategoryPropertiesDialog.Companion.MODE_UPDATE
import com.madalin.notelo.databinding.LayoutCategoryPropertiesDialogBinding
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Dynamic [Dialog] used to create or update a category.
 * @param ownerContext from where it's called
 * @param mode [MODE_CREATE] to create a note, [MODE_UPDATE] to update a note
 */
class CategoryPropertiesDialog(
    private val ownerContext: Context,
    private val mode: String,
    private var givenCategory: Category? = null
) : Dialog(ownerContext), KoinComponent {
    private val binding = LayoutCategoryPropertiesDialogBinding.inflate(layoutInflater)

    private val globalDriver: GlobalDriver by inject()
    private val repository: FirebaseContentRepository by inject()

    private var newColor: String? = null

    /**
     * Determines on which mode should the dialog turn into.
     */
    companion object {
        const val MODE_CREATE = "MODE_CREATE"
        const val MODE_UPDATE = "MODE_UPDATE"
    }

    init {
        setContentView(binding.root) // sets the dialog layout

        // gets the width of the screen and applies it to the dialog
        val width = ownerContext.resources.displayMetrics.widthPixels
        window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // attaches the brightness slider to the color picker
        binding.colorPickerView.attachBrightnessSlider(binding.brightnessSlide)

        // gets the selected color from the color picker
        binding.colorPickerView.setColorListener(ColorEnvelopeListener { selectedColor, fromUser ->
            binding.cardViewColor.setCardBackgroundColor(selectedColor.color)
            newColor = "#${selectedColor.hexCode}"
        })

        // determines the mode
        if (mode == MODE_CREATE) initializeCreateMode()
        else if (mode == MODE_UPDATE) initializeUpdateMode()

    }

    /**
     * Turns the [Dialog] into the creation mode. This will allow to user to create new categories.
     */
    private fun initializeCreateMode() {
        binding.radioButtonPrivate.isChecked = true // marks the visibility button as private
        binding.buttonCreateUpdate.text = context.getString(R.string.create)
        binding.buttonCancelDelete.text = context.getString(R.string.cancel)

        // create and cancel button listeners
        binding.buttonCreateUpdate.setOnClickListener { createCategory() }
        binding.buttonCancelDelete.setOnClickListener { this.dismiss() }
    }

    /**
     * Turns the [Dialog] into the update mode. This will allow the user to update a category or to delete it.
     */
    private fun initializeUpdateMode() {
        val category = givenCategory ?: return

        binding.editTextCategoryName.setText(category.name) // adds the name to the EditText

        // checks the corresponding visibility button according to the visibility of the category
        if (category.visible == Category.VISIBLE_PRIVATE) {
            binding.radioButtonPrivate.isChecked = true
        } else {
            binding.radioButtonPublic.isChecked = true
        }

        // if the category has a color, then the picker sets it as a initial color
        if (category.color != null) {
            binding.colorPickerView.setInitialColor(Color.parseColor(category.color))
        }

        // sets the text of the buttons
        binding.buttonCreateUpdate.text = context.getString(R.string.update)
        binding.buttonCancelDelete.text = context.getString(R.string.delete)

        // update and delete button listeners
        binding.buttonCreateUpdate.setOnClickListener { updateCategory() }
        binding.buttonCancelDelete.setOnClickListener { deleteCategory() }
    }

    /**
     * Builds a new [Category] with the provided data in this [Dialog] and stores it in the database.
     */
    private fun createCategory() {
        val userId = globalDriver.currentUser.value?.id
        if (userId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_create_a_new_category_because_the_user_id_is_null)
            return
        }

        val name = binding.editTextCategoryName.text.toString()
        val visible = if (binding.radioButtonPrivate.isChecked) Category.VISIBLE_PRIVATE else Category.VISIBLE_PUBLIC

        // validates the category name
        if (!validateName(name)) {
            return
        }

        // creates the new category object to store
        val newCategory = Category(userId = userId, name = name, color = newColor, visible = visible)

        // adds the new category to the database
        repository.createCategory(
            newCategory,
            onSuccess = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_SUCCESS, ownerContext.getString(R.string.category_created_successfully))
                this.dismiss()
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: context.getString(R.string.could_not_create_the_category))
            }
        )
    }

    /**
     * Updates the given [givenCategory] with the provided data in the [Dialog].
     */
    private fun updateCategory() {
        val categoryId = givenCategory?.id
        if (categoryId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_update_the_category_because_the_category_id_is_null)
            return
        }

        val newName = binding.editTextCategoryName.text.toString()
        val newVisibility = if (binding.radioButtonPrivate.isChecked) Category.VISIBLE_PRIVATE else Category.VISIBLE_PUBLIC

        // validates the category name
        if (!validateName(newName)) {
            return
        }

        // creates a map with the new data to update
        val newData = mapOf(
            "name" to newName,
            "color" to newColor,
            "visible" to newVisibility
        )

        repository.updateCategory(
            categoryId, newData,
            onSuccess = {
                // applies the data to the local category
                givenCategory?.apply {
                    name = newName
                    color = newColor
                    visible = newVisibility
                }

                globalDriver.showPopupBanner(PopupBanner.TYPE_SUCCESS, ownerContext.getString(R.string.category_created_successfully))
                this.dismiss() // dismisses the dialog
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: context.getString(R.string.could_not_update_the_category))
            }
        )
    }

    /**
     * Deletes the current [givenCategory] from the database.
     */
    private fun deleteCategory() {
        val categoryId = givenCategory?.id
        if (categoryId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_delete_the_category_because_the_category_id_is_null)
            return
        }

        repository.deleteCategory(
            categoryId,
            onSuccess = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_SUCCESS, ownerContext.getString(R.string.category_updated_successfully))
                this.dismiss() // dismisses the dialog
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: context.getString(R.string.could_not_delete_the_category))
            }
        )
    }

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