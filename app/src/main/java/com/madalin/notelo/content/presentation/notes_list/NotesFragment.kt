package com.madalin.notelo.content.presentation.notes_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.core.presentation.components.LayoutMessage
import com.madalin.notelo.core.presentation.components.note_properties.NotePropertiesBottomSheetDialog
import com.madalin.notelo.databinding.FragmentNotesBinding
import com.madalin.notelo.home.presentation.HomeFragmentDirections
import org.koin.androidx.viewmodel.ext.android.viewModel

class NotesFragment : Fragment() {
    private val viewModel: NotesViewModel by viewModel()
    private lateinit var binding: FragmentNotesBinding
    private lateinit var notesAdapter: NotesAdapter
    private lateinit var activityNavController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false)
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
                activityNavController.navigate(action) //navController.navigate(R.id.noteViewerFragment)
            },
            onOpenNoteProperties = { note ->
                context?.let { NotePropertiesBottomSheetDialog(it, note).show() }
            }
        )

        // recycler view preparations
        with(binding) {
            recyclerViewNotes.layoutManager = LinearLayoutManager(context)
            recyclerViewNotes.adapter = notesAdapter
        }

        setupObservers()
        setupListeners()
    }

    private fun setupObservers() {
        // notes list observer
        viewModel.notesListState.observe(viewLifecycleOwner) {
            notesAdapter.setNotesList(it)
            notesAdapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        // creates the "no notes found" layout
        val layoutMessage = LayoutMessage.make(context, binding.constraintLayoutContainer, binding.editTextSearchBar)

        // shows the notes based on the search query while typing
        binding.editTextSearchBar.addTextChangedListener {
            val foundNotes = viewModel.findNotes(it.toString())

            if (foundNotes.isEmpty()) {
                binding.recyclerViewNotes.visibility = View.GONE
                layoutMessage
                    .setContent(R.raw.lottie_empty, getString(R.string.couldn_t_find_any_notes_that_contain_the_searched_term))
                    .show()
            } else {
                binding.recyclerViewNotes.visibility = View.VISIBLE
                layoutMessage.hide()

                notesAdapter.setNotesList(foundNotes)
                notesAdapter.notifyDataSetChanged()
            }
        }

        // shows the notes based on the search query on key pressed
        binding.editTextSearchBar.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val foundNotes = viewModel.findNotes(textView.text.toString())
                notesAdapter.setNotesList(foundNotes)
                notesAdapter.notifyDataSetChanged()
                return@setOnEditorActionListener true
            }

            false
        }

        // obtains the notes on swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getNotesObserver()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        // FAB that triggers the note creation activity
        binding.floatingActionButton.setOnClickListener {
            val action = HomeFragmentDirections.actionGlobalNoteViewerFragment(null)
            activityNavController.navigate(action)
        }
    }
}