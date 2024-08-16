package com.madalin.notelo.core.domain.repository.local

import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.GetCategoriesResult
import com.madalin.notelo.core.domain.result.GetCategoryResult
import com.madalin.notelo.core.domain.result.GetNoteResult
import com.madalin.notelo.core.domain.result.GetNotesResult
import com.madalin.notelo.core.domain.result.GetTagsResult
import com.madalin.notelo.core.domain.result.MoveNoteResult
import com.madalin.notelo.core.domain.result.TagsReplaceResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult
import kotlinx.coroutines.flow.Flow

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
     * Obtains the note with the given [noteId] from the database and returns a [GetNoteResult].
     */
    suspend fun getNoteById(noteId: String): GetNoteResult

    /**
     * Obtains all notes from the database and returns a [GetNotesResult].
     */
    suspend fun getAllNotes(): GetNotesResult

    /**
     * Updates the visibility of the note with the given [noteId] to [isPublic] in the database and
     * returns an [UpdateResult].
     */
    suspend fun updateNoteVisibility(noteId: String, isPublic: Boolean): UpdateResult

    /**
     * Deletes the given [note] and its related data from the database and returns an [DeleteResult].
     */
    suspend fun deleteNoteAndRelatedData(note: Note): DeleteResult

    /**
     * Obtains the note with the given [noteId] from the database alongside the category it is in
     * and the category tags applied to it and returns a [GetNoteResult].
     */
    suspend fun getNoteWithCategoryAndTagsByNoteId(noteId: String): GetNoteResult

    /**
     * Obtains all notes alongside the category they are in and the category tags applied to them
     * from the database and returns them as a [Flow].
     */
    fun getNotesWithCategoryAndTagsObserver(): Flow<List<Note>>

    /**
     * Moves the given [note] to the given [category] and replaces its previous associated tags with
     * the given [tags] and returns a [MoveNoteResult].
     */
    suspend fun moveNoteToCategoryWithTags(note: Note, category: Category, tags: List<Tag>): MoveNoteResult

    /**
     * Upserts the given [category] in the database and returns an [UpdateResult].
     */
    suspend fun upsertCategory(category: Category): UpsertResult

    /**
     * Updates the given [category] in the database and returns an [UpdateResult].
     */
    suspend fun updateCategory(category: Category): UpdateResult

    /**
     * Deletes the given [category] and its related data from the database and returns an [DeleteResult].
     */
    suspend fun deleteCategoryAndRelatedData(category: Category): DeleteResult

    /**
     * Obtains the category with the given [categoryId] and returns a [GetCategoryResult].
     */
    suspend fun getCategoryById(categoryId: String): GetCategoryResult

    /**
     * Obtains the category with the given [categoryId] and returns it as a [Flow].
     */
    suspend fun getCategoryByIdObserver(categoryId: String): Flow<Category>

    /**
     * Obtains all categories from the database and returns a [GetCategoriesResult].
     */
    suspend fun getCategories(): GetCategoriesResult

    /**
     * Obtains all categories from the database and returns them as a [Flow].
     */
    fun getCategoriesObserver(): Flow<List<Category>>

    /**
     * Upserts the given [tag] in the database and returns an [UpsertResult].
     */
    suspend fun upsertTag(tag: Tag): UpsertResult

    /**
     * Updates the given [tag] in the database and returns an [UpdateResult].
     */
    suspend fun updateTag(tag: Tag): UpdateResult

    /**
     * Deletes the given [tag] and its related data from the database and returns an [DeleteResult].
     */
    suspend fun deleteTagAndRelatedData(tag: Tag): DeleteResult

    /**
     * Obtains the tags associated with the given [categoryId] and returns a [GetTagsResult].
     */
    suspend fun getTagsByCategoryId(categoryId: String): GetTagsResult

    /**
     * Obtains the tags associated with the given [categoryId] and returns them as a [Flow].
     */
    fun getTagsByCategoryIdObserver(categoryId: String): Flow<List<Tag>>

    /**
     * Obtains lists of notes mapped by possible tags that belong to the given [categoryId] and
     * returns them as a [Flow].
     */
    fun getNotesInCategoryMappedByTagsObserver(categoryId: String): Flow<Map<Tag, List<Note>>>

    /**
     * Replaces the tags of this [note] with the given [tags] and returns a [TagsReplaceResult].
     */
    suspend fun replaceNoteTags(note: Note, tags: List<Tag>): TagsReplaceResult
}