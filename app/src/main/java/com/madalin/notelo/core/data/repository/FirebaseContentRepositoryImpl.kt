package com.madalin.notelo.core.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.domain.repository.FirebaseContentRepository
import com.madalin.notelo.core.domain.util.DBCollection

class FirebaseContentRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : FirebaseContentRepository {

    override fun getNotesByUserIdListener(
        userId: String,
        onSuccess: (List<Note>) -> Unit, onFailure: (String?) -> Unit
    ) {
        val notesList = mutableListOf<Note>()

        firestore.collection(DBCollection.NOTES)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onFailure(error.message)
                    return@addSnapshotListener
                }

                notesList.clear()

                if (snapshots != null) {
                    for (snapshot in snapshots) {
                        val note = snapshot.toObject<Note>()
                        note.id = snapshot.id // sets the note ID
                        notesList.add(note) // adds the note to the list
                    }
                }

                onSuccess(notesList)
            }
    }

    override fun getNotesByCategoryIdListener(
        categoryId: String,
        onSuccess: (List<Note>) -> Unit, onFailure: (String?) -> Unit
    ) {
        val notesList = mutableListOf<Note>()

        firestore.collection(DBCollection.NOTES)
            .whereEqualTo("categoryId", categoryId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onFailure(error.message)
                    return@addSnapshotListener
                }

                notesList.clear() // clears the current notes list

                if (snapshots != null) {
                    for (snapshot in snapshots) {
                        val note = snapshot.toObject<Note>()
                        note.id = snapshot.id
                        notesList.add(note)
                    }
                }

                onSuccess(notesList)
            }
    }

    override fun getTagsByCategoryIdListener(
        categoryId: String,
        onSuccess: (List<Tag>) -> Unit, onFailure: (String?) -> Unit
    ) {
        val tagsList = mutableListOf<Tag>()

        firestore.collection(DBCollection.TAGS)
            .whereEqualTo("categoryId", categoryId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onFailure(error.message)
                    return@addSnapshotListener
                }

                tagsList.clear() // clears the current tags list

                if (snapshots != null) {
                    for (snapshot in snapshots) {
                        val tag = snapshot.toObject<Tag>()
                        tag.id = snapshot.id
                        tagsList.add(tag)
                    }
                }

                onSuccess(tagsList)
            }
    }

    override fun getCategoriesByUserIdListener(
        userId: String,
        onSuccess: (List<Category>) -> Unit, onFailure: (String?) -> Unit
    ) {
        val categoriesList = mutableListOf<Category>()

        firestore.collection(DBCollection.CATEGORIES)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    onFailure(error.message)
                    return@addSnapshotListener
                }

                categoriesList.clear()

                if (snapshots != null) {
                    for (snapshot in snapshots) {
                        val category = snapshot.toObject<Category>()
                        category.id = snapshot.id // sets the category ID
                        categoriesList.add(category) // adds the category to the list
                    }
                }

                onSuccess(categoriesList)
            }
    }

    override fun getCategoriesByUserId(
        userId: String,
        onSuccess: (List<Category>) -> Unit, onFailure: (String?) -> Unit
    ) {
        val categoriesList = mutableListOf<Category>()

        firestore.collection(DBCollection.CATEGORIES)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshots ->
                categoriesList.clear()

                for (snapshot in snapshots) {
                    val category = snapshot.toObject<Category>()
                    category.id = snapshot.id // sets the category ID
                    categoriesList.add(category) // adds the category to the list
                }

                onSuccess(categoriesList)
            }
            .addOnFailureListener {
                onFailure(it.message)
            }
    }

    override fun getTagsByCategoryId(
        categoryId: String,
        onSuccess: (List<Tag>) -> Unit, onFailure: (String?) -> Unit
    ) {
        val tagsList = mutableListOf<Tag>()

        firestore.collection(DBCollection.TAGS)
            .whereEqualTo("categoryId", categoryId)
            .get()
            .addOnSuccessListener { snapshots ->
                tagsList.clear()

                for (snapshot in snapshots) {
                    val tag = snapshot.toObject<Tag>()
                    tag.id = snapshot.id
                    tagsList.add(tag)
                }

                onSuccess(tagsList)
            }
            .addOnFailureListener {
                onFailure(it.message)
            }
    }

    override fun createNote(
        note: Note,
        onSuccess: () -> Unit, onFailure: () -> Unit
    ) {
        firestore.collection(DBCollection.NOTES)
            .add(note)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure() }
    }

    override fun updateNote(
        noteId: String, newData: Map<String, Any?>,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    ) {
        firestore.collection(DBCollection.NOTES).document(noteId)
            .update(newData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message) }
    }

    override fun deleteNote(
        noteId: String,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    ) {
        firestore.collection(DBCollection.NOTES).document(noteId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message) }
    }

    override fun createCategory(
        category: Category,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    ) {
        firestore.collection(DBCollection.CATEGORIES).add(category)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message) }
    }

    override fun updateCategory(
        categoryId: String, newData: Map<String, Any?>,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    ) {
        firestore.collection(DBCollection.CATEGORIES)
            .document(categoryId)
            .update(newData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message) }
    }

    override fun deleteCategory(
        categoryId: String,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    ) {
        firestore.collection(DBCollection.CATEGORIES)
            .document(categoryId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it.message) }
    }
}