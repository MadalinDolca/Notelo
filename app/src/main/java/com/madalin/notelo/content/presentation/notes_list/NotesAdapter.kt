package com.madalin.notelo.content.presentation.notes_list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.presentation.components.noteproperties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.databinding.LayoutNoteCardBinding
import com.madalin.notelo.home.presentation.HomeFragmentDirections

class NotesAdapter(
    var context: Context?,
    private val navController: NavController
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
        with(holder) {
            val thisNote = notesList[position]

            binding.textViewTitle.text = thisNote.title
            binding.textViewContent.text = thisNote.content
            binding.imageViewTag.visibility = View.GONE
            binding.textViewTags.visibility = View.GONE

            if (notesList[position].categoryId != null) {
                binding.textViewCategoryName.text = thisNote.categoryId
            }

            // opens the note with the given data
            binding.root.setOnClickListener {
                val action = HomeFragmentDirections.actionGlobalNoteViewerFragment(thisNote)
                navController.navigate(action) //navController.navigate(R.id.noteViewerFragment)
            }

            // open the properties dialog on long click
            binding.root.setOnLongClickListener {
                val context = context ?: return@setOnLongClickListener true
                NotePropertiesBottomSheetDialog(context, thisNote).show()
                return@setOnLongClickListener true
            }
        }
    }

    override fun getItemCount() = notesList.size

    fun setNotesList(newNotesList: List<Note>) {
        notesList.clear()
        notesList.addAll(newNotesList)
    }
}