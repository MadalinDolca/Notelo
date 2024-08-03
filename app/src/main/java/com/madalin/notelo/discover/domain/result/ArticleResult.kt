package com.madalin.notelo.discover.domain.result

import com.madalin.notelo.discover.domain.model.Article

sealed class ArticleResult {
    data object Loading : ArticleResult()
    data class Success(val articles: List<Article>?) : ArticleResult()
    data class Error(val message: String?) : ArticleResult()
}