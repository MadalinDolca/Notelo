package com.madalin.notelo.screens.discover

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.madalin.notelo.components.PopupBanner
import com.madalin.notelo.databinding.FragmentDiscoverBinding

class DiscoverFragment : Fragment() {
    private val viewModel by viewModels<DiscoverViewModel>()

    private lateinit var binding: FragmentDiscoverBinding
    private lateinit var articlesAdapter: ArticleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        articlesAdapter = ArticleAdapter(context)

        // checks if the articles have been fetched and gets them otherwise
        if (viewModel.articlesListLiveData.value == null) {
            viewModel.fetchArticles()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // recycler view preparations
        with(binding) {
            recyclerViewArticles.layoutManager = LinearLayoutManager(context)
            recyclerViewArticles.adapter = articlesAdapter
        }

        // articles list observer
        viewModel.articlesListLiveData.observe(viewLifecycleOwner) {
            articlesAdapter.setArticlesList(it)
            articlesAdapter.notifyDataSetChanged()
        }

        // error message observer
        viewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            PopupBanner.make(context, PopupBanner.TYPE_FAILURE, it.toString()).show()
        }

        // swipe refresh to fetch for articles
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchArticles()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}