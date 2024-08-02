package com.madalin.notelo.core.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.madalin.notelo.core.data.local.entity.NoteEntity
import com.madalin.notelo.core.data.local.relation.NoteWithCategoryAndTags
import com.madalin.notelo.core.data.local.relation.NullTag
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("UPDATE notes SET categoryId = :categoryId WHERE id = :noteId")
    suspend fun updateNoteCategory(noteId: String, categoryId: String?)

    /*@Query("SELECT * FROM notes " +
            "INNER JOIN categories ON notes.categoryId = categories.id " +
            "INNER JOIN notes_tags ON notes.id = notes_tags.noteId " +
            "INNER JOIN tags ON notes_tags.tagId = tags.id")*/
    @Transaction
    @Query("SELECT * FROM notes")
    fun getNotesWithCategoryAndTagsObserver(): Flow<List<NoteWithCategoryAndTags>>

    @Query("SELECT * FROM notes WHERE categoryId = :categoryId")
    suspend fun getNotesByCategoryId(categoryId: String): List<NoteEntity>

    @Query(
        """SELECT tags.*, notes.* FROM notes
        LEFT JOIN notes_tags ON notes.id = notes_tags.noteId
        LEFT JOIN tags ON notes_tags.tagId = tags.id
        WHERE notes.categoryId = :categoryId"""
    )
    fun getNotesInCategoryMappedByTagsObserver(categoryId: String): Flow<Map<NullTag, List<NoteEntity>>>
}