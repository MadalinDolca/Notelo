package com.madalin.notelo.core.domain.repository.remote

import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult

/**
 * Repository interface that contains content related methods for Firestore.
 */
interface FirebaseContentRepository {
    /**
     * Adds the given [note] to Firestore and returns an [UpdateResult].
     */
    suspend fun createNote(note: Note): UpsertResult

    /**
     * Updates the note that has this [noteId] with the given [newData] in Firestore and returns an
     * [UpdateResult].
     */
    suspend fun updateNote(noteId: String, newData: Map<String, Any?>): UpdateResult

    /**
     * Deletes the note that has this [noteId] from Firestore and returns a [DeleteResult].
     */
    suspend fun deleteNote(noteId: String): DeleteResult
}