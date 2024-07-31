package com.madalin.notelo.core.presentation.components.category_properties

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.domain.validation.TagValidator
import com.madalin.notelo.databinding.LayoutTagRowBinding

class TagsListAdapter(
    private val tagsList: MutableList<Tag>,
    private val onUpdateTag: (tag: Tag, newName: String) -> Unit,
    private val onDeleteTag: (Tag) -> Unit
) : RecyclerView.Adapter<TagsListAdapter.TagViewHolder>() {

    inner class TagViewHolder(val binding: LayoutTagRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagViewHolder {
        val binding = LayoutTagRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagViewHolder, position: Int) {
        val currentTag = tagsList[position]
        var isEditing = false

        with(holder) {
            // initially the tag name is not editable
            binding.editTextTagName.apply {
                setText(currentTag.name)
                disableEditTextTagName(this@with)
            }

            binding.imageButtonEditUpdate.setOnClickListener {
                if (!isEditing) {
                    binding.imageButtonEditUpdate.setImageResource(R.drawable.ic_save)
                    binding.imageButtonDeleteCancel.setImageResource(R.drawable.ic_cancel_circle)
                    enableEditTextTagName(this@with)
                    isEditing = true
                } else {
                    val newTagName = binding.editTextTagName.text.toString()
                    if (!validateTagName(this@with, newTagName)) return@setOnClickListener
                    onUpdateTag(currentTag, newTagName)

                    binding.imageButtonEditUpdate.setImageResource(R.drawable.ic_edit)
                    binding.imageButtonDeleteCancel.setImageResource(R.drawable.ic_delete)
                    disableEditTextTagName(this@with)
                    isEditing = false
                }
            }

            binding.imageButtonDeleteCancel.setOnClickListener {
                if (!isEditing) {
                    onDeleteTag(currentTag)
                } else {
                    binding.imageButtonEditUpdate.setImageResource(R.drawable.ic_edit)
                    binding.imageButtonDeleteCancel.setImageResource(R.drawable.ic_delete)
                    disableEditTextTagName(this@with)
                    isEditing = false
                }
            }
        }
    }

    override fun getItemCount() = tagsList.size

    /**
     * Enables the EditText that contains the tag name.
     */
    private fun enableEditTextTagName(holder: TagViewHolder) {
        holder.binding.editTextTagName.isEnabled = true
        holder.binding.editTextTagName.setBackgroundResource(R.drawable.background_rounded)
    }

    /**
     * Disables the EditText that contains the tag name.
     */
    private fun disableEditTextTagName(holder: TagViewHolder) {
        holder.binding.editTextTagName.isEnabled = false
        holder.binding.editTextTagName.background = null
    }

    /**
     * Checks if the given [tagName] is valid.
     * @return `true` is valid, `false` otherwise.
     */
    private fun validateTagName(holder: TagViewHolder, tagName: String): Boolean {
        val result = TagValidator.validateName(tagName)
        when (result) {
            TagValidator.NameResult.Valid -> return true
            TagValidator.NameResult.Empty -> {
                holder.binding.editTextTagName.error = holder.binding.root.context.getString(
                    R.string.tag_name_can_not_be_empty
                )
                return false
            }

            TagValidator.NameResult.InvalidLength -> {
                holder.binding.editTextTagName.error = holder.binding.root.context.getString(
                    R.string.tag_name_must_be_between_x_and_y_characters,
                    TagValidator.MIN_TAG_NAME_LENGTH, TagValidator.MAX_TAG_NAME_LENGTH
                )
                return false
            }
        }
    }
}