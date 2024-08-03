package com.madalin.notelo.discover.data.repository

import com.madalin.notelo.discover.data.mapper.toArticleDomain
import com.madalin.notelo.discover.data.network.NewsApiService
import com.madalin.notelo.discover.domain.repository.NewsRepository
import com.madalin.notelo.discover.domain.result.ArticleResult
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class NewsRepositoryImpl(
    private val service: NewsApiService,
    private val apiKey: String,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : NewsRepository {

    override fun getEverythingByTerm(searchTerm: String) = flow {
        emit(ArticleResult.Loading)

        try {
            val response = service.getEverythingByTerm(searchTerm, apiKey)
            val articles = response.articles?.map { it.toArticleDomain() }

            emit(ArticleResult.Success(articles))
        } catch (e: Exception) {
            emit(ArticleResult.Error(e.message))
        }
    }.flowOn(dispatcher)

    override fun getTopHeadlinesFromCountry(country: String) = flow {
        emit(ArticleResult.Loading)

        try {
            val response = service.getTopHeadlinesFromCountry(country, apiKey)
            val articles = response.articles?.map { it.toArticleDomain() }

            emit(ArticleResult.Success(articles))
        } catch (e: Exception) {
            emit(ArticleResult.Error(e.message))
        }
    }.flowOn(dispatcher)
}
