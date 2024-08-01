package com.madalin.notelo.content.presentation.notes_list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NotesViewModel(
    private val globalDriver: GlobalDriver,
    private val localRepository: LocalContentRepository
) : ViewModel() {
    private val _notesListState = MutableLiveData(listOf<Note>())
    val notesListState: LiveData<List<Note>> get() = _notesListState

    init {
        getNotesObserver()
    }

    /**
     * Obtains the notes and associated data and starts listening for changes to update the data
     * holders.
     */
    fun getNotesObserver() {
        viewModelScope.launch(Dispatchers.IO) {
            localRepository.getNotesWithCategoryAndTagsObserver()
                .catch {
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_FAILURE,
                        it.message ?: R.string.could_not_get_the_notes
                    )
                }
                .collect {
                    _notesListState.postValue(it)
                }
        }
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
                if (note.title.lowercase().contains(query.lowercase())) {
                    foundNotes.add(note)
                } else if (note.content.lowercase().contains(query.lowercase())) {
                    foundNotes.add(note)
                }
            }
        }

        return foundNotes
    }
}