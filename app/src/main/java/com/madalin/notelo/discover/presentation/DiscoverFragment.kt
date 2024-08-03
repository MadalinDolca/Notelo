package com.madalin.notelo.discover.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.madalin.notelo.R
import com.madalin.notelo.core.presentation.adapter.NotesAdapter
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.presentation.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.presentation.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.databinding.FragmentDiscoverBinding
import com.madalin.notelo.discover.presentation.adapter.ArticleAdapter
import com.madalin.notelo.home.presentation.HomeFragmentDirections
import org.koin.androidx.viewmodel.ext.android.viewModel

class DiscoverFragment : Fragment() {
    private val viewModel: DiscoverViewModel by viewModel()
    private lateinit var binding: FragmentDiscoverBinding
    private lateinit var articlesAdapter: ArticleAdapter
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.root, SPACING_MARGIN, DIRECTION_TOP)

        // obtains the nav controller of the parent activity
        val activityNavController = (activity as AppCompatActivity).findNavController(R.id.mainActivityFragmentContainerView)
        setupAdapters(activityNavController)

        // recycler views preparations
        with(binding) {
            recyclerViewArticles.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            recyclerViewArticles.adapter = articlesAdapter

            recyclerViewNotes.layoutManager = LinearLayoutManager(context)
            recyclerViewNotes.adapter = notesAdapter
        }

        setupObservers()
        setupListeners()
    }

    private fun setupAdapters(navController: NavController) {
        articlesAdapter = ArticleAdapter(context = context,
            onArticleClick = { article ->
                // todo navigate to article viewer
                /*val intent = Intent(context, ArticleViewerActivity::class.java)
                intent.putExtra(Extra.ARTICLE, articlesList[position])
                context?.startActivity(intent)*/
            }
        )

        // todo check if this user owns the note
        notesAdapter = NotesAdapter(
            onNavigateToNote = { note ->
                val action = HomeFragmentDirections.actionGlobalNoteViewerFragment(note)
                navController.navigate(action)
            },
            onOpenNoteProperties = {}
        )
    }

    private fun setupObservers() {
        // articles loading observer
        viewModel.isLoadingArticlesState.observe(viewLifecycleOwner) {
            if (it) binding.viewSwitcherArticles.displayedChild = 1
            else binding.viewSwitcherArticles.displayedChild = 0
        }

        // notes loading observer
        viewModel.isLoadingNotesState.observe(viewLifecycleOwner) {
            if (it) binding.viewSwitcherNotes.displayedChild = 1
            else binding.viewSwitcherNotes.displayedChild = 0
        }

        // articles list observer
        viewModel.articlesListState.observe(viewLifecycleOwner) {
            articlesAdapter.setArticlesList(it)
            articlesAdapter.notifyDataSetChanged()
        }

        // notes list observer
        viewModel.notesListState.observe(viewLifecycleOwner) {
            notesAdapter.setNotesList(it)
            notesAdapter.notifyDataSetChanged()
        }
    }

    private fun setupListeners() {
        // swipe refresh to fetch for data
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        // search bar text changed listener
        binding.editTextSearchBar.addTextChangedListener {
            viewModel.fetchDataByQuery(it.toString())
        }
    }
}