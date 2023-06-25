package com.madalin.notelo.screens.notes.allnotes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.components.LayoutMessage
import com.madalin.notelo.components.PopupBanner
import com.madalin.notelo.databinding.FragmentAllNotesBinding
import com.madalin.notelo.screens.notes.noteviewer.NoteViewerActivity

class AllNotesFragment : Fragment() {
    private val viewModel: AllNotesViewModel by viewModels()

    private lateinit var binding: FragmentAllNotesBinding
    private lateinit var allNotesAdapter: AllNotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allNotesAdapter = AllNotesAdapter(context)

        // checks if the user's notes have been fetched and gets them otherwise
        if (viewModel.notesListLiveData.value == null) {
            viewModel.getNotesFromFirestore()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAllNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // recycler view preparations
        with(binding) {
            recyclerViewNotes.layoutManager = LinearLayoutManager(context)
            recyclerViewNotes.adapter = allNotesAdapter
        }

        // notes list observer
        viewModel.notesListLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                allNotesAdapter.setNotesList(it)
                allNotesAdapter.notifyDataSetChanged()
            } else {
                PopupBanner.make(context, PopupBanner.TYPE_FAILURE, getString(R.string.something_went_wrong_please_try_again)).show()
            }
        }

        // error message observer
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(context, PopupBanner.TYPE_FAILURE, it.toString()).show()
        }

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

                allNotesAdapter.setNotesList(foundNotes)
                allNotesAdapter.notifyDataSetChanged()
            }
        }

        // shows the notes based on the search query on key pressed
        binding.editTextSearchBar.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val foundNotes = viewModel.findNotes(textView.text.toString())
                allNotesAdapter.setNotesList(foundNotes)
                allNotesAdapter.notifyDataSetChanged()
                return@setOnEditorActionListener true
            }

            false
        }

        // swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getNotesFromFirestore()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        // FAB that triggers the note creation activity
        binding.floatingActionButton.setOnClickListener {
            startActivity(Intent(context, NoteViewerActivity::class.java))
        }
    }
}