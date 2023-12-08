package com.madalin.notelo.feature.discover

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.madalin.notelo.api.Article
import com.madalin.notelo.databinding.LayoutArticleCardBinding
import com.madalin.notelo.feature.discover.articleviewer.ArticleViewerActivity
import com.madalin.notelo.util.Extra

class ArticleAdapter(
    var context: Context?
) : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    private val articlesList = mutableListOf<Article>()

    /**
     * Describes the view of an item and the metadata about its place in the [RecyclerView].
     */
    inner class ArticleViewHolder(val binding: LayoutArticleCardBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = LayoutArticleCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        with(holder) {
            binding.textViewArticleName.text = articlesList[position].title
            binding.textViewArticleDescription.text = articlesList[position].description

            // loads the image from the URL into the view
            context?.let {
                Glide.with(it)
                    .load(articlesList[position].urlToImage)
                    .into(binding.imageViewArticle)
            }

            // darken the article's image
            binding.imageViewArticle.setColorFilter(Color.argb(100, 0, 0, 0), PorterDuff.Mode.DARKEN)

            // opens the article view activity on click
            binding.root.setOnClickListener {
                val intent = Intent(context, ArticleViewerActivity::class.java)
                intent.putExtra(Extra.ARTICLE, articlesList[position])
                context?.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = articlesList.size

    fun setArticlesList(newArticlesList: List<Article>) {
        articlesList.clear()
        articlesList.addAll(newArticlesList)
    }
}