package com.madalin.notelo.core.domain.repository.local

import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.GetNoteResult
import com.madalin.notelo.core.domain.result.GetNotesResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult

interface LocalContentRepository {
    /**
     * Upserts the given [note] in the database and returns an [UpdateResult].
     */
    suspend fun upsertNote(note: Note): UpsertResult

    /**
     * Updates the given [note] in the database and returns an [UpdateResult].
     */
    suspend fun updateNote(note: Note): UpdateResult

    /**
     * Deletes the given [note] from the database and returns an [DeleteResult].
     */
    suspend fun deleteNote(note: Note): DeleteResult

    /**
     * Obtains the note with the given [noteId] and returns a [GetNoteResult].
     */
    suspend fun getNote(noteId: String): GetNoteResult

    /**
     * Obtains all notes from the database and returns a [GetNotesResult].
     */
    suspend fun getAllNotes(): GetNotesResult
}