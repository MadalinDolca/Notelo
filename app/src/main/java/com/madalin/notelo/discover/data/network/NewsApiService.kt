package com.madalin.notelo.discover.data.network

import com.madalin.notelo.core.data.remote.model.ArticleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface that defines the **News API** endpoints.
 * This interface will serve as a contract for Retrofit to generate the necessary code.
 * Defines the HTTP methods, path, query parameters, and request/response types according to the API.
 */
interface NewsApiService {
    @GET("v2/everything")
    suspend fun getEverythingByTerm(
        @Query("q") term: String,
        @Query("apiKey") apiKey: String
    ): Response<ArticleResponse>

    @GET("v2/everything")
    suspend fun getEverythingByTermAndSortByPublished(
        @Query("q") term: String,
        @Query("sortBy") sortBy: String,
        @Query("apiKey") apiKey: String
    ): Response<ArticleResponse>

    @GET("v2/top-headlines")
    suspend fun getTopHeadlinesFromCountry(
        @Query("country") country: String = "ro",
        @Query("apiKey") apiKey: String
    ): Response<ArticleResponse>
}