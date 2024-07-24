package com.madalin.notelo.note_viewer.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.madalin.notelo.R
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.core.presentation.components.noteproperties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.databinding.FragmentNoteViewerBinding
import com.madalin.notelo.core.domain.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.core.domain.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.domain.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.domain.util.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.core.domain.util.EdgeToEdge.edgeToEdge
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment used to view, update and create a note.
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
        edgeToEdge(activity, binding.layoutOptions, SPACING_PADDING, DIRECTION_BOTTOM)

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
            if (!viewModel.isEditEnabled) { // if not in editable state, then enable editing when clicking on "Edit"
                enableEditing()
                viewModel.isEditEnabled = true
            } else { // if already in editable state, update the note when clicking on "Done"
                viewModel.updateNote(binding.editTextTitle.text.toString(), binding.editTextContent.text.toString())
            }
        }

        // properties button (shows the properties dialog)
        binding.textViewProperties.setOnClickListener {
            val context = context ?: return@setOnClickListener
            val currentNote = viewModel.note ?: return@setOnClickListener
            NotePropertiesBottomSheetDialog(context, currentNote).show()
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
            viewModel.createNote(binding.editTextTitle.text.toString(), binding.editTextContent.text.toString())
        }
    }

    /**
     * Shows when the note was last edited using its [updatedAt] value according to today's date.
     */
    private fun setLastEditedLabel(updatedAt: Date?) {
        if (updatedAt == null) {
            binding.textViewLastEdited.visibility = View.INVISIBLE
        } else {
            val dateFormatPattern = if (isToday(updatedAt)) "hh:mm a" else "dd MMM yy"
            val formattedDate = SimpleDateFormat(dateFormatPattern, Locale.getDefault()).format(updatedAt)
            binding.textViewLastEdited.text = getString(R.string.edited_date, formattedDate)
        }
    }

    /**
     * Helper function to check if a [date] is today.
     * @return `True` if today, `False` otherwise
     */
    private fun isToday(date: Date): Boolean {
        val now = Date()
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now) == SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date)
    }

    private fun enableEditing() {
        // enables editing layout
        binding.textViewAction.text = getString(R.string.done)
        enableTitleAndContentField()
    }

    private fun disableEditing() {
        // disables editing
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
        viewModel.titleErrorMessageLiveData.observe(viewLifecycleOwner) {
            binding.editTextTitle.error = getString(it)
            binding.editTextTitle.requestFocus()
        }

        // note content error observer
        viewModel.contentErrorMessageLiveData.observe(viewLifecycleOwner) {
            binding.editTextContent.error = getString(it)
            binding.editTextContent.requestFocus()
        }

        // is note updated observer
        viewModel.isNoteUpdatedLiveData.observe(viewLifecycleOwner) {
            if (it) { // if updated, disables editing
                disableEditing()
                viewModel.isEditEnabled = false
                viewModel.setNoteUpdateStatus(false)
            }
        }

        // is note created observer
        viewModel.isNoteCreatedLiveData.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigateUp() // pops the fragment and returns to the app that navigated to the deep link in this app
                //findNavController().popBackStack() // pops the fragment only from the app backstack
                viewModel.setNoteCreationStatus(false)
            }
        }

        // pop-up message observer
        viewModel.popupMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(activity, it.first, getString(it.second)).show()
        }
    }
}