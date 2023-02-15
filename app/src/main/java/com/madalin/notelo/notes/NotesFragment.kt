package com.madalin.notelo.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.databinding.FragmentNotesBinding
import com.madalin.notelo.ui.PopupBanner

class NotesFragment : Fragment() {

    private lateinit var binding: FragmentNotesBinding
    private lateinit var notesViewModel: NotesViewModel
    private val notesAdapter = NotesAdapter()

    companion object {
        fun newInstance() = NotesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        notesViewModel = ViewModelProvider(this).get() // gets the associated ViewModel

        // checks if the notes have been fetched and gets them otherwise
        if (notesViewModel.getNotesListLiveData.value == null) {
            notesViewModel.getNotesFromFirestore()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNotesBinding.inflate(inflater, container, false) // inflate the layout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView preparations
        binding.recyclerViewNote.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewNote.adapter = notesAdapter

        // notes fetching observer
        notesViewModel.getNotesListLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                notesAdapter.setNotesList(it)
                notesAdapter.notifyDataSetChanged()
            } else {
                PopupBanner.make(context, PopupBanner.TYPE_FAILURE, getString(R.string.you_dont_have_any_notes)).show()
            }
        }

        // notes failed fetching observer
        notesViewModel.getErrorMessage.observe(viewLifecycleOwner) {
            PopupBanner.make(context, PopupBanner.TYPE_FAILURE, it).show()
        }
    }
}