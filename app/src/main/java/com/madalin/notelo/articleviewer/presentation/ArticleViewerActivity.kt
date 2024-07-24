package com.madalin.notelo.articleviewer.presentation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Article
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.databinding.ActivityArticleViewerBinding
import com.madalin.notelo.core.domain.util.EdgeToEdge.DIRECTION_BOTTOM
import com.madalin.notelo.core.domain.util.EdgeToEdge.DIRECTION_TOP
import com.madalin.notelo.core.domain.util.EdgeToEdge.SPACING_MARGIN
import com.madalin.notelo.core.domain.util.EdgeToEdge.SPACING_PADDING
import com.madalin.notelo.core.domain.util.EdgeToEdge.edgeToEdge
import com.madalin.notelo.core.domain.util.Extra

class ArticleViewerActivity : AppCompatActivity() {
    private var articleData: Article? = null
    private val viewModel by viewModels<ArticleViewerViewModel>()

    private lateinit var binding: ActivityArticleViewerBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        edgeToEdge(this, binding.textViewArticleTitle, SPACING_MARGIN, DIRECTION_TOP)
        edgeToEdge(this, binding.textViewArticleSource, SPACING_PADDING, DIRECTION_BOTTOM)

        articleData = intent.getParcelableExtra(Extra.ARTICLE)!! // gets the article's data from extra

        // shows the article's image in the view
        Glide.with(this)
            .load(articleData?.urlToImage)
            .into(binding.imageViewArticleImage)

        // populates the views with data
        with(binding) {
            textViewArticleTitle.text = articleData?.title
            textViewArticleAuthor.text = articleData?.author
            textViewPublicationDate.text = viewModel.formatDate(articleData?.publishedAt)
            textViewArticleContent.text = articleData?.content
        }

        // saves the article to the user's notes
        binding.buttonSaveAsNote.setOnClickListener {
            articleData?.let { viewModel.saveArticleAsNote(it) }
        }

        // opens the full article inside the browser on click
        binding.textViewArticleSource.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(articleData?.url)

            // check if there is a suitable activity available on the device to handle the web browsing intent
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            }
        }

        // shows a PopupBanner to signal if the note has been added successfully to the database
        viewModel.ifNoteAddedSuccessfullyLiveData.observe(this) { isSuccess ->
            if (isSuccess) {
                PopupBanner.make(this, PopupBanner.TYPE_SUCCESS, getString(R.string.note_created_successfully)).show()
            } else {
                PopupBanner.make(this, PopupBanner.TYPE_FAILURE, getString(R.string.something_went_wrong_please_try_again)).show()
            }
        }
    }
}