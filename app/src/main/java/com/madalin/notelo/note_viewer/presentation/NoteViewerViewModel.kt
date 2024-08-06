package com.madalin.notelo.note_viewer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.madalin.notelo.R
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.repository.remote.FirebaseContentRepository
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult
import com.madalin.notelo.core.domain.util.generateId
import com.madalin.notelo.core.domain.validation.NoteValidator
import com.madalin.notelo.core.presentation.GlobalDriver
import com.madalin.notelo.core.presentation.components.PopupBanner
import com.madalin.notelo.core.presentation.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class NoteViewerViewModel(
    savedStateHandle: SavedStateHandle,
    private val globalDriver: GlobalDriver,
    private val localRepository: LocalContentRepository,
    private val firebaseRepository: FirebaseContentRepository
) : ViewModel() {
    var note: Note? = savedStateHandle["noteData"]

    private val _isOwnerState = MutableLiveData(false)
    val isOwnerState: LiveData<Boolean> get() = _isOwnerState

    private val _titleErrorMessageState = MutableLiveData<UiText>()
    val titleErrorMessageState: LiveData<UiText> get() = _titleErrorMessageState

    private val _contentErrorMessageState = MutableLiveData<UiText>()
    val contentErrorMessageState: LiveData<UiText> get() = _contentErrorMessageState

    private val _isNoteSavedState = MutableLiveData(false)
    val isNoteSavedState: LiveData<Boolean> get() = _isNoteSavedState

    private val _isNoteCreatedState = MutableLiveData(false)
    val isNoteCreatedState: LiveData<Boolean> get() = _isNoteCreatedState

    private val _isNoteUpdatedState = MutableLiveData(false)
    val isNoteUpdatedState: LiveData<Boolean> get() = _isNoteUpdatedState

    private val _isNoteDeletedState = MutableLiveData(false)
    val isNoteDeletedState: LiveData<Boolean> get() = _isNoteDeletedState

    init {
        // checks if the note is owned by the current user
        if (note?.userId == null || note?.userId == globalDriver.userId) {
            _isOwnerState.value = true
        } else {
            _isOwnerState.value = false
        }
    }

    /**
     * Adds the current note to the user's personal collection.
     */
    fun addNoteToCollection() {
        val currentNote = note
        if (currentNote == null) {
            globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.could_not_add_a_null_note_to_the_personal_collection
            )
            return
        }

        val userId = globalDriver.userId
        if (userId == null) {
            globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.could_not_add_the_note_to_the_personal_collection_because_the_user_id_is_null
            )
            return
        }

        val newNote = currentNote.copy(
            id = generateId(),
            userId = userId,
            public = false,
            createdAt = Date(),
            updatedAt = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = localRepository.upsertNote(newNote)
            when (result) {
                UpsertResult.Success -> {
                    saveNoteRemote(newNote)
                    _isNoteSavedState.postValue(true)
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_SUCCESS,
                        R.string.note_has_been_added_to_the_personal_collection
                    )
                }

                is UpsertResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_add_the_note_to_the_personal_collection
                )
            }
        }
    }

    /**
     * Creates and saves a note with the given [title] and [content]. The note will be saved
     * in both the local and remote databases.
     */
    fun saveNote(title: String, content: String) {
        if (!validateFields(title, content)) return // checks if the provided data is valid

        val newNote = Note(title = title, content = content)

        viewModelScope.launch(Dispatchers.IO) {
            val result = localRepository.upsertNote(newNote)
            when (result) {
                UpsertResult.Success -> {
                    saveNoteRemote(newNote)
                    _isNoteCreatedState.postValue(true)
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_SUCCESS,
                        R.string.note_created_successfully
                    )
                }

                is UpsertResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_create_the_note
                )
            }
        }
    }

    /**
     * Saves the given [note] in the remote database.
     */
    private suspend fun saveNoteRemote(note: Note) {
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
     * Updates the [current note][note] with the given [title][newTitle] and [content][newContent].
     * The note will be updated in both the local and remote databases.
     */
    fun updateNote(newTitle: String, newContent: String) {
        val currentNote = note
        if (currentNote == null) {
            globalDriver.showPopupBanner(
                PopupBanner.TYPE_FAILURE,
                R.string.could_not_update_the_note_because_the_note_is_null
            )
            return
        }
        if (!validateFields(newTitle, newContent)) return // not valid

        val updatedNote = currentNote.copy(
            title = newTitle,
            content = newContent,
            updatedAt = Date()
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = localRepository.updateNote(updatedNote)
            when (result) {
                UpdateResult.Success -> {
                    // updates the given note locally
                    note?.title = newTitle
                    note?.content = newContent

                    updateNoteRemote(updatedNote)
                    _isNoteUpdatedState.postValue(true)
                    globalDriver.showPopupBanner(
                        PopupBanner.TYPE_SUCCESS,
                        R.string.note_updated_successfully
                    )
                }

                is UpdateResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_update_the_note
                )
            }
        }
    }

    /**
     * Updates the [current note][note] with this [newNoteData] in the remote database.
     */
    private suspend fun updateNoteRemote(newNoteData: Note) {
        val noteId = newNoteData.id
        val newData = mapOf(
            "title" to newNoteData.title,
            "content" to newNoteData.content,
            "updatedAt" to newNoteData.updatedAt
        )

        viewModelScope.launch {
            val result = firebaseRepository.updateNote(noteId, newData)
            when (result) {
                UpdateResult.Success -> {}
                is UpdateResult.Error -> globalDriver.showPopupBanner(
                    PopupBanner.TYPE_FAILURE,
                    result.message ?: R.string.could_not_update_backed_up_note
                )
            }
        }
    }

    /**
     * Checks if the given [title] and [content] are valid. If not, it updates the data
     * holders accordingly.
     * @return `true` is valid, `false` otherwise.
     */
    private fun validateFields(title: String, content: String): Boolean {
        val result = NoteValidator.validateFields(title, content)
        when (result) {
            NoteValidator.NoteResult.Valid -> return true

            NoteValidator.NoteResult.InvalidTitleLength -> {
                _titleErrorMessageState.value = UiText.Resource(
                    R.string.note_title_must_be_between_x_and_y_characters,
                    NoteValidator.MIN_NOTE_TITLE_LENGTH, NoteValidator.MAX_NOTE_TITLE_LENGTH
                )
                return false
            }

            NoteValidator.NoteResult.InvalidContentLength -> {
                _contentErrorMessageState.value = UiText.Resource(
                    R.string.note_content_must_be_between_x_and_y_characters,
                    NoteValidator.MIN_NOTE_CONTENT_LENGTH, NoteValidator.MAX_NOTE_CONTENT_LENGTH
                )
                return false
            }
        }
    }

    /**
     * Sets the note saving status to [isSaved].
     */
    fun setNoteSavedStatus(isSaved: Boolean) {
        _isNoteSavedState.value = isSaved
    }

    /**
     * Sets the note creation status to [isCreated].
     */
    fun setNoteCreationStatus(isCreated: Boolean) {
        _isNoteCreatedState.value = isCreated
    }

    /**
     * Sets the note update status to [isUpdated].
     */
    fun setNoteUpdateStatus(isUpdated: Boolean) {
        _isNoteUpdatedState.value = isUpdated
    }

    /**
     * Sets the note deletion status to [isDeleted].
     */
    fun setNoteDeletionStatus(isDeleted: Boolean) {
        _isNoteDeletedState.value = isDeleted
    }
}