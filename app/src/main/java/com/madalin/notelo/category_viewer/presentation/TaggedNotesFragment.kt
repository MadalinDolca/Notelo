package com.madalin.notelo.category_viewer.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.content.presentation.notes_list.NotesAdapter
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.presentation.components.note_properties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.databinding.FragmentTagNotesBinding
import com.madalin.notelo.home.presentation.HomeFragmentDirections

/**
 * [Fragment] that displays the [Note]s associated with a given [Tag].
 */
class TaggedNotesFragment : Fragment() {
    private lateinit var binding: FragmentTagNotesBinding
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var activityNavController: NavController

    // fragment initialization parameters
    private val ARG_TAG = "ARG_TAG"
    private val ARG_NOTES = "ARG_NOTES"

    companion object {
        /**
         * Factory method that creates and returns a [TaggedNotesFragment] instance bundled with
         * this [tag] and [notesList].
         */
        fun newInstance(tag: Tag, notesList: List<Note>) =
            TaggedNotesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TAG, tag)
                    putParcelableArrayList(ARG_NOTES, ArrayList(notesList))
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTagNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // obtains the nav controller of the parent activity
        activityNavController = (activity as AppCompatActivity).findNavController(R.id.mainActivityFragmentContainerView)

        // sets up the notes adapter
        notesAdapter = NotesAdapter(
            onNavigateToNote = { note ->
                val action = HomeFragmentDirections.actionGlobalNoteViewerFragment(note)
                activityNavController.navigate(action)
            },
            onOpenNoteProperties = { note ->
                context?.let { NotePropertiesBottomSheetDialog(it, note).show() }
            }
        )

        // recycler view preparations
        with(binding) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = notesAdapter
        }

        // populates the adapter
        val notes = arguments?.getParcelableArrayList<Note>(ARG_NOTES) ?: emptyList()
        notesAdapter.setNotesList(notes)
        notesAdapter.notifyDataSetChanged()
    }
}