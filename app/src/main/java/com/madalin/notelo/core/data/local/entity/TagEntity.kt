package com.madalin.notelo.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId"),
        Index(
            value = ["name", "categoryId"],
            unique = true
        )
    ]
)
data class TagEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val name: String,
    val createdAt: Date,
    val updatedAt: Date?
)