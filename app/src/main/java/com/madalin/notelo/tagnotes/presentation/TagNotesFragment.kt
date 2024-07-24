package com.madalin.notelo.tagnotes.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.databinding.FragmentTagNotesBinding
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag

/**
 * [Fragment] to display the list of [Note]s associated with a given [Tag].
 */
class TagNotesFragment : Fragment() {
    private lateinit var binding: FragmentTagNotesBinding
    private lateinit var tagNotesAdapter: TagNotesAdapter
    private lateinit var activityNavController: NavController

    private var tag: Tag? = null
    private var notesList = mutableListOf<Note>()

    // gets the CategoryNotesFragment's ViewModel instance once
    //private val categoryNotesViewModel by lazy { ViewModelProvider(requireParentFragment())[CategoryNotesViewModel::class.java] }

    companion object {
        // the fragment initialization parameters
        private const val ARG_TAG = "ARG_TAG"
        private const val ARG_NOTES_LIST = "ARG_NOTES_LIST"

        /**
         * Factory method to create an instance of this fragment and with a bundle with the given data.
         * @param tag [Tag] of the notes
         * @param notesList list of notes
         * @return new instance of [TagNotesFragment]
         */
        fun newInstance(tag: Tag, notesList: List<Note>) =
            TagNotesFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TAG, tag)
                    putParcelableArrayList(ARG_NOTES_LIST, ArrayList(notesList))
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTagNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityNavController = (activity as AppCompatActivity).findNavController(R.id.mainActivityFragmentContainerView)
        // obtains the arguments and creates an adapter
        tag = arguments?.getParcelable(ARG_TAG)
        notesList = arguments?.getParcelableArrayList(ARG_NOTES_LIST)!!
        tagNotesAdapter = TagNotesAdapter(context, notesList, activityNavController)

        // recycler view preparations
        with(binding) {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = tagNotesAdapter
        }
    }
}