package com.madalin.notelo.settings.domain

import android.util.Log
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.repository.remote.FirebaseContentRepository
import com.madalin.notelo.core.domain.result.GetNotesResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult
import com.madalin.notelo.core.presentation.GlobalDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Date

/**
 * Object that handles the synchronization of notes between the local and remote databases.
 */
object NotesSynchronizer : KoinComponent {
    private val globalDriver: GlobalDriver by inject()
    private val localRepository: LocalContentRepository by inject()
    private val firebaseRepository: FirebaseContentRepository by inject()

    private class DatabaseNotes(
        val localNotes: GetNotesResult,
        val remoteNotes: GetNotesResult
    )

    private class InconsistentNotes(
        val unsyncedNotes: List<Note>,
        val updatedNotes: List<Note>
    )

    private sealed class OperationResult {
        data object Success : OperationResult()
        data class Error(val message: String) : OperationResult()
    }

    sealed class SynchronizationResult {
        data object Success : SynchronizationResult()
        data class Error(val message: String) : SynchronizationResult()
    }

    /**
     * Starts the synchronization process.
     */
    suspend fun start(): SynchronizationResult {
        val userId = globalDriver.userId ?: return SynchronizationResult.Error("Could not synchronize notes because the user ID is null")

        // fetches local and remote notes
        val notes = fetchLocalAndRemoteNotes(userId)
        val localResult = notes.localNotes
        val remoteResult = notes.remoteNotes

        // handles fetch results
        if (localResult is GetNotesResult.Error && remoteResult is GetNotesResult.Error) {
            return SynchronizationResult.Error("Could not fetch notes from the local and remote databases")
        }

        // applies sync actions
        val localNotes = (localResult as GetNotesResult.Success).notes
        val remoteNotes = (remoteResult as GetNotesResult.Success).notes

        return when (val syncResult = applySyncActions(localNotes, remoteNotes)) {
            OperationResult.Success -> SynchronizationResult.Success
            is OperationResult.Error -> SynchronizationResult.Error(syncResult.message)
        }
    }

    /**
     * Starts the synchronization process in a flow.
     */
    fun startFlow() = flow {
        val userId = globalDriver.userId
        if (userId == null) {
            emit(SynchronizationResult.Error("Could not synchronize notes because the user ID is null"))
            return@flow
        }

        // fetches local and remote notes
        val notes = fetchLocalAndRemoteNotes(userId)
        val localResult = notes.localNotes
        val remoteResult = notes.remoteNotes

        // handles fetch results
        if (localResult is GetNotesResult.Error && remoteResult is GetNotesResult.Error) {
            emit(SynchronizationResult.Error("Could not fetch notes from the local and remote databases"))
            return@flow
        }

        // applies sync actions
        val localNotes = (localResult as GetNotesResult.Success).notes
        val remoteNotes = (remoteResult as GetNotesResult.Success).notes
        when (val syncResult = applySyncActions(localNotes, remoteNotes)) {
            OperationResult.Success -> emit(SynchronizationResult.Success)
            is OperationResult.Error -> {
                emit(SynchronizationResult.Error(syncResult.message))
                return@flow
            }
        }
    }

    /**
     * Fetches the local and remote notes from the local and remote databases.
     */
    private suspend fun fetchLocalAndRemoteNotes(userId: String): DatabaseNotes = coroutineScope {
        val remoteNotesDeferred = async(Dispatchers.IO) { firebaseRepository.getNotesByUserId(userId) }
        val localNotesDeferred = async(Dispatchers.IO) { localRepository.getAllNotes() }

        val remoteNotes = remoteNotesDeferred.await()
        val localNotes = localNotesDeferred.await()

        DatabaseNotes(localNotes, remoteNotes)
    }

    /**
     * Compares the [local][localNotes] and [remote][remoteNotes] lists of notes and returns the
     * ones that are different or not present in both lists.
     */
    private fun findInconsistentNotes(
        localNotes: List<Note>, remoteNotes: List<Note>
    ): InconsistentNotes {
        val unsyncedNotes = mutableListOf<Note>()
        val updatedNotes = mutableListOf<Note>()

        val localNotesMap = localNotes.associateBy { it.id }
        val remoteNotesMap = remoteNotes.associateBy { it.id }

        // checks for unsynced or updated notes in local database
        for (remoteNote in remoteNotes) {
            val localNote = localNotesMap[remoteNote.id]
            if (localNote == null) {
                // remote note not found in local database
                unsyncedNotes.add(remoteNote)
            } else if (!localNote.isIdenticalTo(remoteNote)) {
                // local and remote note exists but differ
                updatedNotes.add(remoteNote)
            }
        }

        // checks for unsynced notes in remote database
        for (localNote in localNotes) {
            val remoteNote = remoteNotesMap[localNote.id]
            if (remoteNote == null) {
                // local note not found in remote database
                unsyncedNotes.add(localNote)
            } else if (!localNote.isIdenticalTo(remoteNote)) {
                // local and remote note exists but differ (if not already added)
                if (!updatedNotes.contains(remoteNote)) {
                    updatedNotes.add(localNote)
                }
            }
        }

        return InconsistentNotes(unsyncedNotes, updatedNotes)
    }

