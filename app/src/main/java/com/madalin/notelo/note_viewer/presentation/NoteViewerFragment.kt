package com.madalin.notelo.note_viewer.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.util.asDate
import com.madalin.notelo.core.domain.util.asHourAndMinute
import com.madalin.notelo.core.domain.util.isToday
import com.madalin.notelo.core.presentation.components.note_properties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.presentation.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.presentation.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.databinding.FragmentNoteViewerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Date

/**
 * Fragment used to view, create, update and delete a note.
 */
class NoteViewerFragment : Fragment() {
    private val viewModel: NoteViewerViewModel by viewModel()
    private lateinit var binding: FragmentNoteViewerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // obtains the note's data and sets it to the ViewModel only if it hasn't been set before
        if (viewModel.note == null) {
            val args: NoteViewerFragmentArgs by navArgs()
            viewModel.note = args.noteData
        }

        binding = FragmentNoteViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.editTextTitle, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(activity, binding.layoutOptionsWrapper, SPACING_MARGIN, DIRECTION_BOTTOM)

        // determines the mode
        // if no note has been passed, then the creation layout will be shown
        if (viewModel.note != null) initializeViewAndUpdateMode()
        else initializeCreateMode()

        setupObservers()
    }

    /**
     * Initializes the note viewing and updating mode. Prepares the layout to allow the user to
     * view and update the given note.
     */
    private fun initializeViewAndUpdateMode() {
        binding.editTextTitle.setText(viewModel.note?.title)
        binding.editTextContent.setText(viewModel.note?.content)
        disableTitleAndContentField() // disabled when opened
        setLastEditedLabel(viewModel.note?.updatedAt)

        // edit button (enables/disables editing)
        binding.textViewAction.setOnClickListener {
            // if not in editable state, then enable editing when clicking on "Edit"
            if (!viewModel.isEditEnabled) {
                enableEditing()
                viewModel.isEditEnabled = true
            }
            // if already in editable state, update the note when clicking on "Done"
            else {
                viewModel.updateNote(binding.editTextTitle.text.toString(), binding.editTextContent.text.toString())
            }
        }

        // properties button (shows the properties dialog)
        binding.textViewProperties.setOnClickListener {
            val context = context ?: return@setOnClickListener
            val currentNote = viewModel.note ?: return@setOnClickListener
            NotePropertiesBottomSheetDialog(context, currentNote.id).show()
        }
    }

    /**
     * Initializes the note creation mode. Prepares the layout to allow the user to create a new note.
     */
    private fun initializeCreateMode() {
        binding.textViewProperties.visibility = View.GONE
        binding.textViewAction.text = getString(R.string.create_note)

        enableTitleAndContentField()

        binding.textViewAction.setOnClickListener { // creates a note when clicking on "Create note"
            viewModel.saveNote(binding.editTextTitle.text.toString(), binding.editTextContent.text.toString())
        }
    }

    /**
     * Shows when the note was last edited using its [updatedAt] value according to today's date.
     */
    private fun setLastEditedLabel(updatedAt: Date?) {
        if (updatedAt == null) {
            binding.textViewLastEdited.visibility = View.INVISIBLE
        } else {
            val formattedDate = if (updatedAt.isToday()) updatedAt.asHourAndMinute() else updatedAt.asDate()
            binding.textViewLastEdited.text = getString(R.string.edited_date, formattedDate)
        }
    }

    /**
     * Enabled the editing layout.
     */
    private fun enableEditing() {
        binding.textViewAction.text = getString(R.string.done)
        enableTitleAndContentField()
    }

    /**
     * Disabled the editing layout.
     */
    private fun disableEditing() {
        binding.textViewAction.text = getString(R.string.edit)
        disableTitleAndContentField()
    }

    private fun enableTitleAndContentField() {
        binding.editTextTitle.isEnabled = true
        binding.editTextTitle.setBackgroundResource(R.drawable.background_rounded)

        binding.editTextContent.isEnabled = true
        binding.editTextContent.setBackgroundResource(R.drawable.background_rounded)
        binding.editTextContent.requestFocus()
    }

    private fun disableTitleAndContentField() {
        binding.editTextTitle.isEnabled = false
        binding.editTextTitle.background = null

        binding.editTextContent.isEnabled = false
        binding.editTextContent.background = null
    }

    private fun setupObservers() {
        // note title error observer
        viewModel.titleErrorMessageState.observe(viewLifecycleOwner) {
            binding.editTextTitle.error = it.asString(context)
            binding.editTextTitle.requestFocus()
        }

        // note content error observer
        viewModel.contentErrorMessageState.observe(viewLifecycleOwner) {
            binding.editTextContent.error = it.asString(context)
            binding.editTextContent.requestFocus()
        }

        // is note updated observer
        viewModel.isNoteUpdatedState.observe(viewLifecycleOwner) {
            if (it) { // if updated, disables editing
                disableEditing()
                viewModel.isEditEnabled = false
                viewModel.setNoteUpdateStatus(false) // reset status
            }
        }

        // is note created observer
        viewModel.isNoteCreatedState.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigateUp() // pops the fragment and returns to the app that navigated to the deep link in this app
                //findNavController().popBackStack() // pops the fragment only from the app backstack
                viewModel.setNoteCreationStatus(false)
            }
        }

        // is note deleted observer
        viewModel.isNoteDeletedState.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigateUp()
                viewModel.setNoteDeletionStatus(false)
            }
        }
    }
}