package com.madalin.notelo.collection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.databinding.FragmentCollectionBinding
import com.madalin.notelo.utilities.PopupBanner

class CollectionFragment : Fragment() {

    private lateinit var binding: FragmentCollectionBinding
    private lateinit var collectionViewModel: CollectionViewModel
    private val notesAdapter = CollectionAdapter()

    companion object {
        fun newInstance() = CollectionFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        collectionViewModel = ViewModelProvider(this).get() // gets the associated ViewModel

        // checks if the notes have been fetched and gets them otherwise
        if (collectionViewModel.getNotesListLiveData.value == null) {
            collectionViewModel.getNotesFromFirestore()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCollectionBinding.inflate(inflater, container, false) // inflate the layout
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView preparations
        binding.recyclerViewNote.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewNote.adapter = notesAdapter

        // notes fetching observer
        collectionViewModel.getNotesListLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                notesAdapter.setNotesList(it)
                notesAdapter.notifyDataSetChanged()
            } else {
                PopupBanner.make(context, PopupBanner.TYPE_FAILURE, getString(R.string.you_dont_have_any_notes)).show()
            }
        }

        // notes failed fetching observer
        collectionViewModel.getErrorMessage.observe(viewLifecycleOwner) {
            PopupBanner.make(context, PopupBanner.TYPE_FAILURE, it).show()
        }
    }
}