package com.madalin.notelo.core.data.model

import com.madalin.notelo.core.domain.model.Article

data class ArticleResponse(
    var status: String? = null,
    var totalResults: Long? = null,
    var articles: List<Article>? = null
)