package com.madalin.notelo.components.categoryproperties

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.R
import com.madalin.notelo.components.PopupBanner
import com.madalin.notelo.components.categoryproperties.CategoryPropertiesDialog.Companion.MODE_CREATE
import com.madalin.notelo.components.categoryproperties.CategoryPropertiesDialog.Companion.MODE_UPDATE
import com.madalin.notelo.databinding.LayoutCategoryPropertiesDialogBinding
import com.madalin.notelo.models.Category
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.DBCollection
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener

/**
 * Dynamic [Dialog] used to create or update a category.
 * @param ownerContext from where it's called
 * @param mode [MODE_CREATE] to create a note, [MODE_UPDATE] to update a note
 */
class CategoryPropertiesDialog(
    var ownerContext: Context,
    var mode: String,
    var givenCategory: Category? = null
) : Dialog(ownerContext) {

    var binding: LayoutCategoryPropertiesDialogBinding
    val firestore = Firebase.firestore
    var newColor: String? = null

    /**
     * Determines on which mode should the dialog turn into.
     */
    companion object {
        const val MODE_CREATE = "MODE_CREATE"
        const val MODE_UPDATE = "MODE_UPDATE"
    }

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.layout_category_properties_dialog, null)
        binding = LayoutCategoryPropertiesDialogBinding.inflate(inflater)
        setContentView(binding.root) // sets the dialog view

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
        binding.editTextCategoryName.setText(givenCategory!!.name) // adds the name to the EditText

        // checks the corresponding visibility button according to the visibility of the category
        if (givenCategory!!.visible == Category.VISIBLE_PRIVATE) {
            binding.radioButtonPrivate.isChecked = true
        } else {
            binding.radioButtonPublic.isChecked = true
        }

        // if the category has a color, then the picker sets it as a initial color
        if (givenCategory!!.color != null) {
            binding.colorPickerView.setInitialColor(Color.parseColor(givenCategory!!.color))
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
        val name = binding.editTextCategoryName.text.toString()
        val visible = if (binding.radioButtonPrivate.isChecked) Category.VISIBLE_PRIVATE else Category.VISIBLE_PUBLIC

        // validates the category name
        if (name.length < 3) {
            binding.editTextCategoryName.error = ownerContext.getString(R.string.category_name_is_too_short)
            binding.editTextCategoryName.requestFocus()
            return
        }

        // creates the new category object to store
        val newCategory = Category(userId = UserData.id, name = name, color = newColor, visible = visible)

        // adds the new category to the database
        firestore.collection(DBCollection.CATEGORIES).add(newCategory)
            .addOnSuccessListener {
                PopupBanner.make(
                    ownerContext, PopupBanner.TYPE_SUCCESS,
                    ownerContext.getString(R.string.category_created_successfully)
                )

                this.dismiss()
            }
            .addOnFailureListener {
                PopupBanner.make(
                    ownerContext, PopupBanner.TYPE_FAILURE,
                    ownerContext.getString(R.string.something_went_wrong_please_try_again)
                )
            }
    }

    /**
     * Updates the given [givenCategory] with the provided data in the [Dialog].
     */
    private fun updateCategory() {
        val newName = binding.editTextCategoryName.text.toString()
        val newVisibility = if (binding.radioButtonPrivate.isChecked) Category.VISIBLE_PRIVATE else Category.VISIBLE_PUBLIC

        // validates the category name
        if (newName.length < 3) {
            binding.editTextCategoryName.error = ownerContext.getString(R.string.category_name_is_too_short)
            binding.editTextCategoryName.requestFocus()
            return
        }

        // creates a map with the new data to update
        val newData = mapOf(
            "name" to newName,
            "color" to newColor,
            "visible" to newVisibility
        )

        givenCategory?.id?.let { categoryId ->
            firestore.collection(DBCollection.CATEGORIES)
                .document(categoryId).update(newData)
                .addOnSuccessListener {
                    // applies the data to the local category
                    givenCategory!!.apply {
                        name = newName
                        color = newColor
                        visible = newVisibility
                    }

                    PopupBanner.make(
                        ownerContext, PopupBanner.TYPE_SUCCESS,
                        ownerContext.getString(R.string.category_created_successfully)
                    )

                    this.dismiss() // dismisses the dialog
                }
                .addOnFailureListener {
                    PopupBanner.make(
                        ownerContext, PopupBanner.TYPE_FAILURE,
                        ownerContext.getString(R.string.something_went_wrong_please_try_again)
                    )
                }
        }
    }

    /**
     * Deletes the given [givenCategory] from the database.
     */
    private fun deleteCategory() {
        givenCategory?.id?.let { categoryId ->
            firestore.collection(DBCollection.CATEGORIES)
                .document(categoryId).delete()
                .addOnSuccessListener {
                    PopupBanner.make(
                        ownerContext, PopupBanner.TYPE_SUCCESS,
                        ownerContext.getString(R.string.category_updated_successfully)
                    )

                    this.dismiss() // dismisses the dialog
                }
                .addOnFailureListener {
                    PopupBanner.make(
                        ownerContext, PopupBanner.TYPE_FAILURE,
                        ownerContext.getString(R.string.something_went_wrong_please_try_again)
                    )
                }
        }
    }
}