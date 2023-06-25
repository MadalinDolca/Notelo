package com.madalin.notelo.api

data class ArticleResponse(
    var status: String? = null,
    var totalResults: Long? = null,
    var articles: List<Article>? = null
)