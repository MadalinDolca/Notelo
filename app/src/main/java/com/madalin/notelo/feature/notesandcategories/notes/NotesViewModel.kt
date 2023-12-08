package com.madalin.notelo.feature.notesandcategories.notes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.model.Note
import com.madalin.notelo.repository.FirebaseContentRepository

class NotesViewModel(
    private val repository: FirebaseContentRepository
) : ViewModel() {
    private val notesList = mutableListOf<Note>() // list to store the user's notes

    private val _notesListLiveData by lazy { MutableLiveData<MutableList<Note>>() }
    val notesListLiveData: LiveData<MutableList<Note>> get() = _notesListLiveData

    private val _popupMessageLiveData = MutableLiveData<Pair<Int, String>>()
    val popupMessageLiveData: LiveData<Pair<Int, String>> get() = _popupMessageLiveData

    /**
     * Obtains the notes associated with the given [userId], updates the data holders and starts
     * listening for changes.
     */
    fun getNotesFromFirestore(userId: String) {
        repository.getNotesByUserIdListener(userId,
            onSuccess = {
                notesList.clear()
                notesList.addAll(it)

                _notesListLiveData.value = notesList // sets the value and dispatches it to the active observers
            },
            onFailure = {
                it?.let { _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, it) }
            })
    }

    /**
     * Filters the notes from [notesList] and returns the ones that contains [query].
     */
    fun findNotes(query: String): List<Note> {
        val foundNotes = mutableListOf<Note>()

        if (query.isEmpty()) { // returns the initial list if nothing was searched for
            return notesList
        } else {
            for (note in notesList) {
                if (note.title?.contains(query) == true) {
                    foundNotes.add(note)
                } else if (note.content?.contains(query) == true) {
                    foundNotes.add(note)
                }
            }
        }

        return foundNotes
    }
}