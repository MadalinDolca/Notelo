package com.madalin.notelo.core.data.local.repository

import com.madalin.notelo.core.data.local.dao.CategoryDao
import com.madalin.notelo.core.data.local.dao.NoteDao
import com.madalin.notelo.core.data.local.dao.NoteTagDao
import com.madalin.notelo.core.data.local.dao.TagDao
import com.madalin.notelo.core.data.local.mapper.toDomainModel
import com.madalin.notelo.core.data.local.mapper.toEntity
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.GetCategoriesResult
import com.madalin.notelo.core.domain.result.GetCategoryResult
import com.madalin.notelo.core.domain.result.GetTagsResult
import com.madalin.notelo.core.domain.result.UpdateResult
import com.madalin.notelo.core.domain.result.UpsertResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class LocalContentRepositoryImpl(
    private val noteDao: NoteDao,
    private val categoryDao: CategoryDao,
    private val tagDao: TagDao,
    private val noteTagDao: NoteTagDao
) : LocalContentRepository {

    override suspend fun upsertNote(note: Note): UpsertResult {
        try {
            noteDao.upsertNote(note.toEntity())
            return UpsertResult.Success
        } catch (e: Exception) {
            return UpsertResult.Error(e.message)
        }
    }

    override suspend fun updateNote(note: Note): UpdateResult {
        try {
            noteDao.updateNote(note.toEntity())
            return UpdateResult.Success
        } catch (e: Exception) {
            return UpdateResult.Error(e.message)
        }
    }

    override suspend fun deleteNoteAndRelatedData(note: Note): DeleteResult {
        try {
            noteDao.deleteNote(note.toEntity()) // deletes the note
            noteTagDao.deleteNoteTagByNoteId(note.id) // deletes the note tags association
            return DeleteResult.Success
        } catch (e: Exception) {
            return DeleteResult.Error(e.message)
        }
    }

    /*    override suspend fun getNoteWithTagsByNoteId(noteId: String): GetNoteResult {
            try {
                val noteWithTags = noteDao.getNoteWithTagsByNoteId(noteId)
                return GetNoteResult.Success(noteWithTags.toDomainModel())
            } catch (e: Exception) {
                return GetNoteResult.Error(e.message)
            }
        }*/

    /*override fun getNotesWithTagsObserver(): Flow<List<Note>> {
        return noteDao.getNotesWithTagsObserver()
            .distinctUntilChanged()
            .map { list ->
                list.map { noteWithTags ->
                    noteWithTags.toDomainModel()
                }
            }
    }*/

    override fun getNotesWithCategoryAndTagsObserver(): Flow<List<Note>> {
        return noteDao.getNotesWithCategoryAndTagsObserver()
            .distinctUntilChanged()
            .map { list ->
                list.map { noteWithCategoryAndTags ->
                    noteWithCategoryAndTags.toDomainModel()
                }
            }
    }

    override suspend fun upsertCategory(category: Category): UpsertResult {
        try {
            categoryDao.upsertCategory(category.toEntity())
            return UpsertResult.Success
        } catch (e: Exception) {
            return UpsertResult.Error(e.message)
        }
    }

    override suspend fun updateCategory(category: Category): UpdateResult {
        try {
            categoryDao.updateCategory(category.toEntity())
            return UpdateResult.Success
        } catch (e: Exception) {
            return UpdateResult.Error(e.message)
        }
    }

    override suspend fun deleteCategoryAndRelatedData(category: Category): DeleteResult {
        try {
            // retrieve all notes in the category
            val notesInCategory = noteDao.getNotesInCategoryByCategoryId(category.id)

            // for each note, delete its tag relationships
            notesInCategory.forEach { note ->
                noteTagDao.deleteNoteTagByNoteId(note.id)
            }

            // delete the tags associated with the category
            tagDao.deleteCategoryTags(category.id)

            // delete the category itself
            categoryDao.deleteCategory(category.toEntity())

            return DeleteResult.Success
        } catch (e: Exception) {
            return DeleteResult.Error(e.message)
        }
    }

    override suspend fun getCategoryById(categoryId: String): GetCategoryResult {
        try {
            val category = categoryDao.getCategoryById(categoryId)
            return GetCategoryResult.Success(category.toDomainModel())
        } catch (e: Exception) {
            return GetCategoryResult.Error(e.message)
        }
    }

    override suspend fun getCategories(): GetCategoriesResult {
        try {
            val categories = categoryDao.getCategories().map { it.toDomainModel() }
            return GetCategoriesResult.Success(categories)
        } catch (e: Exception) {
            return GetCategoriesResult.Error(e.message)
        }
    }

    override fun getCategoriesObserver(): Flow<List<Category>> {
        return categoryDao.getCategoriesObserver()
            .distinctUntilChanged()
            .map { list ->
                list.map { categoryEntity ->
                    categoryEntity.toDomainModel()
                }
            }
    }

    override suspend fun upsertTag(tag: Tag): UpsertResult {
        try {
            tagDao.upsertTag(tag.toEntity())
            return UpsertResult.Success
        } catch (e: Exception) {
            return UpsertResult.Error(e.message)
        }
    }

    override suspend fun updateTag(tag: Tag): UpdateResult {
        try {
            tagDao.updateTag(tag.toEntity())
            return UpdateResult.Success
        } catch (e: Exception) {
            return UpdateResult.Error(e.message)
        }
    }

    override suspend fun deleteTagAndRelatedData(tag: Tag): DeleteResult {
        try {
            // deletes the note-tag associations
            noteTagDao.deleteNoteTagByTagId(tag.id)

            // deletes the tag itself
            tagDao.deleteTag(tag.toEntity())

            return DeleteResult.Success
        } catch (e: Exception) {
            return DeleteResult.Error(e.message)
        }
    }

    override suspend fun getTagsByCategoryId(categoryId: String): GetTagsResult {
        try {
            val tags = tagDao.getTagsByCategoryId(categoryId).map { it.toDomainModel() }
            return GetTagsResult.Success(tags)
        } catch (e: Exception) {
            return GetTagsResult.Error(e.message)
        }
    }

    override fun getTagsByCategoryIdObserver(categoryId: String): Flow<List<Tag>> {
        return tagDao.getTagsByCategoryIdObserver(categoryId)
            .distinctUntilChanged()
            .map { list ->
                list.map { tagEntity ->
                    tagEntity.toDomainModel()
                }
            }
    }
}