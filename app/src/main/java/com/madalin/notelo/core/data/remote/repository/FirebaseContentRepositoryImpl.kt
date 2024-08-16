package com.madalin.notelo.core.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.madalin.notelo.core.data.remote.mapper.toDomainModel
import com.madalin.notelo.core.data.remote.model.NoteDocument
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.remote.FirebaseContentRepository
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.GetNotesResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult
import com.madalin.notelo.core.domain.util.DBCollection
import kotlinx.coroutines.tasks.await

class FirebaseContentRepositoryImpl(
    private val firestore: FirebaseFirestore
) : FirebaseContentRepository {

    override suspend fun createNote(note: Note): UpsertResult {
        val docRef = firestore.collection(DBCollection.NOTES).document(note.id)
        try {
            docRef.set(note).await()
            return UpsertResult.Success
        } catch (e: Exception) {
            return UpsertResult.Error(e.message)
        }
    }

    override suspend fun updateNote(
        noteId: String, newData: Map<String, Any?>
    ): UpdateResult {
        val docRef = firestore.collection(DBCollection.NOTES).document(noteId)
        try {
            docRef.update(newData).await()
            return UpdateResult.Success
        } catch (e: Exception) {
            return UpdateResult.Error(e.message)
        }
    }

    override suspend fun deleteNote(noteId: String): DeleteResult {
        val docRef = firestore.collection(DBCollection.NOTES).document(noteId)
        try {
            docRef.delete().await()
            return DeleteResult.Success
        } catch (e: Exception) {
            return DeleteResult.Error(e.message)
        }
    }

    override suspend fun getNotesByUserId(userId: String): GetNotesResult {
        val colRef = firestore.collection(DBCollection.NOTES)
        val filter = colRef.whereEqualTo("userId", userId)

        try {
            val notes = filter.get().await().toObjects<NoteDocument>()
            return GetNotesResult.Success(notes.map { it.toDomainModel() })
        } catch (e: Exception) {
            return GetNotesResult.Error(e.message)
        }
    }

    override suspend fun getAllPublicNotes(): GetNotesResult {
        val docRef = firestore.collection(DBCollection.NOTES)
        val filter = docRef.whereEqualTo("public", true)
        try {
            val noteDocuments = filter.get().await().toObjects<NoteDocument>()
            val notes = noteDocuments.map { it.toDomainModel() }

            return GetNotesResult.Success(notes)
        } catch (e: Exception) {
            return GetNotesResult.Error(e.message)
        }
    }

    override suspend fun getAllPublicNotesByQuery(query: String): GetNotesResult {
        val docRef = firestore.collection(DBCollection.NOTES)
        val filter = docRef.whereEqualTo("public", true)
        try {
            val noteDocuments = filter.get().await().toObjects<NoteDocument>()
            val notes = noteDocuments
                .map { it.toDomainModel() }
                .filter {
                    it.title.contains(query, true)
                            || it.content.contains(query, true)
                }

            return GetNotesResult.Success(notes)
        } catch (e: Exception) {
            return GetNotesResult.Error(e.message)
        }
    }
}