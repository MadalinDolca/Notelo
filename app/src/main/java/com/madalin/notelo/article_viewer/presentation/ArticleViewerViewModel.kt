package com.madalin.notelo.article_viewer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.repository.remote.FirebaseContentRepository
import com.madalin.notelo.core.domain.result.UpsertResult
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.discover.domain.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ArticleViewerViewModel(
    savedStateHandle: SavedStateHandle,
    private val globalDriver: GlobalDriver,
    private val localRepository: LocalContentRepository,
    private val firebaseRepository: FirebaseContentRepository
) : ViewModel() {
    val article: Article? = savedStateHandle["articleData"]

    private val _isArticleAddedState = MutableLiveData(false)
    val isArticleAddedState: LiveData<Boolean> get() = _isArticleAddedState

    /**
     * Converts the current [article] to a [Note] and adds it to the user's notes.
     */
    fun addArticleToNotesCollection() {
        val articleTitle = article?.title
        val articleContent = article?.content

        if (articleTitle == null || articleContent == null) {
            globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.could_not_add_to_collection_a_null_article
            )
            return
        }

        // creates a Note with the provided article data
        val newNote = Note(
            title = articleTitle,
            content = articleContent
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = localRepository.upsertNote(newNote)
            when (result) {
                UpsertResult.Success -> {
                    addRemote(newNote)
                    _isArticleAddedState.postValue(true)
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_SUCCESS,
                        R.string.articled_has_been_added_to_the_personal_collection
                    )
                }

                is UpsertResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_add_the_article_to_the_personal_collection
                )
            }
        }
    }

    /**
     * Saves the given [note] in the remote database.
     */
    private suspend fun addRemote(note: Note) {
        val userId = globalDriver.currentUser.value?.id
        if (userId == null) {
            globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.could_not_back_up_the_note_because_the_user_id_is_null
            )
            return
        }
        val noteWithUserId = note.copy(userId = userId)

        val result = firebaseRepository.createNote(noteWithUserId)
        when (result) {
            UpsertResult.Success -> {}
            is UpsertResult.Error -> globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                result.message ?: R.string.could_not_back_up_the_note
            )
        }
    }

    /**
     * Shows a pop-up banner informing the user that the article could not be opened because the
     * URL is null.
     */
    fun articleOpeningError() {
        globalDriver.showPopupBanner(
            PopupBanner.TYPE_FAILURE,
            R.string.could_not_open_the_article_because_the_url_is_null
        )
    }
}