package com.madalin.notelo.discover.domain.repository

import com.madalin.notelo.discover.domain.result.ArticleResult
import kotlinx.coroutines.flow.Flow

interface NewsRepository {

    /**
     * Obtains articles based on the given [searchTerm] and returns them as a [Flow] of [ArticleResult].
     */
    fun getEverythingByTerm(searchTerm: String): Flow<ArticleResult>

    /**
     * Obtains the top headlines from the given [country] and returns them as a [Flow] of [ArticleResult].
     */
    fun getTopHeadlinesFromCountry(country: String): Flow<ArticleResult>
}