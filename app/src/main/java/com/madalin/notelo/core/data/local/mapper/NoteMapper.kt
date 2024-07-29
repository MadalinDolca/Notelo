package com.madalin.notelo.core.data.local.mapper

import com.madalin.notelo.core.data.local.entity.NoteEntity
import com.madalin.notelo.core.domain.model.Note

/**
 * Converts this [NoteEntity] to [Note].
 */
fun NoteEntity.toDomainModel() = Note(
    id = id,
    userId = userId,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Converts this [Note] to [NoteEntity].
 */
fun Note.toEntity() = NoteEntity(
    id = id,
    userId = userId,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt
)