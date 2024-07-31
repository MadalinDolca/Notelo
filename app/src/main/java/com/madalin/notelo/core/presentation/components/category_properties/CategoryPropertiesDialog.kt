package com.madalin.notelo.core.presentation.components.category_properties

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult
import com.madalin.notelo.core.domain.validation.CategoryValidator
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.core.presentation.components.category_properties.CategoryPropertiesDialog.Companion.MODE_CREATE
import com.madalin.notelo.core.presentation.components.category_properties.CategoryPropertiesDialog.Companion.MODE_UPDATE
import com.madalin.notelo.databinding.LayoutCategoryPropertiesDialogBinding
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Date

/**
 * Dynamic [Dialog] used to create or update a category.
 * @param ownerContext from where it's called
 * @param mode [MODE_CREATE] to create a category, [MODE_UPDATE] to update a category
 */
class CategoryPropertiesDialog(
    private val ownerContext: Context,
    private val mode: String,
    private var givenCategory: Category? = null
) : Dialog(ownerContext), KoinComponent {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val binding = LayoutCategoryPropertiesDialogBinding.inflate(layoutInflater)

    private val globalDriver: GlobalDriver by inject()
    private val localRepository: LocalContentRepository by inject()

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
        binding.colorPickerView.attachBrightnessSlider(binding.brightnessSlider)

        // gets the selected color from the color picker
        binding.colorPickerView.setColorListener(ColorEnvelopeListener { selectedColor, fromUser ->
            binding.cardViewColor.setCardBackgroundColor(selectedColor.color)
            newColor = "#${selectedColor.hexCode}"
        })

        // determines the mode
        if (mode == MODE_CREATE) initializeCreateMode()
        else if (mode == MODE_UPDATE) initializeUpdateMode()

    }

    override fun dismiss() {
        super.dismiss()
        scope.cancel()
    }

    /**
     * Turns the [Dialog] into the creation mode. This will allow to user to create new categories.
     */
    private fun initializeCreateMode() {
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
     * Creates a new category with the provided data in this [Dialog] and stores it in the database.
     */
    private fun createCategory() {
        val name = binding.editTextCategoryName.text.toString()

        if (!validateName(name)) return

        // creates the new category object to store
        val newCategory = Category(name = name, color = newColor, createdAt = Date(), updatedAt = null)

        // adds the new category to the database
        scope.launch(Dispatchers.IO) {
            val result = localRepository.upsertCategory(newCategory)
            when (result) {
                UpsertResult.Success -> {
                    globalDriver.showPopupBanner(PopupBanner.TYPE_SUCCESS, ownerContext.getString(R.string.category_created_successfully))
                    this@CategoryPropertiesDialog.dismiss()
                }

                is UpsertResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_create_the_category)
                )
            }
        }
    }

    /**
     * Updates the given [givenCategory] with the provided data in the [Dialog].
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
                        ownerContext.getString(R.string.category_updated_successfully)
                    )
                    this@CategoryPropertiesDialog.dismiss()
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
                        ownerContext.getString(R.string.category_deleted_successfully)
                    )
                    this@CategoryPropertiesDialog.dismiss()
                }

                is DeleteResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_delete_the_category)
                )
            }
        }
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