package com.madalin.notelo.core.data.remote.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.remote.FirebaseContentRepository
import com.madalin.notelo.core.domain.result.DeleteResult
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
}