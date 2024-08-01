package com.madalin.notelo.core.data.local.mapper

import com.madalin.notelo.core.data.local.entity.NoteEntity
import com.madalin.notelo.core.data.local.entity.NoteTagCrossRefEntity
import com.madalin.notelo.core.data.local.relation.NoteWithCategoryAndTags
import com.madalin.notelo.core.domain.model.Note

/**
 * Returns a [Note] representation of this [NoteEntity].
 */
fun NoteEntity.toNoteDomainModel() = Note(
    id = id,
    userId = userId,
    categoryId = categoryId,
    title = title,
    content = content,
    public = public,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Returns a [NoteEntity] representation of this [Note].
 */
fun Note.toNoteEntity() = NoteEntity(
    id = id,
    userId = userId,
    categoryId = categoryId,
    title = title,
    content = content,
    public = public,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Returns a list of [NoteTagCrossRefEntity] representation of this [Note].
 */
fun Note.toNoteTagCrossRefEntities(): List<NoteTagCrossRefEntity> {
    val noteTagCrossRefs = mutableListOf<NoteTagCrossRefEntity>()
    tags.forEach { tag ->
        noteTagCrossRefs.add(NoteTagCrossRefEntity(id, tag.id))
    }

    return noteTagCrossRefs
}

/**
 * Returns a [Note] representation of this [NoteWithCategoryAndTags].
 */
fun NoteWithCategoryAndTags.toNoteDomainModel(): Note {
    val note = note.toNoteDomainModel()
    note.category = category?.toCategoryDomainModel()
    note.tags = tags.map { it.toTagDomainModel() }

    return note
}