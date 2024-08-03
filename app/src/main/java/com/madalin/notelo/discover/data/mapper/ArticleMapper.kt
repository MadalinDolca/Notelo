package com.madalin.notelo.discover.data.mapper

import com.madalin.notelo.discover.domain.model.Article
import com.madalin.notelo.discover.data.model.Article as ArticleData

fun ArticleData.toArticleDomain() = Article(
    author = author,
    title = title,
    description = description,
    url = url,
    urlToImage = urlToImage,
    publishedAt = publishedAt,
    content = content
)