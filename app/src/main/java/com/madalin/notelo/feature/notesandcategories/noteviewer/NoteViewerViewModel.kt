package com.madalin.notelo.feature.notesandcategories.noteviewer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madalin.notelo.R
import com.madalin.notelo.component.PopupBanner
import com.madalin.notelo.model.Note
import com.madalin.notelo.repository.FirebaseContentRepository
import com.madalin.notelo.user.UserData
import com.madalin.notelo.util.LengthConstraint

class NoteViewerViewModel(
    private val repository: FirebaseContentRepository
) : ViewModel() {
    var note: Note? = null // note from navigation argument
    var isEditEnabled = false

    private val _titleErrorMessageLiveData = MutableLiveData<Int>()
    val titleErrorMessageLiveData: LiveData<Int> get() = _titleErrorMessageLiveData

    private val _contentErrorMessageLiveData = MutableLiveData<Int>()
    val contentErrorMessageLiveData: LiveData<Int> get() = _contentErrorMessageLiveData

    private val _isNoteCreatedLiveData = MutableLiveData(false)
    val isNoteCreatedLiveData: LiveData<Boolean> get() = _isNoteCreatedLiveData

    private val _isNoteUpdatedLiveData = MutableLiveData(false)
    val isNoteUpdatedLiveData: LiveData<Boolean> get() = _isNoteUpdatedLiveData

    private val _popupMessageLiveData = MutableLiveData<Pair<Int, Int>>()
    val popupMessageLiveData: LiveData<Pair<Int, Int>> get() = _popupMessageLiveData

    /**
     * Creates and adds a new [Note] to the database with the given [noteTitle] and [noteContent].
     */
    fun createNote(noteTitle: String, noteContent: String) {
        if (!valitateFields(noteTitle, noteContent)) return // if the provided data is valid

        val newNote = Note(userId = UserData.currentUser.id, title = noteTitle, content = noteContent)

        repository.createNote(newNote,
            onSuccess = {
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_SUCCESS, R.string.note_created_successfully)
                _isNoteCreatedLiveData.value = true
            },
            onFailure = {
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.something_went_wrong_please_try_again)
            })
    }

    /**
     * Updates the current [note] with the given [newTitle] and [newContent] in the database.
     */
    fun updateNote(newTitle: String, newContent: String) {
        val noteId = note?.id

        if (noteId == null) { // no note ID provided
            _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.something_went_wrong_please_try_again)
            return
        }

        if (!valitateFields(newTitle, newContent)) return // not valid

        val newData = mapOf(
            "title" to newTitle,
            "content" to newContent,
            "updatedAt" to null
        )

        // updates the given note in the database
        repository.updateNote(noteId, newData,
            onSuccess = {
                // updates the given note locally
                note?.title = newTitle
                note?.content = newContent

                _isNoteUpdatedLiveData.value = true
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_SUCCESS, R.string.note_updated_successfully)
            },
            onFailure = {
                _popupMessageLiveData.value = Pair(PopupBanner.TYPE_FAILURE, R.string.something_went_wrong_please_try_again)
            })
    }

    /**
     * Checks if the given [noteTitle] and [noteContent] are valid. If not, it updates the data
     * holders accordingly.
     * @return `True` is valid, `False` otherwise.
     */
    private fun valitateFields(noteTitle: String, noteContent: String): Boolean {
        when {
            // title is too short
            noteTitle.length < LengthConstraint.MIN_NOTE_TITLE_LENGTH -> {
                _titleErrorMessageLiveData.value = R.string.title_is_too_short
                return false
            }

            // title is empty
            noteTitle.isEmpty() -> {
                _titleErrorMessageLiveData.value = R.string.note_title_cant_be_empty
                return false
            }

            // content is empty
            noteContent.isEmpty() -> {
                _contentErrorMessageLiveData.value = R.string.note_content_cant_be_empty
                return false
            }
        }

        return true
    }

    /**
     * Sets the note creation data holder status to [status].
     * @param status `True` if note has been created, `False` otherwise.
     */
    fun setNoteCreationStatus(status: Boolean) {
        _isNoteCreatedLiveData.value = status
    }

    /**
     * Sets the note update status to [status].
     * @param status `True` is note has been updated, `False` otherwise.
     */
    fun setNoteUpdateStatus(status: Boolean) {
        _isNoteUpdatedLiveData.value = status
    }
}