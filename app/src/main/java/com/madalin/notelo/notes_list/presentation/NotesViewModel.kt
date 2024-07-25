package com.madalin.notelo.notes_list.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.FirebaseContentRepository
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner

class NotesViewModel(
    private val globalDriver: GlobalDriver,
    private val repository: FirebaseContentRepository
) : ViewModel() {
    private val currentUser = globalDriver.currentUser

    private val _notesListState = MutableLiveData(mutableListOf<Note>())
    val notesListState: LiveData<MutableList<Note>> get() = _notesListState

    init {
        getAndObserveUserNotes()
    }

    /**
     * Obtains the notes associated with the current user ID, updates the data holders and starts
     * listening for changes.
     */
    fun getAndObserveUserNotes() {
        val userId = currentUser.value?.id
        if (userId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_get_the_notes_because_the_user_id_is_null)
            return
        }

        repository.getNotesByUserIdListener(
            userId,
            onSuccess = {
                // sets the value and dispatches it to the active observers
                _notesListState.value = it.toMutableList()
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, it ?: R.string.could_not_get_the_notes)
            }
        )
    }

    /**
     * Filters the [current notes list][_notesListState] and returns the ones that contains [query].
     */
    fun findNotes(query: String): List<Note> {
        val currentNotes = _notesListState.value ?: emptyList()
        val foundNotes = mutableListOf<Note>()

        if (query.isEmpty()) { // returns the initial list if nothing was searched for
            return currentNotes
        } else {
            for (note in currentNotes) {
                if (note.title?.lowercase()?.contains(query.lowercase()) == true) {
                    foundNotes.add(note)
                } else if (note.content?.lowercase()?.contains(query.lowercase()) == true) {
                    foundNotes.add(note)
                }
            }
        }

        return foundNotes
    }
}