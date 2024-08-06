package com.madalin.notelo.article_viewer.presentation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.core.presentation.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.presentation.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.presentation.util.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.core.presentation.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.databinding.FragmentArticleViewerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class ArticleViewerFragment : Fragment() {
    private val viewModel: ArticleViewerViewModel by viewModel()
    private lateinit var binding: FragmentArticleViewerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentArticleViewerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeToEdge(activity, binding.textViewArticleTitle, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(activity, binding.textViewArticleSource, SPACING_PADDING, DIRECTION_BOTTOM)

        // shows the article's image in the view
        Glide.with(this)
            .load(viewModel.article?.urlToImage)
            .into(binding.imageViewArticleImage)

        // populates the views with data
        with(binding) {
            textViewArticleTitle.text = viewModel.article?.title
            textViewArticleAuthor.text = viewModel.article?.author
            textViewPublicationDate.text = (viewModel.article?.publishedAt)
            textViewArticleContent.text = viewModel.article?.content
        }

        if (viewModel.article?.url == null) {
            binding.buttonAddToCollection.visibility = View.GONE
        }

        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        // adds this article to the user's notes collection
        binding.buttonAddToCollection.setOnClickListener {
            viewModel.addArticleToNotesCollection()
        }

        // opens the full article inside the browser on click
        binding.textViewArticleSource.setOnClickListener {
            val articleUrl = viewModel.article?.url

            if (articleUrl == null) {
                viewModel.articleOpeningError()
                return@setOnClickListener
            }

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl))
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        // is article added observer
        viewModel.isArticleAddedState.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigateUp()
            }
        }
    }
}