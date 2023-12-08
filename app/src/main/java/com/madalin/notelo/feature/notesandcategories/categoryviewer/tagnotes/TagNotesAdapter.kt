package com.madalin.notelo.feature.notesandcategories.categoryviewer.tagnotes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.madalin.notelo.MainFragmentDirections
import com.madalin.notelo.component.noteproperties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.databinding.LayoutNoteCardBinding
import com.madalin.notelo.model.Note

class TagNotesAdapter(
    var context: Context?,
    private var notesList: MutableList<Note>,
    private val navController: NavController
) : RecyclerView.Adapter<TagNotesAdapter.TagNotesViewHolder>() {

    inner class TagNotesViewHolder(var binding: LayoutNoteCardBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagNotesViewHolder {
        val binding = LayoutNoteCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagNotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TagNotesViewHolder, position: Int) {
        val thisNote = notesList[position]

        with(holder) {
            binding.textViewTitle.text = thisNote.title
            binding.textViewContent.text = thisNote.content
            binding.imageViewCategory.visibility = View.GONE
            binding.textViewCategoryName.visibility = View.GONE

            if (thisNote.tags.isEmpty()) { // if the note doesn't have any tag, the views are hidden
                binding.imageViewTag.visibility = View.GONE
                binding.textViewTags.visibility = View.GONE
            } else { // if the note has tags, those will be added to the layout
                binding.textViewTags.text = thisNote.tagsData.joinToString(" • ") { it.name ?: "" }
            }

            // opens the note with the given data
            binding.root.setOnClickListener {
                val action = MainFragmentDirections.actionGlobalNoteViewerFragment(thisNote)
                navController.navigate(action)
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
}