package com.madalin.notelo.note_viewer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.FirebaseContentRepository
import com.madalin.notelo.core.domain.validation.NoteValidator
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.core.presentation.util.UiText

class NoteViewerViewModel(
    private val globalDriver: GlobalDriver,
    private val repository: FirebaseContentRepository
) : ViewModel() {
    var note: Note? = null // note from navigation argument
    var isEditEnabled = false

    private val _titleErrorMessageLiveData = MutableLiveData<UiText>()
    val titleErrorMessageLiveData: LiveData<UiText> get() = _titleErrorMessageLiveData

    private val _contentErrorMessageLiveData = MutableLiveData<UiText>()
    val contentErrorMessageLiveData: LiveData<UiText> get() = _contentErrorMessageLiveData

    private val _isNoteCreatedLiveData = MutableLiveData(false)
    val isNoteCreatedLiveData: LiveData<Boolean> get() = _isNoteCreatedLiveData

    private val _isNoteUpdatedLiveData = MutableLiveData(false)
    val isNoteUpdatedLiveData: LiveData<Boolean> get() = _isNoteUpdatedLiveData

    /**
     * Creates and adds a new [Note] to the database with the given [noteTitle] and [noteContent].
     */
    fun createNote(noteTitle: String, noteContent: String) {
        val userId = globalDriver.currentUser.value?.id
        if (userId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.could_not_create_a_new_note_because_the_user_id_is_null)
            return
        }

        if (!valitateFields(noteTitle, noteContent)) return // if the provided data is valid

        val newNote = Note(userId = userId, title = noteTitle, content = noteContent)

        repository.createNote(newNote,
            onSuccess = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_SUCCESS, R.string.note_created_successfully)
                _isNoteCreatedLiveData.value = true
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.something_went_wrong_please_try_again)
            }
        )
    }

    /**
     * Updates the current [note] with the given [newTitle] and [newContent] in the database.
     */
    fun updateNote(newTitle: String, newContent: String) {
        val noteId = note?.id
        if (noteId == null) {
            globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.something_went_wrong_please_try_again)
            return
        }

        if (!valitateFields(newTitle, newContent)) return // not valid

        val newData = mapOf(
            "title" to newTitle,
            "content" to newContent,
            "updatedAt" to null
        )

        // updates the given note in the database
        repository.updateNote(
            noteId, newData,
            onSuccess = {
                // updates the given note locally
                note?.title = newTitle
                note?.content = newContent

                _isNoteUpdatedLiveData.value = true
                globalDriver.showPopupBanner(PopupBanner.TYPE_SUCCESS, R.string.note_updated_successfully)
            },
            onFailure = {
                globalDriver.showPopupBanner(PopupBanner.TYPE_FAILURE, R.string.something_went_wrong_please_try_again)
            }
        )
    }

    /**
     * Checks if the given [noteTitle] and [noteContent] are valid. If not, it updates the data
     * holders accordingly.
     * @return `true` is valid, `false` otherwise.
     */
    private fun valitateFields(noteTitle: String, noteContent: String): Boolean {
        val result = NoteValidator.validateFields(noteTitle, noteContent)
        when (result) {
            NoteValidator.NoteResult.Valid -> return true

            NoteValidator.NoteResult.InvalidTitleLength -> {
                _titleErrorMessageLiveData.value = UiText.Resource(
                    R.string.note_title_must_be_between_x_and_y_characters,
                    NoteValidator.MIN_NOTE_TITLE_LENGTH, NoteValidator.MAX_NOTE_TITLE_LENGTH
                )
                return false
            }

            NoteValidator.NoteResult.InvalidContentLength -> {
                _contentErrorMessageLiveData.value = UiText.Resource(
                    R.string.note_content_must_be_between_x_and_y_characters,
                    NoteValidator.MIN_NOTE_CONTENT_LENGTH, NoteValidator.MAX_NOTE_CONTENT_LENGTH
                )
                return false
            }
        }
    }

    /**
     * Sets the note creation data holder status to [status].
     * @param status `true` if note has been created, `false` otherwise.
     */
    fun setNoteCreationStatus(status: Boolean) {
        _isNoteCreatedLiveData.value = status
    }

    /**
     * Sets the note update status to [status].
     * @param status `true` is note has been updated, `false` otherwise.
     */
    fun setNoteUpdateStatus(status: Boolean) {
        _isNoteUpdatedLiveData.value = status
    }
}