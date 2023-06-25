package com.madalin.notelo.screens.discover

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.api.Article
import com.madalin.notelo.api.NewsRetrofitClient
import kotlinx.coroutines.launch
import java.lang.Exception

class DiscoverViewModel : ViewModel() {

    // data holders to observe
    val articlesListLiveData by lazy { MutableLiveData<List<Article>>() }
    val errorMessageLiveData by lazy { MutableLiveData<String>() }

    /**
     * Fetches a list of articles from the provided API and updates [articlesListLiveData] with
     * the response.
     */
    fun fetchArticles() {
        viewModelScope.launch {
            try {
                val response = NewsRetrofitClient.apiService.getEverythingByTerm("apple", NewsRetrofitClient.getApiKey())

                if (response.isSuccessful) {
                    val newsResponse = response.body()?.articles // gets only the articles array
                    articlesListLiveData.value = newsResponse
                } else {
                    errorMessageLiveData.value = "Error fetching articles"
                }
            } catch (e: Exception) {
                Log.e("ARTICLES_FAIL", "Articles request failed")
            }
        }
    }
}