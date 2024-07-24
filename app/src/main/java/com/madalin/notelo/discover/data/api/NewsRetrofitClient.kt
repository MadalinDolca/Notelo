package com.madalin.notelo.discover.data.api

import com.madalin.notelo.discover.data.network.NewsApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NewsRetrofitClient {
    private const val URL = "https://newsapi.org/"
    private const val API_KEY = "a5e7ded12876444db2b12dccad073c6d"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Interface to communicate with the API.
     */
    val apiService: NewsApiService by lazy {
        retrofit.create(NewsApiService::class.java)
    }

    /**
     * Returns the API key for the news.
     */
    fun getApiKey(): String = API_KEY
}