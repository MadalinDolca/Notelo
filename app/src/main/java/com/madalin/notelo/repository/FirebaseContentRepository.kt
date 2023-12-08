package com.madalin.notelo.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.madalin.notelo.model.Category
import com.madalin.notelo.model.Note
import com.madalin.notelo.model.Tag
import com.madalin.notelo.model.User
import com.madalin.notelo.user.UserFailure

/**
 * Repository interface that contains content related methods for Firestore.
 */
interface FirebaseContentRepository {
    /**
     * Obtains the current user data from [Firestore][Firebase.firestore] and starts listening for updates.
     * @param onSuccess function invoked when data fetching succeeds with the data inside [User]
     * @param onFailure function invoked when data fetching fails with [UserFailure] as the error type
     */
    fun startListeningForUserData(onSuccess: (User) -> Unit, onFailure: (UserFailure) -> Unit)

    /**
     * Obtains the notes associated with the given [userId] and starts listening for changes.
     * @param onSuccess function that will be invoked when the fetching process succeeded
     * - [List] parameter contains the user notes
     * @param onFailure function that will be invoked when the fetching process failed
     * - [String] parameter may contain the failure message
     */
    fun getNotesByUserIdListener(
        userId: String,
        onSuccess: (List<Note>) -> Unit, onFailure: (String?) -> Unit
    )

    /**
     * Obtains the notes associated with the given [categoryId] and starts listening for changes.
     * @param onSuccess Invoked when the fetching process succeeded. [List] parameter contains the
     * obtained notes.
     * @param onFailure Invoked when the fetching process failed. [String] parameter may contain the
     * failure message.
     */
    fun getNotesByCategoryIdListener(
        categoryId: String,
        onSuccess: (List<Note>) -> Unit, onFailure: (String?) -> Unit
    )

    /**
     * Obtains the tags associated with the given [categoryId] and starts listening for changes.
     * @param onSuccess Invoked when the fetching process succeeded. [List] parameter contains the
     * obtained tags.
     * @param onFailure Invoked when the fetching process failed. [String] parameter may contain the
     * failure message.
     */
    fun getTagsByCategoryIdListener(
        categoryId: String,
        onSuccess: (List<Tag>) -> Unit, onFailure: (String?) -> Unit
    )

    /**
     * Obtains the categories associated with the given [userId] and starts listening for changes.
     * @param onSuccess invoked when the fetching process succeeded with the obtained list of categories
     * @param onFailure invoked when the fetching process failed with a [String] parameter that may
     * contain the failure message
     */
    fun getCategoriesByUserIdListener(
        userId: String,
        onSuccess: (List<Category>) -> Unit, onFailure: (String?) -> Unit
    )

    /**
     * Obtains the categories associated with the given [userId].
     * @param onSuccess invoked when the fetching process succeeded with the obtained list of categories
     * @param onFailure invoked when the fetching process failed with a [String] parameter that may
     * contain the failure message
     */
    fun getCategoriesByUserId(
        userId: String,
        onSuccess: (List<Category>) -> Unit, onFailure: (String?) -> Unit
    )

    /**
     * Obtains the tags associated with the given [categoryId].
     * @param onSuccess Invoked when the fetching process succeeded with the obtained list of tags.
     * @param onFailure Invoked when the fetching process failed with a [String] parameter that
     * may contain the failure message.
     */
    fun getTagsByCategoryId(
        categoryId: String,
        onSuccess: (List<Tag>) -> Unit, onFailure: (String?) -> Unit
    )

    /**
     * Creates a note with the given [note] data in the database.
     * @param onSuccess Invoked when the adding process succeeded.
     * @param onFailure Invoked when the adding process failed.
     */
    fun createNote(note: Note, onSuccess: () -> Unit, onFailure: () -> Unit)

    /**
     * Updates the note that has this [noteId] with the given [newData].
     * @param onSuccess Invoked when the updating process succeeded.
     * @param onFailure Invoked when the updating process failed with a [String] parameter that
     * may contain the failure message.
     */
    fun updateNote(
        noteId: String, newData: Map<String, Any?>,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    )

    /**
     * Deletes the note that has this [noteId].
     * @param onSuccess Invoked when the deletion process succeeded.
     * @param onFailure Invoked when the deletion process failed with a [String] parameter that
     * may contain the failure message.
     */
    fun deleteNote(
        noteId: String,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    )

    /**
     * Creates a new category with the given [category] data in the database.
     * @param onSuccess Invoked when the adding process succeeded.
     * @param onFailure Invoked when the adding process failed. [String] parameter may contain the
     * failure message.
     */
    fun createCategory(category: Category, onSuccess: () -> Unit, onFailure: (String?) -> Unit)

    /**
     * Updates the category that has this [categoryId] with the given [newData].
     * @param onSuccess Invoked when the update process succeeded.
     * @param onFailure Invoked when the update process failed. [String] parameter may contain the
     * failure message.
     */
    fun updateCategory(
        categoryId: String, newData: Map<String, Any?>,
        onSuccess: () -> Unit, onFailure: (String?) -> Unit
    )

    /**
     * Deletes the category that has this [categoryId].
     * @param onSuccess Invoked when the deletion process succeeds.
     * @param onFailure Invoked when the deletion process failed. [String] parameter may contain the
     * failure message.
     */
    fun deleteCategory(categoryId: String, onSuccess: () -> Unit, onFailure: (String?) -> Unit)
}