package com.madalin.notelo.core.data.remote.mapper

import com.madalin.notelo.core.data.remote.model.NoteDocument
import com.madalin.notelo.core.domain.model.Note

/**
 * Returns a [Note] representation of this [NoteDocument].
 */
fun NoteDocument.toDomainModel() = Note(
    id = id,
    userId = userId,
    title = title,
    content = content,
    public = public,
    createdAt = createdAt,
    updatedAt = updatedAt
)

/**
 * Returns a [NoteDocument] representation of this [Note].
 */
fun Note.toDocument() = NoteDocument(
    id = id,
    userId = userId,
    title = title,
    content = content,
    public = public,
    createdAt = createdAt,
    updatedAt = updatedAt
)