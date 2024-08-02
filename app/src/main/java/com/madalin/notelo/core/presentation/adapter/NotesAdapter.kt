package com.madalin.notelo.core.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.databinding.LayoutNoteCardBinding

/**
 * [RecyclerView.Adapter] that can display a [Note].
 * @param onNavigateToNote Function to call when the note is clicked.
 * @param onOpenNoteProperties Function to call when the note is long clicked.
 */
class NotesAdapter(
    private val onNavigateToNote: (Note) -> Unit,
    private val onOpenNoteProperties: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    private val notesList = mutableListOf<Note>()

    /**
     * Describes the view of an item and the metadata about its place in the [RecyclerView].
     */
    inner class NotesViewHolder(val binding: LayoutNoteCardBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding = LayoutNoteCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        val currentNote = notesList[position]

        with(holder) {
            binding.textViewTitle.text = currentNote.title
            binding.textViewContent.text = currentNote.content

            // shows the category name if it exists
            val category = currentNote.category
            if (category != null) {
                binding.textViewCategoryName.text = category.name
            } else {
                binding.imageViewCategory.visibility = View.GONE
                binding.textViewCategoryName.visibility = View.GONE
            }

            // shows the tags if they exist
            if (currentNote.tags.isNotEmpty()) {
                binding.textViewTags.text = currentNote.tags.joinToString(separator = ", ") { it.name }
            } else {
                binding.imageViewTag.visibility = View.GONE
                binding.textViewTags.visibility = View.GONE
            }

            // navigates to the note viewer with the given data
            binding.root.setOnClickListener {
                onNavigateToNote(currentNote)
            }

            // opens the note properties dialog on long click
            binding.root.setOnLongClickListener {
                onOpenNoteProperties(currentNote)
                return@setOnLongClickListener true
            }
        }
    }

    override fun getItemCount() = notesList.size

    /**
     * Updates the [notesList] with the given [newNotesList].
     */
    fun setNotesList(newNotesList: List<Note>) {
        notesList.clear()
        notesList.addAll(newNotesList)
    }
}