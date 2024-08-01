package com.madalin.notelo.core.presentation.components.category_properties

import android.app.Dialog
import android.content.Context
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.GetTagsResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult
import com.madalin.notelo.core.domain.validation.TagValidator
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.databinding.LayoutManageTagsDialogBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Date

class ManageTagsDialog(
    ownerContext: Context,
    private val givenCategory: Category
) : Dialog(ownerContext), KoinComponent {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val binding = LayoutManageTagsDialogBinding.inflate(layoutInflater)

    private val globalDriver: GlobalDriver by inject()
    private val localRepository: LocalContentRepository by inject()

    private val tagsList = mutableListOf<Tag>()

    private val tagsAdapter = TagsListAdapter(
        tagsList = tagsList,
        onUpdateTag = { tagId, tagName -> updateTag(tagId, tagName) },
        onDeleteTag = { tag -> deleteTag(tag) }
    )

    init {
        setContentView(binding.root)

        // gets the width of the screen and applies it to the dialog
        val width = context.resources.displayMetrics.widthPixels
        window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        // recycler view preparations
        binding.recyclerViewTags.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewTags.adapter = tagsAdapter

        // obtains the category tags from the database
        getCategoryTags()

        // listeners
        binding.imageButtonAddTag.setOnClickListener { createTag(binding.editTextAddTag.text.toString()) }
        binding.buttonClose.setOnClickListener { this.dismiss() }
    }

    /**
     * Obtains the tags of [givenCategory] from the database and notifies the [tagsAdapter].
     */
    private fun getCategoryTags() {
        scope.launch(Dispatchers.IO) {
            val result = localRepository.getTagsByCategoryId(givenCategory.id)
            when (result) {
                is GetTagsResult.Success -> {
                    tagsList.clear()
                    tagsList.addAll(result.tags)

                    withContext(Dispatchers.Main) {
                        tagsAdapter.notifyDataSetChanged()
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
     * Creates a new tag and saves it in the database if the given [tagName] is valid.
     */
    private fun createTag(tagName: String) {
        if (!validateTagName(tagName)) return // checks if the provided data is valid
        val newTag = Tag(name = tagName, categoryId = givenCategory.id)

        scope.launch(Dispatchers.IO) {
            val result = localRepository.upsertTag(newTag)
            when (result) {
                UpsertResult.Success -> {
                    binding.editTextAddTag.text.clear()
                    tagsList.add(newTag)

                    // updates the adapter on the original thread
                    withContext(Dispatchers.Main) {
                        tagsAdapter.notifyItemInserted(tagsList.size - 1)
                    }
                }

                is UpsertResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_create_the_tag)
                )
            }
        }
    }

    /**
     * Updates this [tag] with the given [tagName] in the database and notifies the [tagsAdapter].
     */
    private fun updateTag(tag: Tag, tagName: String) {
        val newData = tag.copy(name = tagName, updatedAt = Date())

        scope.launch {
            val result = localRepository.updateTag(newData)
            when (result) {
                UpdateResult.Success -> {
                    val updatedTagIndex = tagsList.indexOf(tag)
                    tagsList[updatedTagIndex] = newData

                    withContext(Dispatchers.Main) {
                        tagsAdapter.notifyItemChanged(updatedTagIndex)
                    }
                }

                is UpdateResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_update_the_tag)
                )
            }
        }
    }

    /**
     * Deletes the given [tag] from the database and notifies the [tagsAdapter].
     */
    private fun deleteTag(tag: Tag) {
        scope.launch {
            val result = localRepository.deleteTagAndRelatedData(tag)
            when (result) {
                DeleteResult.Success -> {
                    val removedTagIndex = tagsList.indexOf(tag)
                    tagsList.removeAt(removedTagIndex)

                    withContext(Dispatchers.Main) {
                        tagsAdapter.notifyItemRemoved(removedTagIndex)
                    }
                }

                is DeleteResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: context.getString(R.string.could_not_delete_the_tag)
                )
            }
        }
    }

    /**
     * Checks if the given [tagName] is valid.
     * @return `true` is valid, `false` otherwise.
     */
    private fun validateTagName(tagName: String): Boolean {
        val result = TagValidator.validateName(tagName)
        when (result) {
            TagValidator.NameResult.Valid -> return true

            TagValidator.NameResult.InvalidLength -> {
                binding.editTextAddTag.error = context.getString(
                    R.string.tag_name_must_be_between_x_and_y_characters,
                    TagValidator.MIN_TAG_NAME_LENGTH, TagValidator.MAX_TAG_NAME_LENGTH
                )
                binding.editTextAddTag.requestFocus()
                return false
            }

            TagValidator.NameResult.Empty -> {
                binding.editTextAddTag.error = context.getString(R.string.tag_name_can_not_be_empty)
                binding.editTextAddTag.requestFocus()
                return false
            }
        }
    }
}