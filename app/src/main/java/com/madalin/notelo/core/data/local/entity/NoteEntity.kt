package com.madalin.notelo.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.madalin.notelo.core.domain.util.generateId
import java.util.Date

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String = generateId(),
    val userId: String?,
    val title: String,
    val content: String,
    val createdAt: Date? = Date(),
    val updatedAt: Date? = Date()
)