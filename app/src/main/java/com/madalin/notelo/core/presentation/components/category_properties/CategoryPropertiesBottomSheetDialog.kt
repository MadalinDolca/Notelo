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
import com.madalin.notelo.core.domain.result.GetCategoryResult
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
 * [BottomSheetDialog] used to create or update a category. If [categoryId] is `null`, the
 * creation mode will be used, otherwise the update mode.
 *
 * @param ownerContext Caller's context
 * @param categoryId ID of the category to update
 */
class CategoryPropertiesBottomSheetDialog(
    ownerContext: Context,
    private var categoryId: String? = null
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
        if (categoryId == null) {
            initializeCreateMode()
        } else {
            getCategoryData()
        }
    }

    /**
     * Obtains the data of the category with the given [categoryId] and initializes the update mode.
     */
    private fun getCategoryData() {
        val id = categoryId
        if (id == null) {
            globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.could_not_update_the_category_because_the_category_id_is_null
            )
            return
        }

        scope.launch(Dispatchers.IO) {
            val result = localRepository.getCategoryById(id)
            when (result) {
                is GetCategoryResult.Success -> initializeUpdateMode(result.category)

                is GetCategoryResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_get_this_category_data
                )
            }
        }
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
     * Initializes the update mode of the dialog using this [category].
     * This will allow the user to **update** a category or to **delete** it.
     */
    private fun initializeUpdateMode(category: Category) {
        // adds the name to the EditText
        binding.editTextCategoryName.setText(category.name)

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
        binding.buttonCreateUpdate.setOnClickListener { updateCategoryAndClose(category) }
        binding.buttonCancelDelete.setOnClickListener { deleteCategoryAndClose(category) }
    }

    /**
     * Creates a new category with the data provided in this dialog and stores it in the database.
     */
    private fun createCategory() {
        val name = binding.editTextCategoryName.text.toString().trim()

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
     * Updates the given [category] with the data provided in this dialog and closes the dialog.
     */
    private fun updateCategoryAndClose(category: Category) {
        val newName = binding.editTextCategoryName.text.toString()

        // validates the category name
        if (!validateName(newName)) return

        val updatedCategory = category.copy(name = newName, color = newColor)
        scope.launch(Dispatchers.IO) {
            val result = localRepository.updateCategory(updatedCategory)
            when (result) {
                UpdateResult.Success -> {
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
     * Deletes the given [category] from the database and closes the dialog.
     */
    private fun deleteCategoryAndClose(category: Category) {
        scope.launch(Dispatchers.IO) {
            val result = localRepository.deleteCategoryAndRelatedData(category)
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