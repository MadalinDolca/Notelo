package com.madalin.notelo.screens.notes.categoryviewer.tagnotes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.madalin.notelo.components.categoryproperties.CategoryPropertiesDialog
import com.madalin.notelo.components.noteproperties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.databinding.LayoutNoteCardBinding
import com.madalin.notelo.models.Note
import com.madalin.notelo.screens.notes.noteviewer.NoteViewerActivity
import com.madalin.notelo.util.Extra

class TagNotesAdapter(
    var context: Context?,
    var notesList: MutableList<Note>
) : RecyclerView.Adapter<TagNotesAdapter.TagNotesViewHolder>() {

    inner class TagNotesViewHolder(var binding: LayoutNoteCardBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagNotesViewHolder {
        val binding = LayoutNoteCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagNotesViewHolder(binding)
    }

    override fun getItemCount() = notesList.size

    override fun onBindViewHolder(holder: TagNotesViewHolder, position: Int) {
        with(holder) {
            binding.textViewTitle.text = notesList[position].title
            binding.textViewContent.text = notesList[position].content
            binding.imageViewCategory.visibility = View.GONE
            binding.textViewCategoryName.visibility = View.GONE

            if (notesList[position].tags.isEmpty()) { // if the note doesn't have any tag, the views are hidden
                binding.imageViewTag.visibility = View.GONE
                binding.textViewTags.visibility = View.GONE
            } else { // if the note has tags, those will be added to the layout
                binding.textViewTags.text = notesList[position].tagsData.joinToString(" • ") { it.name ?: "" }
            }

            // opens the note with the given data
            binding.root.setOnClickListener {
                val intent = Intent(context, NoteViewerActivity::class.java)
                intent.putExtra(Extra.NOTE, notesList[position])
                context?.startActivity(intent)
            }

            // open the properties dialog on long click
            binding.root.setOnLongClickListener {
                NotePropertiesBottomSheetDialog(context!!, notesList[position]).show()
                return@setOnLongClickListener true
            }
        }
    }
}