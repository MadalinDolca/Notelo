package com.madalin.notelo.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId")]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val userId: String?,
    val categoryId: String?,
    val title: String,
    val content: String,
    val public: Boolean,
    val createdAt: Date,
    val updatedAt: Date?
)