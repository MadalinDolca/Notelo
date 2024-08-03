package com.madalin.notelo.discover.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.remote.FirebaseContentRepository
import com.madalin.notelo.core.domain.result.GetNotesResult
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.discover.domain.model.Article
import com.madalin.notelo.discover.domain.repository.NewsRepository
import com.madalin.notelo.discover.domain.result.ArticleResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val globalDriver: GlobalDriver,
    private val newsRepository: NewsRepository,
    private val firebaseContentRepository: FirebaseContentRepository
) : ViewModel() {
    private val topHeadlinesList = mutableListOf<Article>()
    private val notesList = mutableListOf<Note>()

    private val _articlesListState = MutableLiveData<List<Article>>()
    val articlesListState: LiveData<List<Article>> = _articlesListState

    private val _notesListState = MutableLiveData<List<Note>>()
    val notesListState: LiveData<List<Note>> = _notesListState

    private val _isLoadingArticlesState = MutableLiveData(false)
    val isLoadingArticlesState: LiveData<Boolean> = _isLoadingArticlesState

    private val _isLoadingNotesState = MutableLiveData(false)
    val isLoadingNotesState: LiveData<Boolean> = _isLoadingNotesState

    init {
        fetchData()
    }

    /**
     * Fetches the top headlines and all public notes.
     */
    fun fetchData() {
        fetchTopHeadlines()
        fetchAllPublicNotes()
    }

    /**
     * Fetches the top headlines and all public notes that match the given [query].
     */
    fun fetchDataByQuery(query: String) {
        if (query.isNotEmpty()) {
            fetchArticlesByTerm(query)
            fetchAllPublicNotesByQuery(query)
        } else {
            _articlesListState.value = topHeadlinesList
            _notesListState.value = notesList
        }
    }

    /**
     * Fetches a list of top headlines from the News API and updates the [_articlesListState].
     */
    private fun fetchTopHeadlines() {
        viewModelScope.launch {
            newsRepository.getTopHeadlinesFromCountry("us")
                .collect {
                    when (it) {
                        ArticleResult.Loading -> _isLoadingArticlesState.postValue(true)

                        is ArticleResult.Success -> {
                            topHeadlinesList.clear()
                            topHeadlinesList.addAll(it.articles ?: emptyList())
                            _articlesListState.postValue(topHeadlinesList)
                            _isLoadingArticlesState.postValue(false)
                        }

                        is ArticleResult.Error -> globalDriver.showPopupBanner(
                            PopupBanner.TYPE_FAILURE,
                            it.message ?: R.string.could_not_fetch_articles
                        )
                    }
                }
        }
    }

    /**
     * Fetches a list of articles based on the given [searchTerm] and updates the [articlesListState].
     */
    private fun fetchArticlesByTerm(searchTerm: String) {
        viewModelScope.launch {
            newsRepository.getEverythingByTerm(searchTerm)
                .collect {
                    when (it) {
                        ArticleResult.Loading -> _isLoadingNotesState.postValue(true)

                        is ArticleResult.Success -> {
                            _articlesListState.postValue(it.articles ?: emptyList())
                            _isLoadingNotesState.postValue(false)
                        }

                        is ArticleResult.Error -> globalDriver.showPopupBanner(
                            PopupBanner.TYPE_FAILURE,
                            it.message ?: R.string.could_not_fetch_searched_articles
                        )
                    }
                }
        }
    }

    /**
     * Fetches all public notes from Firestore and updates the [notesListState].
     */
    private fun fetchAllPublicNotes() {
        _isLoadingNotesState.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val result = firebaseContentRepository.getAllPublicNotes()
            when (result) {
                is GetNotesResult.Success -> {
                    notesList.clear()
                    notesList.addAll(result.notes)
                    _notesListState.postValue(notesList)
                    _isLoadingNotesState.postValue(false)
                }

                is GetNotesResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_fetch_notes
                )
            }
        }
    }

    /**
     * Fetches all public notes from Firestore that match the given [query] and updates the
     * [_notesListState].
     */
    private fun fetchAllPublicNotesByQuery(query: String) {
        _isLoadingNotesState.value = true

        viewModelScope.launch(Dispatchers.IO) {
            val result = firebaseContentRepository.getAllPublicNotesByQuery(query)
            when (result) {
                is GetNotesResult.Success -> {
                    _notesListState.postValue(result.notes)
                    _isLoadingNotesState.postValue(false)
                }

                is GetNotesResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_fetch_searched_notes
                )
            }
        }
    }
}