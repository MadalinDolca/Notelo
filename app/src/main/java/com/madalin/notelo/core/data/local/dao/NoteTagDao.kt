package com.madalin.notelo.core.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.madalin.notelo.core.data.local.entity.NoteTagCrossRefEntity

@Dao
interface NoteTagDao {
    @Upsert
    suspend fun upsertNoteTag(noteTag: NoteTagCrossRefEntity)

    @Upsert
    suspend fun upsertNoteTags(noteTags: List<NoteTagCrossRefEntity>)

    @Query("DELETE FROM notes_tags WHERE tagId = :tagId")
    suspend fun deleteNoteTagByTagId(tagId: String)

    @Query("DELETE FROM notes_tags WHERE noteId = :noteId")
    suspend fun deleteNoteTagByNoteId(noteId: String)
}