    /**
     * Checks if the two notes are identical.
     */
    private fun Note.isIdenticalTo(other: Note): Boolean {
        return this.title == other.title &&
                this.content == other.content &&
                this.public == other.public &&
                this.createdAt == other.createdAt &&
                this.updatedAt == other.updatedAt
    }

    /**
     * Applies the synchronization actions to the [local][localNotes] and [remote][remoteNotes]
     * database notes based on the unsynced or updated criteria.
     */
    private suspend fun applySyncActions(
        localNotes: List<Note>, remoteNotes: List<Note>
    ): OperationResult {
        val inconsistentNotes = findInconsistentNotes(localNotes, remoteNotes)
        val unsyncedNotes = inconsistentNotes.unsyncedNotes
        val updatedNotes = inconsistentNotes.updatedNotes
        var result: OperationResult = OperationResult.Success

        for (note in unsyncedNotes) {
            val actionResult = if (localNotes.any { it.id == note.id }) {
                // this note is in the local database but not in the remote one, save it remotely
                saveNoteToRemoteDatabase(note)
            } else {
                // this note is in the remote database but not in the local one, save it locally
                saveNoteToLocalDatabase(note)
            }

            if (actionResult is OperationResult.Error) {
                result = result.takeUnless { it is OperationResult.Error } ?: actionResult
            }
        }

        for (note in updatedNotes) {
            // this note exists in both databases but differs, decide whether to update local or remote
            val localNote = localNotes.find { it.id == note.id }

            if (localNote == null) continue

            val actionResult = if (shouldUpdateLocal(note, localNote)) {
                // remote note is more recent, update local
                updateNoteInLocalDatabase(note)
            } else {
                // local note is more recent, update remote
                updateNoteInRemoteDatabase(localNote)
            }

            if (actionResult is OperationResult.Error) {
                result = result.takeUnless { it is OperationResult.Error } ?: actionResult
            }
        }

        return result
    }

    /**
     * Checks if the [remote note][remoteNote] is more recent than the [local][localNote] one.
     */
    private fun shouldUpdateLocal(remoteNote: Note, localNote: Note): Boolean {
        val remoteUpdatedAt = remoteNote.updatedAt ?: Date(0)
        val localUpdatedAt = localNote.updatedAt ?: Date(0)

        // if remote note is more recent or local note has never been updated, update the local
        return remoteUpdatedAt.after(localUpdatedAt)
    }

    /**
     * Saves the given [note] in the remote database.
     */
    private suspend fun saveNoteToRemoteDatabase(note: Note): OperationResult {
        when (val result = firebaseRepository.createNote(note)) {
            UpsertResult.Success -> {
                Log.d("NotesSynchronizer", "Note ${note.id} saved to remote database")
                return OperationResult.Success
            }

            is UpsertResult.Error -> {
                val message = "Could not save note ${note.id} to remote database: ${result.message}"
                Log.e("NotesSynchronizer", message)
                return OperationResult.Error(message)
            }
        }
    }

    /**
     * Saves the given [note] in the local database.
     */
    private suspend fun saveNoteToLocalDatabase(note: Note): OperationResult {
        when (val result = localRepository.upsertNote(note)) {
            UpsertResult.Success -> {
                Log.d("NotesSynchronizer", "Note ${note.id} saved to local database")
                return OperationResult.Success
            }

            is UpsertResult.Error -> {
                val message = "Could not save note ${note.id} to local database: ${result.message}"
                Log.e("NotesSynchronizer", message)
                return OperationResult.Error(message)
            }
        }
    }

    /**
     * Updates the given [note] in the local database.
     */
    private suspend fun updateNoteInLocalDatabase(note: Note): OperationResult {
        when (val result = localRepository.updateNote(note)) {
            UpdateResult.Success -> {
                Log.d("NotesSynchronizer", "Note ${note.id} updated in local database")
                return OperationResult.Success
            }

            is UpdateResult.Error -> {
                val message = "Could not update note ${note.id} in local database: ${result.message}"
                Log.e("NotesSynchronizer", message)
                return OperationResult.Error(message)
            }
        }
    }

    /**
     * Updates the given [note] in the remote database.
     */
    private suspend fun updateNoteInRemoteDatabase(note: Note): OperationResult {
        val newData = mapOf(
            "title" to note.title,
            "content" to note.content,
            "public" to note.public,
            "updatedAt" to note.updatedAt
        )
        when (val result = firebaseRepository.updateNote(note.id, newData)) {
            UpdateResult.Success -> {
                Log.d("NotesSynchronizer", "Note ${note.id} updated in remote database")
                return OperationResult.Success
            }

            is UpdateResult.Error -> {
                val message = "Could not update note ${note.id} in remote database: ${result.message}"
                Log.e("NotesSynchronizer", message)
                return OperationResult.Error(message)
            }
        }
    }
}