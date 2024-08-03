package com.madalin.notelo.discover.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class NewsResponse(
    var status: String? = null,
    var totalResults: Long? = null,
    var articles: List<Article>? = null
)

@Parcelize
data class Article(
    var author: String? = null,
    var title: String? = null,
    var description: String? = null,
    var url: String? = null,
    var urlToImage: String? = null,
    var publishedAt: String? = null,
    var content: String? = null
) : Parcelable