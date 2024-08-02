package com.madalin.notelo.core.data.local.repository

import com.madalin.notelo.core.data.local.dao.CategoryDao
import com.madalin.notelo.core.data.local.dao.NoteDao
import com.madalin.notelo.core.data.local.dao.NoteTagDao
import com.madalin.notelo.core.data.local.dao.TagDao
import com.madalin.notelo.core.data.local.mapper.toCategoryDomainModel
import com.madalin.notelo.core.data.local.mapper.toCategoryEntity
import com.madalin.notelo.core.data.local.mapper.toNoteDomainModel
import com.madalin.notelo.core.data.local.mapper.toNoteEntity
import com.madalin.notelo.core.data.local.mapper.toNoteTagCrossRefEntities
import com.madalin.notelo.core.data.local.mapper.toTagDomainModel
import com.madalin.notelo.core.data.local.mapper.toTagEntity
import com.madalin.notelo.core.domain.model.Category
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.model.Tag
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.result.DeleteResult
import com.madalin.notelo.core.domain.result.GetCategoriesResult
import com.madalin.notelo.core.domain.result.GetCategoryResult
import com.madalin.notelo.core.domain.result.GetNoteResult
import com.madalin.notelo.core.domain.result.GetTagsResult
import com.madalin.notelo.core.domain.result.MoveNoteResult
import com.madalin.notelo.core.domain.result.TagsReplaceResult
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
            noteDao.upsertNote(note.toNoteEntity())
            return UpsertResult.Success
        } catch (e: Exception) {
            return UpsertResult.Error(e.message)
        }
    }

    override suspend fun updateNote(note: Note): UpdateResult {
        try {
            noteDao.updateNote(note.toNoteEntity())
            return UpdateResult.Success
        } catch (e: Exception) {
            return UpdateResult.Error(e.message)
        }
    }

    override suspend fun getNoteById(noteId: String): GetNoteResult {
        try {
            val note = noteDao.getNoteById(noteId)
            return GetNoteResult.Success(note.toNoteDomainModel())
        } catch (e: Exception) {
            return GetNoteResult.Error(e.message)
        }
    }

    override suspend fun updateNoteVisibility(noteId: String, isPublic: Boolean): UpdateResult {
        try {
            noteDao.updateNoteVisibility(noteId, isPublic)
            return UpdateResult.Success
        } catch (e: Exception) {
            return UpdateResult.Error(e.message)
        }
    }

    override suspend fun deleteNoteAndRelatedData(note: Note): DeleteResult {
        try {
            noteDao.deleteNote(note.toNoteEntity()) // deletes the note
            noteTagDao.deleteNoteTagByNoteId(note.id) // deletes the note tags association
            return DeleteResult.Success
        } catch (e: Exception) {
            return DeleteResult.Error(e.message)
        }
    }

    override suspend fun getNoteWithCategoryAndTagsByNoteId(noteId: String): GetNoteResult {
        try {
            val note = noteDao.getNoteWithCategoryAndTagsByNoteId(noteId)
            return GetNoteResult.Success(note.toNoteDomainModel())
        } catch (e: Exception) {
            return GetNoteResult.Error(e.message)
        }
    }

    override fun getNotesWithCategoryAndTagsObserver(): Flow<List<Note>> {
        return noteDao.getNotesWithCategoryAndTagsObserver()
            .distinctUntilChanged()
            .map { list ->
                list.map { noteWithCategoryAndTags ->
                    noteWithCategoryAndTags.toNoteDomainModel()
                }
            }
    }

    override suspend fun moveNoteToCategoryWithTags(note: Note, category: Category, tags: List<Tag>): MoveNoteResult {
        try {
            val categoryId = if (category.id == Category.ID_UNCATEGORIZED) null else category.id
            val noteTags = note.apply { this.tags = tags }.toNoteTagCrossRefEntities()

            noteTagDao.deleteNoteTagByNoteId(note.id) // deletes the previous note-tag associations
            noteDao.updateNoteCategory(note.id, categoryId) // updates the note category
            noteTagDao.upsertNoteTags(noteTags) // inserts the new note-tag associations

            return MoveNoteResult.Success
        } catch (e: Exception) {
            return MoveNoteResult.Error(e.message)
        }
    }

    override suspend fun upsertCategory(category: Category): UpsertResult {
        try {
            categoryDao.upsertCategory(category.toCategoryEntity())
            return UpsertResult.Success
        } catch (e: Exception) {
            return UpsertResult.Error(e.message)
        }
    }

    override suspend fun updateCategory(category: Category): UpdateResult {
        try {
            categoryDao.updateCategory(category.toCategoryEntity())
            return UpdateResult.Success
        } catch (e: Exception) {
            return UpdateResult.Error(e.message)
        }
    }

    override suspend fun deleteCategoryAndRelatedData(category: Category): DeleteResult {
        try {
            // retrieve all notes in the category
            val notesInCategory = noteDao.getNotesByCategoryId(category.id)

            // for each note, delete its tag relationships
            notesInCategory.forEach { note ->
                noteTagDao.deleteNoteTagByNoteId(note.id)
            }

            // delete the tags associated with the category
            tagDao.deleteCategoryTags(category.id)

            // delete the category itself
            categoryDao.deleteCategory(category.toCategoryEntity())

            return DeleteResult.Success
        } catch (e: Exception) {
            return DeleteResult.Error(e.message)
        }
    }

    override suspend fun getCategoryById(categoryId: String): GetCategoryResult {
        try {
            val category = categoryDao.getCategoryById(categoryId)
            return GetCategoryResult.Success(category.toCategoryDomainModel())
        } catch (e: Exception) {
            return GetCategoryResult.Error(e.message)
        }
    }

    override suspend fun getCategoryByIdObserver(categoryId: String): Flow<Category> {
        return categoryDao.getCategoryByIdObserver(categoryId)
            .map { it.toCategoryDomainModel() }
    }

    override suspend fun getCategories(): GetCategoriesResult {
        try {
            val categories = categoryDao.getCategories().map { it.toCategoryDomainModel() }
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
                    categoryEntity.toCategoryDomainModel()
                }
            }
    }

    override suspend fun upsertTag(tag: Tag): UpsertResult {
        try {
            tagDao.upsertTag(tag.toTagEntity())
            return UpsertResult.Success
        } catch (e: Exception) {
            return UpsertResult.Error(e.message)
        }
    }

    override suspend fun updateTag(tag: Tag): UpdateResult {
        try {
            tagDao.updateTag(tag.toTagEntity())
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
            tagDao.deleteTag(tag.toTagEntity())

            return DeleteResult.Success
        } catch (e: Exception) {
            return DeleteResult.Error(e.message)
        }
    }

    override suspend fun getTagsByCategoryId(categoryId: String): GetTagsResult {
        try {
            val tags = tagDao.getTagsByCategoryId(categoryId).map { it.toTagDomainModel() }
            return GetTagsResult.Success(tags)
        } catch (e: Exception) {
            return GetTagsResult.Error(e.message)
        }
    }

    override fun getTagsByCategoryIdObserver(categoryId: String): Flow<List<Tag>> {
        return tagDao.getTagsByCategoryIdObserver(categoryId)
            .distinctUntilChanged()
            .map { list ->
                list.map { it.toTagDomainModel() }
            }
    }

    override fun getNotesInCategoryMappedByTagsObserver(categoryId: String): Flow<Map<Tag, List<Note>>> {
        return noteDao.getNotesInCategoryMappedByTagsObserver(categoryId)
            .map { entityMap ->
                entityMap
                    .mapKeys { (nullTag, _) -> nullTag.toTagDomainModel() }
                    .mapValues { (_, noteEntities) -> noteEntities.map { it.toNoteDomainModel() } }
            }
    }

    override suspend fun replaceNoteTags(note: Note, tags: List<Tag>): TagsReplaceResult {
        try {
            val noteTags = note.apply { this.tags = tags }.toNoteTagCrossRefEntities()

            noteTagDao.deleteNoteTagByNoteId(note.id) // deletes the previous note-tag associations
            noteTagDao.upsertNoteTags(noteTags) // inserts the new ones

            return TagsReplaceResult.Success
        } catch (e: Exception) {
            return TagsReplaceResult.Error(e.message)
        }
    }
}