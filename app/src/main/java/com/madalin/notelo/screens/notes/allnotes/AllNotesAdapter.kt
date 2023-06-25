package com.madalin.notelo.screens.notes.allnotes

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.madalin.notelo.components.noteproperties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.databinding.LayoutNoteCardBinding
import com.madalin.notelo.models.Note
import com.madalin.notelo.screens.notes.noteviewer.NoteViewerActivity
import com.madalin.notelo.util.Extra

class AllNotesAdapter(
    var context: Context?
) : RecyclerView.Adapter<AllNotesAdapter.NotesViewHolder>() {

    private val notesList = mutableListOf<Note>()

    /**
     * Describes the view of an item and the metadata about its place in the [RecyclerView].
     */
    inner class NotesViewHolder(val binding: LayoutNoteCardBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllNotesAdapter.NotesViewHolder {
        val binding = LayoutNoteCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AllNotesAdapter.NotesViewHolder, position: Int) {
        with(holder) {
            binding.textViewTitle.text = notesList[position].title
            binding.textViewContent.text = notesList[position].content
            binding.imageViewTag.visibility = View.GONE
            binding.textViewTags.visibility = View.GONE

            if (notesList[position].categoryId != null) {
                binding.textViewCategoryName.text = notesList[position].categoryId
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

    override fun getItemCount() = notesList.size

    fun setNotesList(newNotesList: List<Note>) {
        notesList.clear()
        notesList.addAll(newNotesList)
    }
}