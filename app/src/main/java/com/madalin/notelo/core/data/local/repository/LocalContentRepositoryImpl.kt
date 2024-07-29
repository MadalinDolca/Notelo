package com.madalin.notelo.core.data.local.repository

import com.madalin.notelo.core.data.local.dao.NoteDao
import com.madalin.notelo.core.data.local.mapper.toDomainModel
import com.madalin.notelo.core.data.local.mapper.toEntity
import com.madalin.notelo.core.domain.model.Note
import com.madalin.notelo.core.domain.repository.local.LocalContentRepository
import com.madalin.notelo.core.domain.result.UpsertResult

class LocalContentRepositoryImpl(
    private val noteDao: NoteDao
) : LocalContentRepository {
    override suspend fun getAllNotes(): List<Note> {
        return noteDao.getAllNotes().map { it.toDomainModel() }
    }

    override suspend fun upsertNote(note: Note): UpsertResult {
        try {
            noteDao.upsertNote(note.toEntity())
            return UpsertResult.Success
        } catch (e: Exception) {
            return UpsertResult.Error(e.message)
        }
    }
}